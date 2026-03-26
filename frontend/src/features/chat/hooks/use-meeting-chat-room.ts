/**
 * ============================================================================
 * 정모 채팅방 Combined Hook
 * ============================================================================
 *
 * [역할]
 * WebSocket + TanStack Query를 통합하여 정모 채팅방 기능을 제공합니다.
 *
 * [권한]
 * ATTENDING 상태의 참석자만 채팅에 참여할 수 있습니다.
 *
 * @author damoyeo
 * @since 2025-03-16
 */

import { useEffect, useRef, useCallback } from "react";
import { useMeetingWebSocket } from "./use-meeting-websocket";
import { useMeetingChatMessagesInfinite } from "./use-meeting-chat-messages";
import { useChatStore } from "../stores/chat-store";

/**
 * 정모 채팅방 Combined Hook
 *
 * @param meetingId 정모 ID
 * @returns 채팅방 상태 및 액션
 */
export function useMeetingChatRoom(meetingId: number) {
  const loadedPagesRef = useRef(0);

  // 1. TanStack Query: 메시지 히스토리 조회
  const {
    data,
    isLoading,
    error,
    fetchNextPage,
    hasNextPage,
    isFetchingNextPage,
  } = useMeetingChatMessagesInfinite(meetingId);

  // 2. WebSocket: 실시간 연결
  const { isConnected, sendMessage, sendTyping } = useMeetingWebSocket(meetingId);

  // 3. Zustand: 메시지 상태 관리
  const {
    messages,
    setMessages,
    prependMessages,
    clearMessages,
    connectionStatus,
    typingUsers,
    clearTypingUsers,
  } = useChatStore();

  // 4. 메시지 로드 처리
  useEffect(() => {
    if (!data?.pages) return;

    const currentPageCount = data.pages.length;

    // 첫 페이지 로드
    if (loadedPagesRef.current === 0 && currentPageCount >= 1) {
      const firstPageMessages = data.pages[0]?.dtoList || [];
      setMessages([...firstPageMessages].reverse());
      loadedPagesRef.current = 1;
    }
    // 추가 페이지 로드
    else if (currentPageCount > loadedPagesRef.current) {
      for (let i = loadedPagesRef.current; i < currentPageCount; i++) {
        const newPageMessages = data.pages[i]?.dtoList || [];
        prependMessages([...newPageMessages].reverse());
      }
      loadedPagesRef.current = currentPageCount;
    }
  }, [data, setMessages, prependMessages]);

  // 5. 채팅방 전환 시 초기화
  useEffect(() => {
    loadedPagesRef.current = 0;

    return () => {
      clearMessages();
      clearTypingUsers();
      loadedPagesRef.current = 0;
    };
  }, [meetingId, clearMessages, clearTypingUsers]);

  /**
   * 이전 메시지 로드
   */
  const fetchPreviousMessages = useCallback(() => {
    if (hasNextPage && !isFetchingNextPage) {
      fetchNextPage();
    }
  }, [fetchNextPage, hasNextPage, isFetchingNextPage]);

  return {
    messages,
    typingUsers,
    isLoading,
    error,
    isFetchingPrevious: isFetchingNextPage,
    fetchPreviousMessages,
    hasPreviousMessages: hasNextPage ?? false,
    isConnected,
    connectionStatus,
    sendMessage,
    sendTyping,
  };
}
