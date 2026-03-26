/**
 * ============================================================================
 * 결제 관련 타입 정의
 * ============================================================================
 */

/**
 * 결제 유형
 */
export type PaymentType = "PREMIUM_MONTHLY" | "PREMIUM_YEARLY";

/**
 * 결제 상태
 */
export type PaymentStatus =
  | "READY"
  | "APPROVED"
  | "CANCELLED"
  | "FAILED"
  | "REFUNDED";

/**
 * 결제 유형 정보
 */
export interface PaymentTypeInfo {
  type: PaymentType;
  name: string;
  price: number;
  originalPrice?: number;
  durationDays: number;
  description: string;
  badge?: string;
}

/**
 * 카카오페이 결제 준비 요청
 */
export interface KakaoPayReadyRequest {
  paymentType: PaymentType;
}

/**
 * 카카오페이 결제 준비 응답
 */
export interface KakaoPayReadyResponse {
  tid: string;
  nextRedirectPcUrl: string;
  nextRedirectMobileUrl: string;
  nextRedirectAppUrl: string;
  createdAt: string;
}

/**
 * 결제 DTO
 */
export interface PaymentDTO {
  id: number;
  paymentType: PaymentType;
  paymentTypeName: string;
  status: PaymentStatus;
  statusName: string;
  amount: number;
  paymentMethod: string | null;
  orderId: string;
  premiumStartDate: string | null;
  premiumEndDate: string | null;
  premiumActive: boolean;
  approvedAt: string | null;
  createdAt: string;
}

/**
 * 프리미엄 상태 응답
 */
export interface PremiumStatus {
  isPremium: boolean;
  premiumType: PaymentType | null;
  premiumTypeName: string | null;
  startDate: string | null;
  endDate: string | null;
  daysRemaining: number;
}

/**
 * 결제 통계 (관리자용)
 */
export interface PaymentStats {
  todayCount: number;
  todayAmount: number;
  monthCount: number;
  monthAmount: number;
  activePremiumMembers: number;
  totalPayments: number;
}

/**
 * 결제 유형 정보 목록
 */
export const PAYMENT_TYPES: PaymentTypeInfo[] = [
  {
    type: "PREMIUM_MONTHLY",
    name: "프리미엄 월간 구독",
    price: 3900,
    durationDays: 30,
    description: "한 달 동안 프리미엄 혜택을 누리세요",
  },
  {
    type: "PREMIUM_YEARLY",
    name: "프리미엄 연간 구독",
    price: 39000,
    originalPrice: 46800, // 3,900 * 12
    durationDays: 365,
    description: "2개월 무료! 연간 구독으로 더 저렴하게",
    badge: "2개월 무료",
  },
];

/**
 * 결제 상태 표시명 반환
 */
export function getStatusDisplayName(status: PaymentStatus): string {
  const statusMap: Record<PaymentStatus, string> = {
    READY: "결제 대기",
    APPROVED: "결제 완료",
    CANCELLED: "결제 취소",
    FAILED: "결제 실패",
    REFUNDED: "환불 완료",
  };
  return statusMap[status] || status;
}

/**
 * 금액 포맷팅
 */
export function formatPrice(price: number): string {
  return price.toLocaleString("ko-KR") + "원";
}
