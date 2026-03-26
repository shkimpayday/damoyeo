/**
 * ============================================================================
 * 타이핑 인디케이터 컴포넌트
 * ============================================================================
 *
 * [역할]
 * 다른 사용자가 메시지를 입력 중일 때 "○○○님이 입력 중..." 표시
 *
 * [기능]
 * - 최대 3명까지 닉네임 표시, 그 이상은 "외 N명"
 * - 본인 이메일은 필터링 (자기 자신의 타이핑은 표시 안 함)
 * - 타이핑 중인 사람이 없으면 렌더링 안 함
 * - 애니메이션으로 시각적 효과 (점 3개 깜빡임)
 *
 * @author damoyeo
 * @since 2025-02-25
 */

import { useMemo } from "react";
import { useAuthStore } from "@/features/auth/stores/auth-store";

interface TypingIndicatorProps {
  typingUsers: Set<string>;  // 타이핑 중인 사용자 이메일 Set
}

export function TypingIndicator({ typingUsers }: TypingIndicatorProps) {
  const { member } = useAuthStore();

  // 본인 제외한 타이핑 중인 사용자 목록
  const otherTypingUsers = useMemo(() => {
    if (!member?.email) return [];

    return Array.from(typingUsers).filter((email) => email !== member.email);
  }, [typingUsers, member?.email]);

  // 타이핑 중인 사람이 없으면 렌더링 안 함
  if (otherTypingUsers.length === 0) {
    return null;
  }

  // 닉네임 표시 로직
  const getDisplayText = () => {
    const count = otherTypingUsers.length;

    if (count === 1) {
      // 1명: "○○○님이 입력 중..."
      return `${getNickname(otherTypingUsers[0])}님이 입력 중`;
    } else if (count === 2) {
      // 2명: "○○○님, △△△님이 입력 중..."
      return `${getNickname(otherTypingUsers[0])}님, ${getNickname(otherTypingUsers[1])}님이 입력 중`;
    } else if (count === 3) {
      // 3명: "○○○님, △△△님, □□□님이 입력 중..."
      return `${getNickname(otherTypingUsers[0])}님, ${getNickname(otherTypingUsers[1])}님, ${getNickname(otherTypingUsers[2])}님이 입력 중`;
    } else {
      // 4명 이상: "○○○님 외 N명이 입력 중..."
      return `${getNickname(otherTypingUsers[0])}님 외 ${count - 1}명이 입력 중`;
    }
  };

  const getNickname = (email: string): string => {
    return email.split("@")[0];
  };

  return (
    <div className="px-4 py-2 bg-gray-50 border-t border-gray-200">
      <div className="flex items-center gap-2 text-sm text-gray-600">
        {/* 텍스트 */}
        <span>{getDisplayText()}</span>

        {/* 애니메이션 점 3개 */}
        <div className="flex gap-1">
          <span className="animate-bounce" style={{ animationDelay: "0ms" }}>
            .
          </span>
          <span className="animate-bounce" style={{ animationDelay: "150ms" }}>
            .
          </span>
          <span className="animate-bounce" style={{ animationDelay: "300ms" }}>
            .
          </span>
        </div>
      </div>
    </div>
  );
}
