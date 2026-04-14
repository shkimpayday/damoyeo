import { jwtAxios } from "@/lib/axios";
import type {
  BoardPostDTO,
  BoardPageResponse,
  BoardPostCreateRequest,
  BoardLikeToggleResponse,
  BoardCommentDTO,
  BoardCategory,
} from "../types";

/**
 * ============================================================================
 * 모임 게시판 API
 * ============================================================================
 *
 * [엔드포인트]
 * POST   /api/groups/{groupId}/board          - 게시글 작성
 * GET    /api/groups/{groupId}/board          - 게시글 목록 조회 (페이지네이션)
 * GET    /api/groups/{groupId}/board/{postId} - 게시글 상세
 * DELETE /api/board/posts/{postId}            - 게시글 삭제
 * POST   /api/board/posts/{postId}/like       - 좋아요 토글
 * GET    /api/board/posts/{postId}/comments   - 댓글 목록
 * POST   /api/board/posts/{postId}/comments   - 댓글 작성
 * DELETE /api/board/comments/{commentId}      - 댓글 삭제
 */

const API_BASE = "/api";

// 게시글 API

/**
 * 게시글 작성
 *
 * <p>텍스트 + 이미지(최대 5개)를 포함한 게시글을 작성합니다.</p>
 *
 * @param groupId 모임 ID
 * @param request 게시글 작성 요청 (카테고리, 제목, 본문, 이미지)
 * @returns 생성된 게시글 정보
 */
export async function createBoardPost(
  groupId: number,
  request: BoardPostCreateRequest
): Promise<BoardPostDTO> {
  const formData = new FormData();
  formData.append("category", request.category);
  formData.append("title", request.title);
  formData.append("content", request.content);

  if (request.files && request.files.length > 0) {
    request.files.forEach((file) => {
      formData.append("files", file);
    });
  }

  const response = await jwtAxios.post<BoardPostDTO>(
    `${API_BASE}/groups/${groupId}/board`,
    formData,
    {
      headers: { "Content-Type": "multipart/form-data" },
    }
  );
  return response.data;
}

/**
 * 게시글 목록 조회 (페이지네이션)
 *
 * @param groupId 모임 ID
 * @param page 페이지 번호 (0부터 시작)
 * @param size 페이지 크기 (기본 10)
 * @param category 카테고리 필터 (없으면 전체)
 * @returns 페이지네이션된 게시글 목록
 */
export async function getBoardPosts(
  groupId: number,
  page: number = 0,
  size: number = 10,
  category?: BoardCategory
): Promise<BoardPageResponse> {
  const response = await jwtAxios.get<BoardPageResponse>(
    `${API_BASE}/groups/${groupId}/board`,
    {
      params: { page, size, ...(category ? { category } : {}) },
    }
  );
  return response.data;
}

/**
 * 게시글 상세 조회
 *
 * @param groupId 모임 ID
 * @param postId 게시글 ID
 * @returns 게시글 상세 정보
 */
export async function getBoardPost(
  groupId: number,
  postId: number
): Promise<BoardPostDTO> {
  const response = await jwtAxios.get<BoardPostDTO>(
    `${API_BASE}/groups/${groupId}/board/${postId}`
  );
  return response.data;
}

/**
 * 게시글 삭제
 *
 * @param postId 삭제할 게시글 ID
 * @returns 성공 메시지
 */
export async function deleteBoardPost(
  postId: number
): Promise<{ message: string }> {
  const response = await jwtAxios.delete<{ message: string }>(
    `${API_BASE}/board/posts/${postId}`
  );
  return response.data;
}

// 좋아요 API

/**
 * 게시글 좋아요 토글
 *
 * <p>이미 좋아요한 경우 취소, 아닌 경우 추가</p>
 *
 * @param postId 게시글 ID
 * @returns 좋아요 상태 및 개수
 */
export async function toggleBoardPostLike(
  postId: number
): Promise<BoardLikeToggleResponse> {
  const response = await jwtAxios.post<BoardLikeToggleResponse>(
    `${API_BASE}/board/posts/${postId}/like`
  );
  return response.data;
}

// 댓글 API

/**
 * 댓글 목록 조회
 *
 * @param postId 게시글 ID
 * @returns 댓글 목록
 */
export async function getBoardComments(
  postId: number
): Promise<BoardCommentDTO[]> {
  const response = await jwtAxios.get<BoardCommentDTO[]>(
    `${API_BASE}/board/posts/${postId}/comments`
  );
  return response.data;
}

/**
 * 댓글 작성
 *
 * @param postId 게시글 ID
 * @param content 댓글 내용
 * @returns 작성된 댓글
 */
export async function addBoardComment(
  postId: number,
  content: string
): Promise<BoardCommentDTO> {
  const response = await jwtAxios.post<BoardCommentDTO>(
    `${API_BASE}/board/posts/${postId}/comments`,
    { content }
  );
  return response.data;
}

/**
 * 댓글 삭제
 *
 * @param commentId 삭제할 댓글 ID
 * @returns 성공 메시지
 */
export async function deleteBoardComment(
  commentId: number
): Promise<{ message: string }> {
  const response = await jwtAxios.delete<{ message: string }>(
    `${API_BASE}/board/comments/${commentId}`
  );
  return response.data;
}
