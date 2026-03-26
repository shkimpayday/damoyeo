/**
 * ============================================================================
 * 결제 API 클라이언트
 * ============================================================================
 *
 * [역할]
 * 카카오페이 결제 관련 API 호출을 담당합니다.
 *
 * [API 목록]
 * - ready: 결제 준비 요청
 * - approve: 결제 승인 처리
 * - cancel: 결제 취소 처리
 * - fail: 결제 실패 처리
 * - getMyPayments: 내 결제 내역 조회
 * - getPaymentDetail: 결제 상세 조회
 * - getPremiumStatus: 프리미엄 상태 확인
 */

import { jwtAxios } from "@/lib/axios";
import type { PageResponseDTO } from "@/features/groups/types";
import type {
  KakaoPayReadyRequest,
  KakaoPayReadyResponse,
  PaymentDTO,
  PremiumStatus,
  PaymentStats,
} from "../types";

const BASE_URL = "/api/payments";

/**
 * 결제 준비 요청
 *
 * @param request 결제 준비 요청 (paymentType)
 * @returns 카카오페이 redirect URL 등
 */
export async function ready(
  request: KakaoPayReadyRequest
): Promise<KakaoPayReadyResponse> {
  const response = await jwtAxios.post<KakaoPayReadyResponse>(
    `${BASE_URL}/ready`,
    request
  );
  return response.data;
}

/**
 * 결제 승인 처리
 *
 * @param pgToken 카카오페이 승인 토큰
 * @param orderId 주문번호
 * @returns 결제 완료 정보
 */
export async function approve(
  pgToken: string,
  orderId: string
): Promise<PaymentDTO> {
  const response = await jwtAxios.get<PaymentDTO>(`${BASE_URL}/approve`, {
    params: { pg_token: pgToken, order_id: orderId },
  });
  return response.data;
}

/**
 * 결제 취소 처리
 *
 * @param orderId 주문번호
 */
export async function cancel(orderId: string): Promise<void> {
  await jwtAxios.get(`${BASE_URL}/cancel`, {
    params: { order_id: orderId },
  });
}

/**
 * 결제 실패 처리
 *
 * @param orderId 주문번호
 */
export async function fail(orderId: string): Promise<void> {
  await jwtAxios.get(`${BASE_URL}/fail`, {
    params: { order_id: orderId },
  });
}

/**
 * 내 결제 내역 조회
 *
 * @param page 페이지 번호 (1부터 시작)
 * @param size 페이지 크기
 * @returns 결제 내역 목록
 */
export async function getMyPayments(
  page: number = 1,
  size: number = 10
): Promise<PageResponseDTO<PaymentDTO>> {
  const response = await jwtAxios.get<PageResponseDTO<PaymentDTO>>(BASE_URL, {
    params: { page, size },
  });
  return response.data;
}

/**
 * 결제 상세 조회
 *
 * @param paymentId 결제 ID
 * @returns 결제 상세 정보
 */
export async function getPaymentDetail(paymentId: number): Promise<PaymentDTO> {
  const response = await jwtAxios.get<PaymentDTO>(`${BASE_URL}/${paymentId}`);
  return response.data;
}

/**
 * 프리미엄 상태 확인
 *
 * @returns 프리미엄 상태 정보
 */
export async function getPremiumStatus(): Promise<PremiumStatus> {
  const response = await jwtAxios.get<PremiumStatus>(
    `${BASE_URL}/premium-status`
  );
  return response.data;
}

// ============================================================================
// 관리자 API
// ============================================================================

/**
 * 전체 결제 내역 조회 (관리자용)
 *
 * @param page 페이지 번호
 * @param size 페이지 크기
 * @returns 결제 내역 목록
 */
export async function getAllPayments(
  page: number = 1,
  size: number = 10
): Promise<PageResponseDTO<PaymentDTO>> {
  const response = await jwtAxios.get<PageResponseDTO<PaymentDTO>>(
    `${BASE_URL}/admin`,
    { params: { page, size } }
  );
  return response.data;
}

/**
 * 결제 통계 조회 (관리자용)
 *
 * @returns 결제 통계
 */
export async function getPaymentStats(): Promise<PaymentStats> {
  const response = await jwtAxios.get<PaymentStats>(`${BASE_URL}/admin/stats`);
  return response.data;
}
