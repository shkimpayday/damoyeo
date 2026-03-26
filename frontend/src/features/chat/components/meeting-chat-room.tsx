/**
 * ============================================================================
 * 정모 채팅방 메인 컴포넌트
 * ============================================================================
 *
 * [역할]
 * 정모 참석자 전용 채팅방 UI를 제공합니다.
 *
 * [권한]
 * ATTENDING 상태의 참석자만 채팅에 참여할 수 있습니다.
 *
 * [사용 위치]
 * - MeetingChatPage (/meetings/:meetingId/chat)
 *
 * @author damoyeo
 * @since 2025-03-16
 */

import { ChatHeader } from "./chat-header";
import { MessageList } from "./message-list";
import { MessageInput } from "./message-input";
import { TypingIndicator } from "./typing-indicator";
import { Spinner } from "@/components/ui/spinner";
import { useMeetingChatRoom } from "../hooks/use-meeting-chat-room";

interface MeetingChatRoomProps {
  meetingId: number;
  meetingTitle: string;
  onBack?: () => void;
}

/**
 * 정모 채팅방 메인 컴포넌트
 */
export function MeetingChatRoom({ meetingId, meetingTitle, onBack }: MeetingChatRoomProps) {
  const {
    messages,
    typingUsers,
    isLoading,
    error,
    isConnected,
    connectionStatus,
    sendMessage,
    sendTyping,
    fetchPreviousMessages,
    hasPreviousMessages,
    isFetchingPrevious,
  } = useMeetingChatRoom(meetingId);

  // 로딩 상태
  if (isLoading) {
    return (
      <div className="flex h-full items-center justify-center">
        <Spinner size="lg" />
      </div>
    );
  }

  // 에러 상태
  if (error) {
    return (
      <div className="flex h-full flex-col items-center justify-center gap-4 px-4">
        <div className="text-center">
          <p className="text-lg font-semibold text-red-600">
            채팅방을 불러오는 중 오류가 발생했습니다
          </p>
          <p className="mt-2 text-sm text-gray-600">
            {error instanceof Error ? error.message : "정모 참석자만 채팅에 참여할 수 있습니다"}
          </p>
        </div>

        {onBack && (
          <button
            onClick={onBack}
            className="rounded-lg bg-gray-200 px-4 py-2 text-gray-700 hover:bg-gray-300"
          >
            뒤로 가기
          </button>
        )}
      </div>
    );
  }

  // 정상 상태: 채팅 UI 렌더링
  return (
    <div className="flex h-full flex-col">
      {/* 헤더 */}
      <ChatHeader
        groupName={meetingTitle}
        connectionStatus={connectionStatus}
        onBack={onBack}
      />

      {/* 메시지 목록 */}
      <MessageList
        messages={messages}
        onLoadPrevious={fetchPreviousMessages}
        hasPrevious={hasPreviousMessages}
        isLoadingPrevious={isFetchingPrevious}
      />

      {/* 타이핑 인디케이터 */}
      <TypingIndicator typingUsers={typingUsers} />

      {/* 입력창 */}
      <MessageInput
        onSendMessage={sendMessage}
        onTyping={sendTyping}
        disabled={!isConnected}
      />
    </div>
  );
}
