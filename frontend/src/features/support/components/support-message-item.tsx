/**
 * ============================================================================
 * 상담 메시지 아이템 컴포넌트
 * ============================================================================
 *
 * [역할]
 * 개별 상담 메시지를 표시합니다.
 * 사용자/관리자 메시지를 좌/우로 구분합니다.
 *
 * @author damoyeo
 * @since 2025-03-16
 */

import { Avatar } from "@/components/ui/avatar";
import type { SupportMessageDTO } from "../types";
import { getRelativeTime } from "@/utils/date";

interface SupportMessageItemProps {
  /**
   * 메시지 데이터
   */
  message: SupportMessageDTO;

  /**
   * 현재 사용자가 이 메시지를 보낸 사람인지 여부
   */
  isMine: boolean;
}

/**
 * 상담 메시지 아이템
 */
export function SupportMessageItem({ message, isMine }: SupportMessageItemProps) {
  return (
    <div
      className={`flex gap-2 ${isMine ? "flex-row-reverse" : "flex-row"} items-end mb-3`}
    >
      {/* 아바타 (상대방만 표시) */}
      {!isMine && (
        <Avatar
          src={message.sender?.profileImage}
          alt={message.sender?.nickname ?? (message.isAdmin ? "상담사" : "익명")}
          size="sm"
        />
      )}

      {/* 메시지 버블 */}
      <div className={`flex flex-col gap-1 max-w-[70%] ${isMine ? "items-end" : "items-start"}`}>
        {/* 발신자 이름 (상대방만) */}
        {!isMine && (
          <span className="text-xs text-gray-500 px-1">
            {message.isAdmin ? `🛡️ ${message.sender?.nickname ?? "상담사"}` : message.sender?.nickname}
          </span>
        )}

        {/* 메시지 내용 */}
        <div
          className={`
            px-3 py-2 rounded-2xl text-sm leading-relaxed break-words
            ${isMine
              ? "bg-primary-500 text-white rounded-br-sm"
              : "bg-white border border-gray-200 text-gray-800 rounded-bl-sm shadow-sm"
            }
          `}
        >
          {message.message}
        </div>

        {/* 시간 */}
        <span className="text-xs text-gray-400 px-1">
          {getRelativeTime(message.createdAt)}
        </span>
      </div>
    </div>
  );
}
