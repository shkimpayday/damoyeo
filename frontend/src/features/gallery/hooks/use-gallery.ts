import { useQuery, useMutation, useQueryClient, useInfiniteQuery } from "@tanstack/react-query";
import {
  getGalleryPosts,
  getGalleryPostCount,
  getRecentGalleryPosts,
  uploadGalleryPost,
  deleteGalleryPost,
  togglePostLike,
  getPostComments,
  addPostComment,
  deletePostComment,
} from "../api/gallery-api";
import type { GalleryPageResponse, GalleryPostDTO } from "../types";

/**
 * ============================================================================
 * 갤러리 Query Hooks
 * ============================================================================
 *
 * [구조 변경]
 * - 기존: 개별 이미지(GalleryImage) 단위
 * - 변경: 게시물(GalleryPost) 단위 (여러 이미지 묶음)
 */

/** Query 키 팩토리 */
const galleryKeys = {
  all: ["gallery"] as const,
  lists: () => [...galleryKeys.all, "list"] as const,
  list: (groupId: number) => [...galleryKeys.lists(), groupId] as const,
  count: (groupId: number) => [...galleryKeys.all, "count", groupId] as const,
  recent: (groupId: number) => [...galleryKeys.all, "recent", groupId] as const,
  comments: (postId: number) => [...galleryKeys.all, "comments", postId] as const,
};

// 게시물 Hooks

/**
 * 갤러리 게시물 목록 조회 (페이지네이션)
 *
 * @param groupId 모임 ID
 * @param page 페이지 번호
 * @param size 페이지 크기
 */
export function useGalleryPosts(groupId: number, page: number = 0, size: number = 20) {
  return useQuery({
    queryKey: [...galleryKeys.list(groupId), page, size],
    queryFn: () => getGalleryPosts(groupId, page, size),
    enabled: !!groupId,
    staleTime: 0, // 항상 최신 데이터 요청
    refetchOnMount: true, // 마운트 시 다시 가져오기
  });
}

/**
 * 갤러리 게시물 목록 (무한 스크롤)
 *
 * @param groupId 모임 ID
 * @param size 페이지 크기 (기본 20)
 */
export function useGalleryPostsInfinite(groupId: number, size: number = 20) {
  return useInfiniteQuery<GalleryPageResponse>({
    queryKey: [...galleryKeys.list(groupId), "infinite"],
    queryFn: ({ pageParam = 0 }) => getGalleryPosts(groupId, pageParam as number, size),
    initialPageParam: 0,
    getNextPageParam: (lastPage) => {
      if (lastPage.next) {
        return lastPage.current + 1;
      }
      return undefined;
    },
    enabled: !!groupId,
    staleTime: 0, // 항상 최신 데이터 요청
    refetchOnMount: true, // 마운트 시 다시 가져오기
  });
}

/**
 * 갤러리 게시물 개수 조회
 *
 * @param groupId 모임 ID
 */
export function useGalleryPostCount(groupId: number) {
  return useQuery({
    queryKey: galleryKeys.count(groupId),
    queryFn: () => getGalleryPostCount(groupId),
    enabled: !!groupId,
  });
}

/**
 * 최신 게시물 미리보기 조회
 *
 * @param groupId 모임 ID
 * @param limit 조회 개수 (기본 4)
 */
export function useRecentGalleryPosts(groupId: number, limit: number = 4) {
  return useQuery({
    queryKey: [...galleryKeys.recent(groupId), limit],
    queryFn: () => getRecentGalleryPosts(groupId, limit),
    enabled: !!groupId,
  });
}

/**
 * 갤러리 게시물 업로드 Mutation
 */
export function useUploadGalleryPost() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({
      groupId,
      files,
      caption,
    }: {
      groupId: number;
      files: File[];
      caption?: string;
    }) => uploadGalleryPost(groupId, files, caption),
    onSuccess: (_, variables) => {
      // 캐시 무효화
      queryClient.invalidateQueries({
        queryKey: galleryKeys.list(variables.groupId),
      });
      queryClient.invalidateQueries({
        queryKey: galleryKeys.count(variables.groupId),
      });
      queryClient.invalidateQueries({
        queryKey: galleryKeys.recent(variables.groupId),
      });
    },
  });
}

/**
 * 갤러리 게시물 삭제 Mutation
 */
export function useDeleteGalleryPost() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({ postId }: { postId: number; groupId: number }) =>
      deleteGalleryPost(postId),
    onSuccess: (_, variables) => {
      // 캐시 무효화
      queryClient.invalidateQueries({
        queryKey: galleryKeys.list(variables.groupId),
      });
      queryClient.invalidateQueries({
        queryKey: galleryKeys.count(variables.groupId),
      });
      queryClient.invalidateQueries({
        queryKey: galleryKeys.recent(variables.groupId),
      });
    },
  });
}

// 좋아요 Hooks

/**
 * 게시물 좋아요 토글 Mutation
 *
 * <p>낙관적 업데이트(Optimistic Update) 적용:</p>
 * - 서버 응답 전에 UI를 즉시 업데이트
 * - 실패 시 이전 상태로 롤백
 */
export function useTogglePostLike() {
  const queryClient = useQueryClient();

  /**
   * 게시물 목록에서 특정 게시물의 좋아요 상태를 토글하는 헬퍼 함수
   */
  const updatePostInList = (posts: GalleryPostDTO[], postId: number) => {
    return posts.map((post) => {
      if (post.id === postId) {
        return {
          ...post,
          liked: !post.liked,
          likeCount: post.liked ? post.likeCount - 1 : post.likeCount + 1,
        };
      }
      return post;
    });
  };

  return useMutation({
    mutationFn: (postId: number) => togglePostLike(postId),

    // 낙관적 업데이트
    onMutate: async (postId) => {
      // 진행 중인 쿼리 취소
      await queryClient.cancelQueries({ queryKey: galleryKeys.lists() });

      // 이전 데이터 스냅샷
      const previousData = queryClient.getQueriesData({ queryKey: galleryKeys.lists() });

      // 모든 gallery list 쿼리 업데이트 (일반 쿼리 + 무한 스크롤 쿼리)
      queryClient.setQueriesData(
        { queryKey: galleryKeys.lists() },
        // eslint-disable-next-line @typescript-eslint/no-explicit-any
        (oldData: any) => {
          if (!oldData) return oldData;

          // 무한 스크롤 쿼리 (pages 배열 형태)
          if (oldData.pages) {
            return {
              ...oldData,
              pages: oldData.pages.map((page: GalleryPageResponse) => ({
                ...page,
                dtoList: page.dtoList ? updatePostInList(page.dtoList, postId) : page.dtoList,
              })),
            };
          }

          // 일반 페이지네이션 쿼리
          if (oldData.dtoList) {
            return {
              ...oldData,
              dtoList: updatePostInList(oldData.dtoList, postId),
            };
          }

          return oldData;
        }
      );

      return { previousData };
    },

    // 에러 시 롤백
    onError: (_err, _postId, context) => {
      if (context?.previousData) {
        context.previousData.forEach(([queryKey, data]) => {
          queryClient.setQueryData(queryKey, data);
        });
      }
    },
  });
}

// 댓글 Hooks

/**
 * 게시물 댓글 목록 조회
 *
 * @param postId 게시물 ID
 */
export function usePostComments(postId: number) {
  return useQuery({
    queryKey: galleryKeys.comments(postId),
    queryFn: () => getPostComments(postId),
    enabled: !!postId,
  });
}

/**
 * 댓글 작성 Mutation
 */
export function useAddPostComment() {
  const queryClient = useQueryClient();

  const updateCommentCount = (posts: GalleryPostDTO[], postId: number, delta: number) => {
    return posts.map((post) => {
      if (post.id === postId) {
        return {
          ...post,
          commentCount: post.commentCount + delta,
        };
      }
      return post;
    });
  };

  return useMutation({
    mutationFn: ({ postId, content }: { postId: number; content: string }) =>
      addPostComment(postId, content),
    onSuccess: (_newComment, variables) => {
      // 댓글 목록 캐시 무효화
      queryClient.invalidateQueries({
        queryKey: galleryKeys.comments(variables.postId),
      });

      // 게시물 목록의 commentCount 업데이트
      queryClient.setQueriesData(
        { queryKey: galleryKeys.lists() },
        // eslint-disable-next-line @typescript-eslint/no-explicit-any
        (oldData: any) => {
          if (!oldData) return oldData;

          // 무한 스크롤 쿼리 (pages 배열 형태)
          if (oldData.pages) {
            return {
              ...oldData,
              pages: oldData.pages.map((page: GalleryPageResponse) => ({
                ...page,
                dtoList: page.dtoList ? updateCommentCount(page.dtoList, variables.postId, 1) : page.dtoList,
              })),
            };
          }

          // 일반 페이지네이션 쿼리
          if (oldData.dtoList) {
            return {
              ...oldData,
              dtoList: updateCommentCount(oldData.dtoList, variables.postId, 1),
            };
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
export function useDeletePostComment() {
  const queryClient = useQueryClient();

  const updateCommentCount = (posts: GalleryPostDTO[], postId: number) => {
    return posts.map((post) => {
      if (post.id === postId) {
        return {
          ...post,
          commentCount: Math.max(0, post.commentCount - 1),
        };
      }
      return post;
    });
  };

  return useMutation({
    mutationFn: ({ commentId }: { commentId: number; postId: number }) =>
      deletePostComment(commentId),
    onSuccess: (_, variables) => {
      // 댓글 목록 캐시 무효화
      queryClient.invalidateQueries({
        queryKey: galleryKeys.comments(variables.postId),
      });

      // 게시물 목록의 commentCount 업데이트
      queryClient.setQueriesData(
        { queryKey: galleryKeys.lists() },
        // eslint-disable-next-line @typescript-eslint/no-explicit-any
        (oldData: any) => {
          if (!oldData) return oldData;

          // 무한 스크롤 쿼리 (pages 배열 형태)
          if (oldData.pages) {
            return {
              ...oldData,
              pages: oldData.pages.map((page: GalleryPageResponse) => ({
                ...page,
                dtoList: page.dtoList ? updateCommentCount(page.dtoList, variables.postId) : page.dtoList,
              })),
            };
          }

          // 일반 페이지네이션 쿼리
          if (oldData.dtoList) {
            return {
              ...oldData,
              dtoList: updateCommentCount(oldData.dtoList, variables.postId),
            };
          }

          return oldData;
        }
      );
    },
  });
}

// 하위 호환성을 위한 별칭 (deprecated)

/** @deprecated useGalleryPosts를 사용하세요 */
export const useGalleryImages = useGalleryPosts;

/** @deprecated useGalleryPostsInfinite를 사용하세요 */
export const useGalleryImagesInfinite = useGalleryPostsInfinite;

/** @deprecated useGalleryPostCount를 사용하세요 */
export const useGalleryImageCount = useGalleryPostCount;

/** @deprecated useRecentGalleryPosts를 사용하세요 */
export const useRecentGalleryImages = useRecentGalleryPosts;

/** @deprecated useUploadGalleryPost를 사용하세요 */
export const useUploadGalleryImages = useUploadGalleryPost;

/** @deprecated useDeleteGalleryPost를 사용하세요 */
export const useDeleteGalleryImage = useDeleteGalleryPost;

/** @deprecated useTogglePostLike를 사용하세요 */
export const useToggleImageLike = useTogglePostLike;

/** @deprecated usePostComments를 사용하세요 */
export const useImageComments = usePostComments;

/** @deprecated useAddPostComment를 사용하세요 */
export const useAddImageComment = useAddPostComment;

/** @deprecated useDeletePostComment를 사용하세요 */
export const useDeleteImageComment = useDeletePostComment;
