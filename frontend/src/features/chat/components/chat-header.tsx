/**
 * ============================================================================
 * 채팅방 헤더 컴포넌트
 * ============================================================================
 *
 * [역할]
 * 채팅방 상단 헤더 UI를 제공합니다.
 *
 * [기능]
 * - 모임명 표시
 * - 연결 상태 표시 (🟢 연결됨, 🔴 연결 안 됨)
 * - 뒤로 가기 버튼 (선택)
 *
 * @author damoyeo
 * @since 2025-02-25
 */

import type { ConnectionStatus } from "../types";

interface ChatHeaderProps {
  groupName: string;
  connectionStatus: ConnectionStatus;
  onBack?: () => void;
}

/**
 * 연결 상태 표시 텍스트 및 색상 매핑
 */
const CONNECTION_STATUS_MAP = {
  connecting: {
    label: "연결 중...",
    icon: "🟡",
    color: "text-yellow-500",
  },
  connected: {
    label: "연결됨",
    icon: "🟢",
    color: "text-green-500",
  },
  disconnected: {
    label: "연결 안 됨",
    icon: "🔴",
    color: "text-red-500",
  },
  error: {
    label: "오류",
    icon: "🔴",
    color: "text-red-500",
  },
} as const;

export function ChatHeader({ groupName, connectionStatus, onBack }: ChatHeaderProps) {
  const status = CONNECTION_STATUS_MAP[connectionStatus];

  return (
    <header className="flex items-center justify-between border-b border-gray-200 bg-white px-4 py-3">
      {/* 왼쪽: 뒤로 가기 버튼 (선택) */}
      <div className="flex items-center gap-3">
        {onBack && (
          <button
            onClick={onBack}
            className="text-gray-600 hover:text-gray-900 transition-colors"
            aria-label="뒤로 가기"
          >
            <svg
              className="h-6 w-6"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={2}
                d="M15 19l-7-7 7-7"
              />
            </svg>
          </button>
        )}

        {/* 모임명 */}
        <h1 className="text-lg font-bold text-gray-900">{groupName}</h1>
      </div>

      {/* 오른쪽: 연결 상태 */}
      <div className={`flex items-center gap-1 text-sm ${status.color}`}>
        <span>{status.icon}</span>
        <span className="font-medium">{status.label}</span>
      </div>
    </header>
  );
}
