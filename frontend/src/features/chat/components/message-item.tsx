/**
 * ============================================================================
 * 메시지 아이템 컴포넌트
 * ============================================================================
 *
 * [역할]
 * 개별 채팅 메시지를 버블 형태로 렌더링합니다.
 *
 * [UI]
 * - 내 메시지: 오른쪽 정렬, 파란색 배경
 * - 상대 메시지: 왼쪽 정렬, 회색 배경, 프로필 이미지 포함
 * - SYSTEM 메시지: 중앙 정렬, 회색 텍스트
 *
 * @author damoyeo
 * @since 2025-02-25
 */

import { useAuthStore } from "@/features/auth/stores/auth-store";
import { Avatar } from "@/components/ui/avatar";
import type { ChatMessageDTO } from "../types";

interface MessageItemProps {
  message: ChatMessageDTO;
}

/**
 * 시간 포맷 (상대 시간)
 */
function formatTime(dateString: string): string {
  const date = new Date(dateString);
  const now = new Date();
  const diffMs = now.getTime() - date.getTime();
  const diffMins = Math.floor(diffMs / 60000);

  if (diffMins < 1) return "방금 전";
  if (diffMins < 60) return `${diffMins}분 전`;

  const diffHours = Math.floor(diffMins / 60);
  if (diffHours < 24) return `${diffHours}시간 전`;

  const diffDays = Math.floor(diffHours / 24);
  if (diffDays < 7) return `${diffDays}일 전`;

  // 7일 이상은 날짜 표시
  return date.toLocaleDateString("ko-KR", {
    month: "short",
    day: "numeric",
  });
}

export function MessageItem({ message }: MessageItemProps) {
  const { member } = useAuthStore();

  // SYSTEM 메시지 (중앙 정렬)
  if (message.messageType === "SYSTEM") {
    return (
      <div className="flex justify-center my-4">
        <div className="px-4 py-2 bg-gray-100 rounded-full text-sm text-gray-600">
          {message.message}
        </div>
      </div>
    );
  }

  // 내 메시지 여부 확인
  const isMyMessage = message.sender?.id === member?.id;

  // 내 메시지 (오른쪽 정렬)
  if (isMyMessage) {
    return (
      <div className="flex justify-end mb-4">
        <div className="flex flex-col items-end max-w-[70%]">
          <div className="px-4 py-2 bg-blue-500 text-white rounded-2xl rounded-tr-none break-words">
            {message.message}
          </div>
          <span className="text-xs text-gray-500 mt-1">
            {formatTime(message.createdAt)}
          </span>
        </div>
      </div>
    );
  }

  // 상대 메시지 (왼쪽 정렬 + 프로필 이미지)
  return (
    <div className="flex items-start mb-4 gap-2">
      {/* 프로필 이미지 */}
      <Avatar
        src={message.sender?.profileImage}
        alt={message.sender?.nickname || "User"}
        size="sm"
      />

      {/* 메시지 내용 */}
      <div className="flex flex-col max-w-[70%]">
        {/* 닉네임 */}
        <span className="text-xs text-gray-600 mb-1 px-1">
          {message.sender?.nickname}
        </span>

        {/* 메시지 버블 */}
        <div className="px-4 py-2 bg-gray-100 rounded-2xl rounded-tl-none break-words">
          {message.message}
        </div>

        {/* 시간 */}
        <span className="text-xs text-gray-500 mt-1 px-1">
          {formatTime(message.createdAt)}
        </span>
      </div>
    </div>
  );
}
