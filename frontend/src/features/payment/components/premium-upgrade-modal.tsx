/**
 * ============================================================================
 * 프리미엄 업그레이드 모달
 * ============================================================================
 *
 * [역할]
 * 프리미엄 구독 선택 및 결제 진행을 위한 모달입니다.
 *
 * [기능]
 * - 월간/연간 구독 선택
 * - 프리미엄 혜택 안내
 * - 카카오페이 결제 연동
 */

import { useState } from "react";
import { X, Crown, Check, Loader2 } from "lucide-react";
import { usePaymentReady } from "../hooks/use-payment";
import { PAYMENT_TYPES, formatPrice, type PaymentType } from "../types";

interface PremiumUpgradeModalProps {
  isOpen: boolean;
  onClose: () => void;
}

/**
 * 프리미엄 혜택 목록
 */
const PREMIUM_BENEFITS = [
  "무제한 모임 생성 (일반: 2개)",
  "무제한 모임 인원 (일반: 30명)",
  "프리미엄 뱃지 표시",
  "광고 없는 깔끔한 화면",
  "모임 상세 통계 확인",
  "우선 고객 지원",
];

/**
 * 모바일 여부 확인
 */
function isMobile(): boolean {
  return /Android|webOS|iPhone|iPad|iPod|BlackBerry|IEMobile|Opera Mini/i.test(
    navigator.userAgent
  );
}

export function PremiumUpgradeModal({
  isOpen,
  onClose,
}: PremiumUpgradeModalProps) {
  const [selectedType, setSelectedType] = useState<PaymentType>("PREMIUM_YEARLY");
  const { mutate: readyPayment, isPending } = usePaymentReady();

  /**
   * 결제 진행
   */
  const handlePayment = () => {
    readyPayment(
      { paymentType: selectedType },
      {
        onSuccess: (data) => {
          // 모바일/PC 구분하여 redirect
          const redirectUrl = isMobile()
            ? data.nextRedirectMobileUrl
            : data.nextRedirectPcUrl;
          window.location.href = redirectUrl;
        },
        onError: (error) => {
          console.error("결제 준비 실패:", error);
          alert("결제 준비에 실패했습니다. 다시 시도해주세요.");
        },
      }
    );
  };

  if (!isOpen) return null;

  const selectedPlan = PAYMENT_TYPES.find((p) => p.type === selectedType);

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center">
      {/* 배경 오버레이 */}
      <div
        className="absolute inset-0 bg-black/50"
        onClick={onClose}
      />

      {/* 모달 컨텐츠 */}
      <div className="relative bg-white rounded-2xl w-full max-w-lg mx-4 max-h-[90vh] overflow-y-auto">
        {/* 헤더 */}
        <div className="sticky top-0 bg-gradient-to-r from-amber-500 to-orange-500 text-white p-6 rounded-t-2xl">
          <button
            onClick={onClose}
            className="absolute top-4 right-4 p-1 hover:bg-white/20 rounded-full transition-colors"
          >
            <X size={24} />
          </button>

          <div className="flex items-center gap-3">
            <div className="w-12 h-12 bg-white/20 rounded-full flex items-center justify-center">
              <Crown size={28} className="text-yellow-200" />
            </div>
            <div>
              <h2 className="text-xl font-bold">프리미엄 업그레이드</h2>
              <p className="text-white/80 text-sm">더 많은 혜택을 누리세요</p>
            </div>
          </div>
        </div>

        {/* 혜택 목록 */}
        <div className="p-6 border-b">
          <h3 className="font-semibold text-gray-900 mb-4">프리미엄 혜택</h3>
          <ul className="space-y-3">
            {PREMIUM_BENEFITS.map((benefit, index) => (
              <li key={index} className="flex items-start gap-3">
                <div className="w-5 h-5 rounded-full bg-green-100 flex items-center justify-center flex-shrink-0 mt-0.5">
                  <Check size={12} className="text-green-600" />
                </div>
                <span className="text-gray-700">{benefit}</span>
              </li>
            ))}
          </ul>
        </div>

        {/* 플랜 선택 */}
        <div className="p-6">
          <h3 className="font-semibold text-gray-900 mb-4">구독 플랜 선택</h3>
          <div className="space-y-3">
            {PAYMENT_TYPES.map((plan) => (
              <button
                key={plan.type}
                onClick={() => setSelectedType(plan.type)}
                className={`w-full p-4 rounded-xl border-2 text-left transition-all relative ${
                  selectedType === plan.type
                    ? "border-primary-500 bg-primary-50"
                    : "border-gray-200 hover:border-gray-300"
                }`}
              >
                {/* 배지 */}
                {plan.badge && (
                  <span className="absolute -top-2 -right-2 bg-red-500 text-white text-xs font-bold px-2 py-0.5 rounded-full">
                    {plan.badge}
                  </span>
                )}

                <div className="flex items-center justify-between">
                  <div>
                    <p className="font-semibold text-gray-900">{plan.name}</p>
                    <p className="text-sm text-gray-500 mt-1">
                      {plan.description}
                    </p>
                  </div>
                  <div className="text-right">
                    {plan.originalPrice && (
                      <p className="text-sm text-gray-400 line-through">
                        {formatPrice(plan.originalPrice)}
                      </p>
                    )}
                    <p className="text-lg font-bold text-gray-900">
                      {formatPrice(plan.price)}
                    </p>
                    {plan.type === "PREMIUM_YEARLY" && (
                      <p className="text-xs text-green-600">
                        월 {formatPrice(Math.round(plan.price / 12))}
                      </p>
                    )}
                  </div>
                </div>

                {/* 선택 인디케이터 */}
                <div
                  className={`absolute top-4 left-4 w-5 h-5 rounded-full border-2 flex items-center justify-center ${
                    selectedType === plan.type
                      ? "border-primary-500 bg-primary-500"
                      : "border-gray-300"
                  }`}
                >
                  {selectedType === plan.type && (
                    <Check size={12} className="text-white" />
                  )}
                </div>
              </button>
            ))}
          </div>
        </div>

        {/* 결제 버튼 */}
        <div className="p-6 bg-gray-50 rounded-b-2xl">
          <button
            onClick={handlePayment}
            disabled={isPending}
            className="w-full py-4 bg-[#FEE500] text-gray-900 font-bold rounded-xl hover:bg-[#FADA0A] disabled:opacity-50 disabled:cursor-not-allowed transition-colors flex items-center justify-center gap-2"
          >
            {isPending ? (
              <>
                <Loader2 size={20} className="animate-spin" />
                결제 준비 중...
              </>
            ) : (
              <>
                <span className="font-bold text-[#3C1E1E]">kakao</span>
                <span className="font-bold text-[#3C1E1E]">pay</span>
                <span className="mx-1">|</span>
                {selectedPlan && formatPrice(selectedPlan.price)} 결제하기
              </>
            )}
          </button>

          <p className="text-xs text-gray-500 text-center mt-3">
            결제 시{" "}
            <a href="/terms" className="text-primary-600 hover:underline">
              이용약관
            </a>{" "}
            및{" "}
            <a href="/privacy" className="text-primary-600 hover:underline">
              개인정보처리방침
            </a>
            에 동의하는 것으로 간주됩니다.
          </p>
        </div>
      </div>
    </div>
  );
}
