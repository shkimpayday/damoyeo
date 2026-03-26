/**
 * ============================================================================
 * 프리미엄 상태 카드 컴포넌트
 * ============================================================================
 *
 * [역할]
 * 마이페이지에서 프리미엄 구독 상태를 표시합니다.
 * - 프리미엄 회원: 구독 정보 및 남은 기간 표시
 * - 일반 회원: 프리미엄 업그레이드 유도
 */

import { useState } from "react";
import { Crown, Calendar, ChevronRight, Sparkles } from "lucide-react";
import { usePremiumStatus } from "../hooks/use-payment";
import { PremiumUpgradeModal } from "./premium-upgrade-modal";

export function PremiumStatusCard() {
  const { data: status, isLoading } = usePremiumStatus();
  const [showUpgradeModal, setShowUpgradeModal] = useState(false);

  if (isLoading) {
    return (
      <div className="bg-white rounded-xl p-4 border border-gray-100 animate-pulse">
        <div className="h-6 bg-gray-200 rounded w-1/3 mb-2" />
        <div className="h-4 bg-gray-200 rounded w-2/3" />
      </div>
    );
  }

  // 프리미엄 회원
  if (status?.isPremium) {
    return (
      <div className="bg-gradient-to-r from-amber-50 to-orange-50 rounded-xl p-4 border border-amber-200">
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 bg-gradient-to-br from-amber-400 to-orange-500 rounded-full flex items-center justify-center">
              <Crown size={20} className="text-white" />
            </div>
            <div>
              <div className="flex items-center gap-2">
                <h3 className="font-semibold text-gray-900">프리미엄 회원</h3>
                <span className="px-2 py-0.5 bg-amber-500 text-white text-xs font-bold rounded-full">
                  ACTIVE
                </span>
              </div>
              <p className="text-sm text-gray-600">
                {status.premiumTypeName}
              </p>
            </div>
          </div>
        </div>

        {/* 구독 정보 */}
        <div className="mt-4 p-3 bg-white/60 rounded-lg">
          <div className="flex items-center gap-2 text-sm text-gray-600">
            <Calendar size={14} />
            <span>
              {status.endDate &&
                new Date(status.endDate).toLocaleDateString("ko-KR")}
              까지 ({status.daysRemaining}일 남음)
            </span>
          </div>
        </div>

        {/* 갱신 버튼 */}
        {status.daysRemaining <= 7 && (
          <button
            onClick={() => setShowUpgradeModal(true)}
            className="mt-3 w-full py-2 bg-amber-500 text-white text-sm font-semibold rounded-lg hover:bg-amber-600 transition-colors"
          >
            구독 갱신하기
          </button>
        )}

        <PremiumUpgradeModal
          isOpen={showUpgradeModal}
          onClose={() => setShowUpgradeModal(false)}
        />
      </div>
    );
  }

  // 일반 회원
  return (
    <>
      <button
        onClick={() => setShowUpgradeModal(true)}
        className="w-full bg-gradient-to-r from-gray-50 to-gray-100 rounded-xl p-4 border border-gray-200 hover:border-amber-300 hover:from-amber-50 hover:to-orange-50 transition-all group"
      >
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 bg-gray-200 rounded-full flex items-center justify-center group-hover:bg-gradient-to-br group-hover:from-amber-400 group-hover:to-orange-500 transition-colors">
              <Sparkles
                size={20}
                className="text-gray-400 group-hover:text-white transition-colors"
              />
            </div>
            <div className="text-left">
              <h3 className="font-semibold text-gray-900">일반 회원</h3>
              <p className="text-sm text-gray-500">
                프리미엄으로 더 많은 혜택을 누리세요
              </p>
            </div>
          </div>
          <ChevronRight
            size={20}
            className="text-gray-400 group-hover:text-amber-500 transition-colors"
          />
        </div>

        {/* 혜택 미리보기 */}
        <div className="mt-3 flex gap-2">
          <span className="px-2 py-1 bg-white text-xs text-gray-600 rounded-full border">
            무제한 모임 생성
          </span>
          <span className="px-2 py-1 bg-white text-xs text-gray-600 rounded-full border">
            무제한 인원
          </span>
          <span className="px-2 py-1 bg-white text-xs text-gray-600 rounded-full border">
            광고 제거
          </span>
        </div>
      </button>

      <PremiumUpgradeModal
        isOpen={showUpgradeModal}
        onClose={() => setShowUpgradeModal(false)}
      />
    </>
  );
}
