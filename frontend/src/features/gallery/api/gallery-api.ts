import { jwtAxios } from "@/lib/axios";
import type {
  GalleryPostDTO,
  GalleryPageResponse,
  GalleryCountResponse,
  LikeToggleResponse,
  GalleryCommentDTO,
} from "../types";

/**
 * ============================================================================
 * 갤러리 API
 * ============================================================================
 *
 * [구조 변경]
 * - 기존: 개별 이미지(GalleryImage) 단위
 * - 변경: 게시물(GalleryPost) 단위 (여러 이미지 묶음)
 *
 * [엔드포인트]
 * POST   /api/groups/{groupId}/gallery          - 게시물 업로드
 * GET    /api/groups/{groupId}/gallery          - 게시물 목록 조회
 * GET    /api/groups/{groupId}/gallery/count    - 게시물 개수 조회
 * GET    /api/groups/{groupId}/gallery/recent   - 최신 게시물 미리보기
 * DELETE /api/gallery/posts/{postId}            - 게시물 삭제
 * POST   /api/gallery/posts/{postId}/like       - 게시물 좋아요 토글
 * GET    /api/gallery/posts/{postId}/comments   - 댓글 목록 조회
 * POST   /api/gallery/posts/{postId}/comments   - 댓글 작성
 * DELETE /api/gallery/comments/{commentId}      - 댓글 삭제
 */

const API_BASE = "/api";

// 게시물 API

/**
 * 갤러리 게시물 업로드
 *
 * <p>여러 이미지를 하나의 게시물로 업로드합니다.</p>
 *
 * @param groupId 모임 ID
 * @param files 업로드할 파일들 (최대 10개)
 * @param caption 게시물 설명 (선택)
 * @returns 업로드된 게시물 정보
 */
export async function uploadGalleryPost(
  groupId: number,
  files: File[],
  caption?: string
): Promise<GalleryPostDTO> {
  const formData = new FormData();

  files.forEach((file) => {
    formData.append("files", file);
  });

  if (caption) {
    formData.append("caption", caption);
  }

  const response = await jwtAxios.post<GalleryPostDTO>(
    `${API_BASE}/groups/${groupId}/gallery`,
    formData,
    {
      headers: {
        "Content-Type": "multipart/form-data",
      },
    }
  );

  return response.data;
}

/**
 * 갤러리 게시물 목록 조회 (페이지네이션)
 *
 * @param groupId 모임 ID
 * @param page 페이지 번호 (0부터 시작)
 * @param size 페이지 크기 (기본 20)
 * @returns 페이지네이션된 게시물 목록
 */
export async function getGalleryPosts(
  groupId: number,
  page: number = 0,
  size: number = 20
): Promise<GalleryPageResponse> {
  const response = await jwtAxios.get<GalleryPageResponse>(
    `${API_BASE}/groups/${groupId}/gallery`,
    {
      params: { page, size },
    }
  );

  return response.data;
}

/**
 * 갤러리 게시물 개수 조회
 *
 * @param groupId 모임 ID
 * @returns 게시물 개수
 */
export async function getGalleryPostCount(
  groupId: number
): Promise<GalleryCountResponse> {
  const response = await jwtAxios.get<GalleryCountResponse>(
    `${API_BASE}/groups/${groupId}/gallery/count`
  );

  return response.data;
}

/**
 * 최신 게시물 미리보기 조회
 *
 * @param groupId 모임 ID
 * @param limit 조회할 개수 (기본 4, 최대 10)
 * @returns 최신 게시물 목록
 */
export async function getRecentGalleryPosts(
  groupId: number,
  limit: number = 4
): Promise<GalleryPostDTO[]> {
  const response = await jwtAxios.get<GalleryPostDTO[]>(
    `${API_BASE}/groups/${groupId}/gallery/recent`,
    {
      params: { limit },
    }
  );

  return response.data;
}

/**
 * 갤러리 게시물 삭제
 *
 * @param postId 삭제할 게시물 ID
 * @returns 성공 메시지
 */
export async function deleteGalleryPost(
  postId: number
): Promise<{ message: string }> {
  const response = await jwtAxios.delete<{ message: string }>(
    `${API_BASE}/gallery/posts/${postId}`
  );

  return response.data;
}

// 좋아요 API

/**
 * 게시물 좋아요 토글
 *
 * <p>이미 좋아요한 경우 취소, 아니면 좋아요 추가</p>
 *
 * @param postId 게시물 ID
 * @returns 좋아요 상태 및 개수
 */
export async function togglePostLike(
  postId: number
): Promise<LikeToggleResponse> {
  const response = await jwtAxios.post<LikeToggleResponse>(
    `${API_BASE}/gallery/posts/${postId}/like`
  );

  return response.data;
}

/**
 * 게시물 좋아요 개수 조회
 *
 * @param postId 게시물 ID
 * @returns 좋아요 개수
 */
export async function getPostLikeCount(
  postId: number
): Promise<{ likeCount: number }> {
  const response = await jwtAxios.get<{ likeCount: number }>(
    `${API_BASE}/gallery/posts/${postId}/like/count`
  );

  return response.data;
}

// 댓글 API

/**
 * 게시물에 댓글 작성
 *
 * @param postId 게시물 ID
 * @param content 댓글 내용
 * @returns 작성된 댓글
 */
export async function addPostComment(
  postId: number,
  content: string
): Promise<GalleryCommentDTO> {
  const response = await jwtAxios.post<GalleryCommentDTO>(
    `${API_BASE}/gallery/posts/${postId}/comments`,
    { content }
  );

  return response.data;
}

/**
 * 게시물의 댓글 목록 조회
 *
 * @param postId 게시물 ID
 * @returns 댓글 목록
 */
export async function getPostComments(
  postId: number
): Promise<GalleryCommentDTO[]> {
  const response = await jwtAxios.get<GalleryCommentDTO[]>(
    `${API_BASE}/gallery/posts/${postId}/comments`
  );

  return response.data;
}

/**
 * 댓글 삭제
 *
 * @param commentId 삭제할 댓글 ID
 * @returns 성공 메시지
 */
export async function deletePostComment(
  commentId: number
): Promise<{ message: string }> {
  const response = await jwtAxios.delete<{ message: string }>(
    `${API_BASE}/gallery/comments/${commentId}`
  );

  return response.data;
}

// 하위 호환성을 위한 별칭 (deprecated)

/** @deprecated uploadGalleryPost를 사용하세요 */
export const uploadGalleryImages = uploadGalleryPost;

/** @deprecated getGalleryPosts를 사용하세요 */
export const getGalleryImages = getGalleryPosts;

/** @deprecated getGalleryPostCount를 사용하세요 */
export const getGalleryImageCount = getGalleryPostCount;

/** @deprecated getRecentGalleryPosts를 사용하세요 */
export const getRecentGalleryImages = getRecentGalleryPosts;

/** @deprecated deleteGalleryPost를 사용하세요 */
export const deleteGalleryImage = deleteGalleryPost;

/** @deprecated togglePostLike를 사용하세요 */
export const toggleImageLike = togglePostLike;

/** @deprecated addPostComment를 사용하세요 */
export const addImageComment = addPostComment;

/** @deprecated getPostComments를 사용하세요 */
export const getImageComments = getPostComments;

/** @deprecated deletePostComment를 사용하세요 */
export const deleteImageComment = deletePostComment;
