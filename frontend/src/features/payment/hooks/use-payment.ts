/**
 * ============================================================================
 * 결제 관련 커스텀 훅
 * ============================================================================
 *
 * [역할]
 * TanStack Query를 사용하여 결제 API를 호출하고 상태를 관리합니다.
 *
 * [훅 목록]
 * - usePremiumStatus: 프리미엄 상태 조회
 * - useMyPayments: 내 결제 내역 조회
 * - usePaymentReady: 결제 준비 요청
 * - usePaymentApprove: 결제 승인 처리
 */

import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import * as paymentApi from "../api/payment-api";
import type { KakaoPayReadyRequest } from "../types";

/**
 * 결제 쿼리 키
 */
export const paymentKeys = {
  all: ["payment"] as const,
  premiumStatus: () => [...paymentKeys.all, "premium-status"] as const,
  myPayments: (page: number) => [...paymentKeys.all, "my", page] as const,
  detail: (id: number) => [...paymentKeys.all, "detail", id] as const,
  adminAll: (page: number) => [...paymentKeys.all, "admin", page] as const,
  adminStats: () => [...paymentKeys.all, "admin", "stats"] as const,
};

/**
 * 프리미엄 상태 조회 훅
 *
 * @description
 * 현재 로그인한 사용자의 프리미엄 구독 상태를 조회합니다.
 * 프리미엄 여부, 만료일, 남은 일수 등을 반환합니다.
 *
 * @example
 * const { data: status, isLoading } = usePremiumStatus();
 * if (status?.isPremium) {
 *   console.log(`프리미엄 만료일: ${status.endDate}`);
 * }
 */
export function usePremiumStatus() {
  return useQuery({
    queryKey: paymentKeys.premiumStatus(),
    queryFn: paymentApi.getPremiumStatus,
    staleTime: 5 * 60 * 1000, // 5분
  });
}

/**
 * 내 결제 내역 조회 훅
 *
 * @param page 페이지 번호 (1부터 시작)
 * @param size 페이지 크기
 *
 * @example
 * const { data } = useMyPayments(1);
 * data?.dtoList.map(payment => ...)
 */
export function useMyPayments(page: number = 1, size: number = 10) {
  return useQuery({
    queryKey: paymentKeys.myPayments(page),
    queryFn: () => paymentApi.getMyPayments(page, size),
    staleTime: 1 * 60 * 1000, // 1분
  });
}

/**
 * 결제 상세 조회 훅
 *
 * @param paymentId 결제 ID
 */
export function usePaymentDetail(paymentId: number) {
  return useQuery({
    queryKey: paymentKeys.detail(paymentId),
    queryFn: () => paymentApi.getPaymentDetail(paymentId),
    enabled: !!paymentId,
  });
}

/**
 * 결제 준비 요청 훅
 *
 * @description
 * 카카오페이 결제를 준비하고 redirect URL을 반환합니다.
 * 성공 시 사용자를 카카오페이 결제 페이지로 이동시켜야 합니다.
 *
 * @example
 * const { mutate: readyPayment, isPending } = usePaymentReady();
 *
 * const handlePayment = (type: PaymentType) => {
 *   readyPayment({ paymentType: type }, {
 *     onSuccess: (data) => {
 *       // 모바일/PC 구분하여 redirect
 *       const redirectUrl = isMobile() ? data.nextRedirectMobileUrl : data.nextRedirectPcUrl;
 *       window.location.href = redirectUrl;
 *     }
 *   });
 * };
 */
export function usePaymentReady() {
  return useMutation({
    mutationFn: (request: KakaoPayReadyRequest) => paymentApi.ready(request),
  });
}

/**
 * 결제 승인 처리 훅
 *
 * @description
 * 카카오페이에서 결제 완료 후 redirect될 때 사용합니다.
 * pg_token과 order_id를 전달하여 결제를 승인합니다.
 *
 * @example
 * const { mutate: approvePayment } = usePaymentApprove();
 *
 * useEffect(() => {
 *   const params = new URLSearchParams(location.search);
 *   const pgToken = params.get('pg_token');
 *   const orderId = params.get('order_id');
 *
 *   if (pgToken && orderId) {
 *     approvePayment({ pgToken, orderId });
 *   }
 * }, []);
 */
export function usePaymentApprove() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({ pgToken, orderId }: { pgToken: string; orderId: string }) =>
      paymentApi.approve(pgToken, orderId),
    onSuccess: () => {
      // 결제 승인 후 프리미엄 상태 갱신
      queryClient.invalidateQueries({ queryKey: paymentKeys.premiumStatus() });
      queryClient.invalidateQueries({ queryKey: paymentKeys.all });
    },
  });
}

/**
 * 결제 취소 처리 훅
 */
export function usePaymentCancel() {
  return useMutation({
    mutationFn: (orderId: string) => paymentApi.cancel(orderId),
  });
}

/**
 * 결제 실패 처리 훅
 */
export function usePaymentFail() {
  return useMutation({
    mutationFn: (orderId: string) => paymentApi.fail(orderId),
  });
}

// 관리자 훅

/**
 * 전체 결제 내역 조회 (관리자용)
 */
export function useAdminPayments(page: number = 1, size: number = 10) {
  return useQuery({
    queryKey: paymentKeys.adminAll(page),
    queryFn: () => paymentApi.getAllPayments(page, size),
  });
}

/**
 * 결제 통계 조회 (관리자용)
 */
export function usePaymentStats() {
  return useQuery({
    queryKey: paymentKeys.adminStats(),
    queryFn: paymentApi.getPaymentStats,
    staleTime: 1 * 60 * 1000, // 1분
  });
}
