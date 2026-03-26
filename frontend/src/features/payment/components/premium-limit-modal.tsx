/**
 * ============================================================================
 * 프리미엄 업그레이드 안내 모달
 * ============================================================================
 *
 * [역할]
 * 일반 회원이 프리미엄 기능(모임 무제한, 인원 무제한)을 사용하려 할 때
 * 프리미엄 업그레이드를 친절하게 안내하는 모달입니다.
 *
 * [UX 원칙]
 * - "제한"이 아닌 "업그레이드 기회"로 안내
 * - 긍정적인 메시지와 혜택 강조
 * - 자연스러운 결제 플로우 유도
 */

import { useState } from "react";
import { Crown, X, Sparkles, Users, Infinity } from "lucide-react";
import { PremiumUpgradeModal } from "./premium-upgrade-modal";

type LimitType = "group" | "member";

interface PremiumLimitModalProps {
  isOpen: boolean;
  onClose: () => void;
  limitType: LimitType;
}

const LIMIT_MESSAGES: Record<
  LimitType,
  { title: string; subtitle: string; description: string; icon: React.ReactNode }
> = {
  group: {
    title: "더 많은 모임을 만들고 싶으신가요?",
    subtitle: "프리미엄으로 무제한 모임 생성",
    description:
      "프리미엄 회원이 되시면 원하는 만큼\n모임을 만들고 운영할 수 있어요!",
    icon: <Sparkles size={28} />,
  },
  member: {
    title: "더 많은 멤버와 함께하세요!",
    subtitle: "프리미엄으로 인원 제한 해제",
    description:
      "30명 이상의 대규모 모임을 운영하고 싶으시다면\n프리미엄으로 업그레이드해 보세요!",
    icon: <Users size={28} />,
  },
};

export function PremiumLimitModal({
  isOpen,
  onClose,
  limitType,
}: PremiumLimitModalProps) {
  const [showUpgradeModal, setShowUpgradeModal] = useState(false);

  const message = LIMIT_MESSAGES[limitType];

  /**
   * 프리미엄 시작하기 버튼 클릭 핸들러
   * - 안내 모달을 닫고 결제 모달을 엽니다
   */
  const handleUpgradeClick = () => {
    setShowUpgradeModal(true);
  };

  /**
   * 결제 모달 닫기 핸들러
   * - 결제 모달과 안내 모달 모두 닫습니다
   */
  const handleUpgradeModalClose = () => {
    setShowUpgradeModal(false);
    onClose();
  };

  // 두 모달 모두 닫혀있으면 렌더링하지 않음
  if (!isOpen && !showUpgradeModal) return null;

  return (
    <>
      {/* 안내 모달 - 결제 모달이 열리면 숨김 */}
      {isOpen && !showUpgradeModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center">
          {/* 배경 오버레이 */}
          <div className="absolute inset-0 bg-black/50" onClick={onClose} />

          {/* 모달 컨텐츠 */}
          <div className="relative bg-white rounded-2xl w-full max-w-md mx-4 overflow-hidden">
            {/* 닫기 버튼 */}
            <button
              onClick={onClose}
              className="absolute top-4 right-4 p-1 text-gray-400 hover:text-gray-600 transition-colors z-10"
            >
              <X size={24} />
            </button>

            {/* 헤더 영역 - 밝고 긍정적인 디자인 */}
            <div className="bg-gradient-to-br from-amber-400 via-orange-400 to-orange-500 pt-10 pb-8 px-6 text-center">
              <div className="w-20 h-20 bg-white rounded-full flex items-center justify-center mx-auto mb-4 shadow-lg">
                <div className="text-amber-500">{message.icon}</div>
              </div>
              <h2 className="text-xl font-bold text-white mb-2">{message.title}</h2>
              <p className="text-white/90 text-sm flex items-center justify-center gap-1.5">
                <Crown size={14} />
                {message.subtitle}
              </p>
            </div>

            {/* 내용 - 긍정적인 메시지 */}
            <div className="p-6">
              <p className="text-gray-700 text-center whitespace-pre-line leading-relaxed text-[15px]">
                {message.description}
              </p>

              {/* 프리미엄 혜택 카드 */}
              <div className="mt-6 p-4 bg-gradient-to-r from-amber-50 to-orange-50 rounded-xl border border-amber-100">
                <div className="flex items-center gap-2 mb-3">
                  <Infinity size={16} className="text-amber-600" />
                  <p className="text-sm font-semibold text-amber-900">
                    프리미엄 특별 혜택
                  </p>
                </div>
                <ul className="text-sm text-amber-800 space-y-2">
                  <li className="flex items-center gap-2">
                    <span className="w-5 h-5 bg-amber-500 text-white rounded-full flex items-center justify-center text-xs">✓</span>
                    무제한 모임 생성
                  </li>
                  <li className="flex items-center gap-2">
                    <span className="w-5 h-5 bg-amber-500 text-white rounded-full flex items-center justify-center text-xs">✓</span>
                    무제한 모임 인원
                  </li>
                  <li className="flex items-center gap-2">
                    <span className="w-5 h-5 bg-amber-500 text-white rounded-full flex items-center justify-center text-xs">✓</span>
                    프리미엄 뱃지 & 광고 제거
                  </li>
                </ul>
              </div>

              {/* 가격 안내 */}
              <div className="mt-4 text-center">
                <p className="text-xs text-gray-500">
                  월 <span className="font-semibold text-amber-600">3,900원</span>부터 시작
                </p>
              </div>
            </div>

            {/* 버튼 영역 - 더 눈에 띄는 CTA */}
            <div className="p-6 pt-0 space-y-3">
              <button
                onClick={handleUpgradeClick}
                className="w-full py-4 bg-gradient-to-r from-amber-500 to-orange-500 text-white font-bold rounded-xl hover:from-amber-600 hover:to-orange-600 transition-all shadow-lg shadow-amber-500/30 flex items-center justify-center gap-2 text-lg"
              >
                <Crown size={20} />
                프리미엄 시작하기
              </button>
              <button
                onClick={onClose}
                className="w-full py-2.5 text-gray-400 hover:text-gray-600 text-sm transition-colors"
              >
                다음에 할게요
              </button>
            </div>
          </div>
        </div>
      )}

      {/* 프리미엄 결제 모달 */}
      <PremiumUpgradeModal
        isOpen={showUpgradeModal}
        onClose={handleUpgradeModalClose}
      />
    </>
  );
}
