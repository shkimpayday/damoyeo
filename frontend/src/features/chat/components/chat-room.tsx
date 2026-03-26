/**
 * ============================================================================
 * 채팅방 메인 컨테이너 컴포넌트
 * ============================================================================
 *
 * [역할]
 * 채팅방의 모든 UI 컴포넌트를 통합하여 제공합니다.
 *
 * [기능]
 * - 메시지 히스토리 로드 (TanStack Query)
 * - 실시간 메시지 수신 (WebSocket)
 * - 메시지 전송
 * - 연결 상태 표시
 *
 * [사용 위치]
 * - GroupDetailPage의 "채팅" 탭
 *
 * [사용 예시]
 * ```tsx
 * <ChatRoom groupId={1} groupName="강남 러닝 크루" />
 * ```
 *
 * @author damoyeo
 * @since 2025-02-25
 */

import { useEffect } from "react";
import { ChatHeader } from "./chat-header";
import { MessageList } from "./message-list";
import { MessageInput } from "./message-input";
import { TypingIndicator } from "./typing-indicator";
import { Spinner } from "@/components/ui/spinner";
import { useChatRoom } from "../hooks/use-chat-room";
import { useChatStore } from "../stores/chat-store";

interface ChatRoomProps {
  groupId: number;
  groupName: string;
  onBack?: () => void;
}

/**
 * 채팅방 메인 컴포넌트
 *
 * [처리 흐름]
 * 1. useChatRoom hook으로 초기 메시지 로드 + WebSocket 연결
 * 2. 로딩 중: 스피너 표시
 * 3. 에러 발생: 에러 메시지 표시
 * 4. 정상: 채팅 UI 렌더링 (헤더 + 메시지 목록 + 입력창)
 */
export function ChatRoom({ groupId, groupName, onBack }: ChatRoomProps) {
  // 1. 채팅방 상태 및 액션 로드
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
  } = useChatRoom(groupId);

  // WebSocket 에러 메시지 (alert 대신 UI에 표시)
  const { errorMessage, setErrorMessage } = useChatStore();

  // 에러 메시지 3초 후 자동 클리어
  useEffect(() => {
    if (!errorMessage) return;
    const timer = setTimeout(() => setErrorMessage(null), 3000);
    return () => clearTimeout(timer);
  }, [errorMessage, setErrorMessage]);

  // 2. 로딩 상태
  if (isLoading) {
    return (
      <div className="flex h-full items-center justify-center">
        <Spinner size="lg" />
      </div>
    );
  }

  // 3. 에러 상태
  if (error) {
    return (
      <div className="flex h-full flex-col items-center justify-center gap-4 px-4">
        <div className="text-center">
          <p className="text-lg font-semibold text-red-600">
            채팅방을 불러오는 중 오류가 발생했습니다
          </p>
          <p className="mt-2 text-sm text-gray-600">
            {error instanceof Error ? error.message : "알 수 없는 오류"}
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

  // 4. 정상 상태: 채팅 UI 렌더링
  return (
    <div className="flex h-full flex-col">
      {/* 헤더 */}
      <ChatHeader
        groupName={groupName}
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

      {/* WebSocket 에러 토스트 */}
      {errorMessage && (
        <div className="mx-4 mb-2 rounded-lg bg-red-50 px-4 py-2 text-sm text-red-600 flex items-center justify-between">
          <span>{errorMessage}</span>
          <button
            onClick={() => setErrorMessage(null)}
            className="ml-2 text-red-400 hover:text-red-600"
            aria-label="닫기"
          >
            ✕
          </button>
        </div>
      )}

      {/* 입력창 */}
      <MessageInput
        onSendMessage={sendMessage}
        onTyping={sendTyping}
        disabled={!isConnected}
      />
    </div>
  );
}
