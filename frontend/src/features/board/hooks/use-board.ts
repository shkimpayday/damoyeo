import {
  useQuery,
  useMutation,
  useQueryClient,
  useInfiniteQuery,
} from "@tanstack/react-query";
import {
  getBoardPosts,
  getBoardPost,
  createBoardPost,
  deleteBoardPost,
  toggleBoardPostLike,
  getBoardComments,
  addBoardComment,
  deleteBoardComment,
} from "../api/board-api";
import type {
  BoardCategory,
  BoardPageResponse,
  BoardPostCreateRequest,
  BoardPostDTO,
} from "../types";

/**
 * ============================================================================
 * 모임 게시판 Query Hooks
 * ============================================================================
 */

/** Query 키 팩토리 */
const boardKeys = {
  all: ["board"] as const,
  lists: () => [...boardKeys.all, "list"] as const,
  list: (groupId: number, category?: BoardCategory) =>
    [...boardKeys.lists(), groupId, category] as const,
  detail: (groupId: number, postId: number) =>
    [...boardKeys.all, "detail", groupId, postId] as const,
  comments: (postId: number) =>
    [...boardKeys.all, "comments", postId] as const,
};

// ========================================================================
// 게시글 Hooks
// ========================================================================

/**
 * 게시글 목록 조회 (무한 스크롤)
 *
 * @param groupId 모임 ID
 * @param category 카테고리 필터 (없으면 전체)
 * @param size 페이지 크기
 */
export function useBoardPostsInfinite(
  groupId: number,
  category?: BoardCategory,
  size: number = 10
) {
  return useInfiniteQuery<BoardPageResponse>({
    queryKey: [...boardKeys.list(groupId, category), "infinite"],
    queryFn: ({ pageParam = 0 }) =>
      getBoardPosts(groupId, pageParam as number, size, category),
    initialPageParam: 0,
    getNextPageParam: (lastPage) => {
      if (lastPage.next) return lastPage.current + 1;
      return undefined;
    },
    enabled: !!groupId,
    staleTime: 0,
    refetchOnMount: true,
  });
}

/**
 * 게시글 상세 조회
 *
 * @param groupId 모임 ID
 * @param postId 게시글 ID
 */
export function useBoardPost(groupId: number, postId: number) {
  return useQuery({
    queryKey: boardKeys.detail(groupId, postId),
    queryFn: () => getBoardPost(groupId, postId),
    enabled: !!groupId && !!postId,
  });
}

/**
 * 게시글 작성 Mutation
 */
export function useCreateBoardPost() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({
      groupId,
      request,
    }: {
      groupId: number;
      request: BoardPostCreateRequest;
    }) => createBoardPost(groupId, request),
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({
        queryKey: boardKeys.lists(),
      });
      queryClient.invalidateQueries({
        queryKey: boardKeys.list(variables.groupId),
      });
    },
  });
}

/**
 * 게시글 삭제 Mutation
 */
export function useDeleteBoardPost() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({ postId }: { postId: number; groupId: number }) =>
      deleteBoardPost(postId),
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({
        queryKey: boardKeys.lists(),
      });
      queryClient.invalidateQueries({
        queryKey: boardKeys.list(variables.groupId),
      });
    },
  });
}

// ========================================================================
// 좋아요 Hooks
// ========================================================================

/**
 * 게시글 좋아요 토글 Mutation (낙관적 업데이트)
 */
export function useToggleBoardPostLike() {
  const queryClient = useQueryClient();

  const updatePostLike = (posts: BoardPostDTO[], postId: number) =>
    posts.map((post) =>
      post.id === postId
        ? {
            ...post,
            liked: !post.liked,
            likeCount: post.liked ? post.likeCount - 1 : post.likeCount + 1,
          }
        : post
    );

  return useMutation({
    mutationFn: (postId: number) => toggleBoardPostLike(postId),

    onMutate: async (postId) => {
      await queryClient.cancelQueries({ queryKey: boardKeys.lists() });
      const previousData = queryClient.getQueriesData({
        queryKey: boardKeys.lists(),
      });

      queryClient.setQueriesData(
        { queryKey: boardKeys.lists() },
        // eslint-disable-next-line @typescript-eslint/no-explicit-any
        (oldData: any) => {
          if (!oldData) return oldData;
          if (oldData.pages) {
            return {
              ...oldData,
              pages: oldData.pages.map((page: BoardPageResponse) => ({
                ...page,
                dtoList: page.dtoList
                  ? updatePostLike(page.dtoList, postId)
                  : page.dtoList,
              })),
            };
          }
          if (oldData.dtoList) {
            return { ...oldData, dtoList: updatePostLike(oldData.dtoList, postId) };
          }
          return oldData;
        }
      );

      return { previousData };
    },

    onError: (_err, _postId, context) => {
      if (context?.previousData) {
        context.previousData.forEach(([queryKey, data]) => {
          queryClient.setQueryData(queryKey, data);
        });
      }
    },
  });
}

// ========================================================================
// 댓글 Hooks
// ========================================================================

/**
 * 댓글 목록 조회
 *
 * @param postId 게시글 ID
 */
export function useBoardComments(postId: number) {
  return useQuery({
    queryKey: boardKeys.comments(postId),
    queryFn: () => getBoardComments(postId),
    enabled: !!postId,
  });
}

/**
 * 댓글 작성 Mutation
 */
export function useAddBoardComment() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({ postId, content }: { postId: number; content: string }) =>
      addBoardComment(postId, content),
    onSuccess: (_newComment, variables) => {
      queryClient.invalidateQueries({
        queryKey: boardKeys.comments(variables.postId),
      });
      // 게시글 목록의 commentCount 증가
      queryClient.setQueriesData(
        { queryKey: boardKeys.lists() },
        // eslint-disable-next-line @typescript-eslint/no-explicit-any
        (oldData: any) => {
          if (!oldData) return oldData;
          const updateCount = (posts: BoardPostDTO[]) =>
            posts.map((p) =>
              p.id === variables.postId
                ? { ...p, commentCount: p.commentCount + 1 }
                : p
            );
          if (oldData.pages) {
            return {
              ...oldData,
              pages: oldData.pages.map((page: BoardPageResponse) => ({
                ...page,
                dtoList: page.dtoList ? updateCount(page.dtoList) : page.dtoList,
              })),
            };
          }
          if (oldData.dtoList) {
            return { ...oldData, dtoList: updateCount(oldData.dtoList) };
          }
          return oldData;
        }
      );
    },
  });
}

/**
 * 댓글 삭제 Mutation
 */
export function useDeleteBoardComment() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({
      commentId,
    }: {
      commentId: number;
      postId: number;
    }) => deleteBoardComment(commentId),
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({
        queryKey: boardKeys.comments(variables.postId),
      });
    },
  });
}
