/**
 * ============================================================================
 * 채팅방 Combined Hook
 * ============================================================================
 *
 * [역할]
 * WebSocket + TanStack Query를 통합하여 채팅방 기능을 제공합니다.
 *
 * [기능]
 * - 초기 메시지 히스토리 로드 (TanStack Query)
 * - 무한 스크롤로 이전 메시지 로드
 * - 실시간 메시지 수신 (WebSocket)
 * - 메시지 전송 (WebSocket)
 * - 연결 상태 관리
 *
 * [사용 위치]
 * - components/chat-room.tsx
 *
 * [사용 예시]
 * ```typescript
 * const {
 *   messages,
 *   isConnected,
 *   isLoading,
 *   sendMessage,
 *   fetchPreviousMessages,
 *   hasPreviousMessages
 * } = useChatRoom(groupId);
 * ```
 *
 * @author damoyeo
 * @since 2025-02-25
 */

import { useEffect, useRef, useCallback } from "react";
import { useWebSocket } from "./use-websocket";
import { useChatMessagesInfinite } from "./use-chat-messages";
import { useChatStore } from "../stores/chat-store";

/**
 * 채팅방 Combined Hook
 *
 * [처리 흐름]
 * 1. TanStack Query로 메시지 히스토리 로드 (무한 스크롤)
 * 2. 첫 페이지는 setMessages, 추가 페이지는 prependMessages
 * 3. WebSocket 연결 및 구독
 * 4. 새 메시지 수신 시 store에 추가
 *
 * @param groupId 모임 ID
 * @returns 채팅방 상태 및 액션
 */
export function useChatRoom(groupId: number) {
  // 로드된 페이지 수 추적 (새 페이지 감지용)
  const loadedPagesRef = useRef(0);

  // 1. TanStack Query: 메시지 히스토리 조회 (무한 스크롤)
  const {
    data,
    isLoading,
    error,
    fetchNextPage,
    hasNextPage,
    isFetchingNextPage,
  } = useChatMessagesInfinite(groupId);

  // 2. WebSocket: 실시간 연결 및 메시지 송수신
  const { isConnected, sendMessage, sendTyping } = useWebSocket(groupId);

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

    // 첫 페이지 로드 (초기 로드)
    if (loadedPagesRef.current === 0 && currentPageCount >= 1) {
      const firstPageMessages = data.pages[0]?.dtoList || [];
      // 백엔드는 최신순(내림차순)으로 반환하므로 reverse 필요
      setMessages([...firstPageMessages].reverse());
      loadedPagesRef.current = 1;
    }
    // 추가 페이지 로드 (무한 스크롤)
    else if (currentPageCount > loadedPagesRef.current) {
      // 새로 로드된 페이지들만 처리
      for (let i = loadedPagesRef.current; i < currentPageCount; i++) {
        const newPageMessages = data.pages[i]?.dtoList || [];
        // 이전 메시지를 앞에 추가 (reverse해서 오래된 순으로)
        prependMessages([...newPageMessages].reverse());
      }
      loadedPagesRef.current = currentPageCount;
    }
  }, [data, setMessages, prependMessages]);

  // 5. 채팅방 전환 시 초기화
  useEffect(() => {
    // groupId 변경 시 페이지 카운트 리셋
    loadedPagesRef.current = 0;

    return () => {
      clearMessages();
      clearTypingUsers();
      loadedPagesRef.current = 0;
    };
  }, [groupId, clearMessages, clearTypingUsers]);

  /**
   * 이전 메시지 로드 (무한 스크롤용)
   *
   * [사용 시점]
   * 스크롤이 상단에 도달했을 때 호출
   */
  const fetchPreviousMessages = useCallback(() => {
    if (hasNextPage && !isFetchingNextPage) {
      fetchNextPage();
    }
  }, [fetchNextPage, hasNextPage, isFetchingNextPage]);

  return {
    // 메시지
    messages,
    typingUsers,

    // 로딩 상태
    isLoading,
    error,
    isFetchingPrevious: isFetchingNextPage,

    // 무한 스크롤
    fetchPreviousMessages,
    hasPreviousMessages: hasNextPage ?? false,

    // 연결 상태
    isConnected,
    connectionStatus,

    // 액션
    sendMessage,
    sendTyping,
  };
}
