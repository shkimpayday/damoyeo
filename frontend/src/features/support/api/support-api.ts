/**
 * ============================================================================
 * 상담 채팅 API
 * ============================================================================
 *
 * [역할]
 * 상담 관련 REST API를 호출합니다.
 *
 * @author damoyeo
 * @since 2025-03-16
 */

import { jwtAxios } from "@/lib/axios";
import type {
  SupportChatDTO,
  SupportMessageDTO,
  CreateSupportChatRequest,
  PageResponseDTO,
} from "../types";

const BASE_URL = "/api/support";

// ========================================================================
// 사용자 API
// ========================================================================

/**
 * 새 상담 생성
 *
 * @param request 상담 생성 요청 (제목, 첫 메시지)
 * @returns 생성된 상담 정보
 */
export async function createSupportChat(
  request: CreateSupportChatRequest
): Promise<SupportChatDTO> {
  const response = await jwtAxios.post<SupportChatDTO>(BASE_URL, request);
  return response.data;
}

/**
 * 내 상담 목록 조회
 *
 * @returns 상담 목록
 */
export async function getMySupportChats(): Promise<SupportChatDTO[]> {
  const response = await jwtAxios.get<SupportChatDTO[]>(`${BASE_URL}/my-chats`);
  return response.data;
}

/**
 * 활성 상담 조회 (진행 중인 상담)
 *
 * @returns 진행 중인 상담 (없으면 null)
 */
export async function getActiveSupportChat(): Promise<SupportChatDTO | null> {
  try {
    const response = await jwtAxios.get<SupportChatDTO>(`${BASE_URL}/active`);
    return response.data;
  } catch (error: unknown) {
    // 204 No Content = 활성 상담 없음
    if (
      error &&
      typeof error === "object" &&
      "response" in error &&
      (error as { response?: { status?: number } }).response?.status === 204
    ) {
      return null;
    }
    throw error;
  }
}

/**
 * 상담 상세 조회
 *
 * @param chatId 상담 ID
 * @returns 상담 상세 정보
 */
export async function getSupportChatDetail(
  chatId: number
): Promise<SupportChatDTO> {
  const response = await jwtAxios.get<SupportChatDTO>(`${BASE_URL}/${chatId}`);
  return response.data;
}

/**
 * 메시지 히스토리 조회
 *
 * @param chatId 상담 ID
 * @param page 페이지 번호 (1부터 시작)
 * @param size 페이지 크기
 * @returns 메시지 목록
 */
export async function getSupportMessages(
  chatId: number,
  page: number = 1,
  size: number = 50
): Promise<PageResponseDTO<SupportMessageDTO>> {
  const response = await jwtAxios.get<PageResponseDTO<SupportMessageDTO>>(
    `${BASE_URL}/${chatId}/messages`,
    { params: { page, size } }
  );
  return response.data;
}

/**
 * 상담 평가
 *
 * @param chatId 상담 ID
 * @param rating 평점 (1-5)
 */
export async function rateSupportChat(
  chatId: number,
  rating: number
): Promise<void> {
  await jwtAxios.post(`${BASE_URL}/${chatId}/rate`, { rating });
}

// ========================================================================
// 관리자 API
// ========================================================================

/**
 * 대기 중인 상담 목록 조회 (관리자용)
 *
 * @param page 페이지 번호
 * @param size 페이지 크기
 * @returns 대기 중인 상담 목록
 */
export async function getWaitingSupportChats(
  page: number = 1,
  size: number = 20
): Promise<PageResponseDTO<SupportChatDTO>> {
  const response = await jwtAxios.get<PageResponseDTO<SupportChatDTO>>(
    `${BASE_URL}/admin/waiting`,
    { params: { page, size } }
  );
  return response.data;
}

/**
 * 내가 담당 중인 상담 목록 (관리자용)
 *
 * @param page 페이지 번호
 * @param size 페이지 크기
 * @returns 담당 중인 상담 목록
 */
export async function getMyAssignedChats(
  page: number = 1,
  size: number = 20
): Promise<PageResponseDTO<SupportChatDTO>> {
  const response = await jwtAxios.get<PageResponseDTO<SupportChatDTO>>(
    `${BASE_URL}/admin/my-assigned`,
    { params: { page, size } }
  );
  return response.data;
}

/**
 * 전체 상담 목록 (관리자용)
 *
 * @param page 페이지 번호
 * @param size 페이지 크기
 * @returns 전체 상담 목록
 */
export async function getAllSupportChats(
  page: number = 1,
  size: number = 20
): Promise<PageResponseDTO<SupportChatDTO>> {
  const response = await jwtAxios.get<PageResponseDTO<SupportChatDTO>>(
    `${BASE_URL}/admin/all`,
    { params: { page, size } }
  );
  return response.data;
}

/**
 * 상담 배정 (관리자가 상담을 가져감)
 *
 * @param chatId 상담 ID
 * @returns 업데이트된 상담 정보
 */
export async function assignSupportChat(
  chatId: number
): Promise<SupportChatDTO> {
  const response = await jwtAxios.post<SupportChatDTO>(
    `${BASE_URL}/admin/${chatId}/assign`
  );
  return response.data;
}

/**
 * 상담 완료 처리 (관리자용)
 *
 * @param chatId 상담 ID
 * @returns 업데이트된 상담 정보
 */
export async function completeSupportChat(
  chatId: number
): Promise<SupportChatDTO> {
  const response = await jwtAxios.post<SupportChatDTO>(
    `${BASE_URL}/admin/${chatId}/complete`
  );
  return response.data;
}

/**
 * 대기 중인 상담 개수 조회 (관리자용)
 *
 * @returns 대기 중인 상담 개수
 */
export async function getWaitingCount(): Promise<number> {
  const response = await jwtAxios.get<number>(
    `${BASE_URL}/admin/waiting-count`
  );
  return response.data;
}
