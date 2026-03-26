/**
 * ============================================================================
 * 상담 채팅 통합 훅
 * ============================================================================
 *
 * [역할]
 * WebSocket + TanStack Query를 통합하여 상담 채팅의 모든 상태를 관리합니다.
 *
 * @author damoyeo
 * @since 2025-03-16
 */

import { useCallback, useEffect, useRef } from "react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { useSupportStore } from "../stores/support-store";
import { useSupportWebSocket } from "./use-support-websocket";
import * as supportApi from "../api/support-api";
import type { CreateSupportChatRequest } from "../types";

interface UseSupportChatOptions {
  /**
   * 관리자 여부
   */
  isAdmin?: boolean;
}

/**
 * 상담 채팅 통합 훅
 *
 * @param options 옵션
 * @returns 상담 채팅 상태 및 제어 함수
 */
export function useSupportChat({ isAdmin = false }: UseSupportChatOptions = {}) {
  const queryClient = useQueryClient();
  const {
    activeChat,
    messages,
    connectionStatus,
    typingUsers,
    isOpen,
    setActiveChat,
    setMessages,
    prependMessages,
    openChat,
    closeChat,
    reset,
  } = useSupportStore();

  const chatId = activeChat?.id ?? null;
  const typingTimerRef = useRef<ReturnType<typeof setTimeout> | null>(null);

  // WebSocket 연결
  const { sendMessage: wsSendMessage, sendTyping } = useSupportWebSocket({
    chatId,
    isAdmin,
    enabled: isOpen && chatId !== null,
  });

  // ========================================================================
  // Queries
  // ========================================================================

  /**
   * 활성 상담 조회 (사용자용)
   */
  const { data: activeChatData } = useQuery({
    queryKey: ["support", "active"],
    queryFn: () => supportApi.getActiveSupportChat(),
    enabled: !isAdmin,
    staleTime: 30 * 1000,
  });

  // 활성 상담 동기화
  useEffect(() => {
    if (activeChatData !== undefined) {
      setActiveChat(activeChatData);
    }
  }, [activeChatData, setActiveChat]);

  /**
   * 메시지 히스토리 로드
   */
  const { data: messageHistory, isLoading: isLoadingMessages } = useQuery({
    queryKey: ["support", "messages", chatId],
    queryFn: () => supportApi.getSupportMessages(chatId!, 1, 50),
    enabled: chatId !== null && isOpen,
    staleTime: 0,
  });

  // 히스토리 메시지를 스토어에 설정
  useEffect(() => {
    if (messageHistory?.dtoList) {
      // API는 최신순이므로 역순으로 정렬
      const sorted = [...messageHistory.dtoList].reverse();
      setMessages(sorted);
    }
  }, [messageHistory, setMessages]);

  // ========================================================================
  // Mutations
  // ========================================================================

  /**
   * 상담 생성
   */
  const createChatMutation = useMutation({
    mutationFn: (request: CreateSupportChatRequest) =>
      supportApi.createSupportChat(request),
    onSuccess: (newChat) => {
      setActiveChat(newChat);
      queryClient.invalidateQueries({ queryKey: ["support"] });
    },
  });

  /**
   * 상담 평가
   */
  const rateMutation = useMutation({
    mutationFn: ({ rating }: { rating: number }) =>
      supportApi.rateSupportChat(chatId!, rating),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["support"] });
    },
  });

  // ========================================================================
  // 핸들러
  // ========================================================================

  /**
   * 채팅방 열기
   *
   * 활성 상담이 없으면 새 상담 생성 모달 표시
   */
  const handleOpenChat = useCallback(() => {
    openChat();
  }, [openChat]);

  /**
   * 채팅방 닫기
   */
  const handleCloseChat = useCallback(() => {
    closeChat();
  }, [closeChat]);

  /**
   * 새 상담 시작
   */
  const startNewChat = useCallback(
    async (title: string, message: string) => {
      try {
        await createChatMutation.mutateAsync({ title, message });
        openChat();
      } catch (error) {
        console.error("상담 생성 실패:", error);
        throw error;
      }
    },
    [createChatMutation, openChat]
  );

  /**
   * 메시지 전송
   */
  const sendMessage = useCallback(
    (message: string) => {
      if (!message.trim() || !chatId) return;
      wsSendMessage(message);
    },
    [chatId, wsSendMessage]
  );

  /**
   * 타이핑 이벤트 (디바운싱 처리)
   */
  const handleTyping = useCallback(() => {
    sendTyping(true);

    if (typingTimerRef.current) {
      clearTimeout(typingTimerRef.current);
    }

    typingTimerRef.current = setTimeout(() => {
      sendTyping(false);
    }, 3000);
  }, [sendTyping]);

  /**
   * 상담 평가
   */
  const rateChat = useCallback(
    (rating: number) => {
      rateMutation.mutate({ rating });
    },
    [rateMutation]
  );

  return {
    // 상태
    activeChat,
    messages,
    connectionStatus,
    typingUsers,
    isOpen,
    isLoadingMessages,
    isCreating: createChatMutation.isPending,

    // 핸들러
    openChat: handleOpenChat,
    closeChat: handleCloseChat,
    startNewChat,
    sendMessage,
    handleTyping,
    rateChat,
    reset,
  };
}
