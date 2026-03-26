/**
 * ============================================================================
 * 상담 WebSocket 연결 훅
 * ============================================================================
 *
 * [역할]
 * 상담 채팅의 WebSocket 연결을 관리합니다.
 *
 * @author damoyeo
 * @since 2025-03-16
 */

import { useCallback, useEffect, useRef } from "react";
import { Client, type IMessage } from "@stomp/stompjs";
import SockJS from "sockjs-client";
import { getCookie } from "@/lib/cookie";
import { useSupportStore } from "../stores/support-store";
import type {
  SupportMessageDTO,
  SupportTypingEvent,
  SupportEvent,
} from "../types";
import { ENV } from "@/config/env";

interface UseSupportWebSocketOptions {
  /**
   * 상담 채팅 ID
   */
  chatId: number | null;

  /**
   * 관리자 여부
   */
  isAdmin?: boolean;

  /**
   * 연결 활성화 여부
   */
  enabled?: boolean;
}

interface UseSupportWebSocketReturn {
  /**
   * 메시지 전송
   */
  sendMessage: (message: string) => void;

  /**
   * 타이핑 이벤트 전송
   */
  sendTyping: (typing: boolean) => void;

  /**
   * 연결 상태
   */
  isConnected: boolean;
}

/**
 * 상담 WebSocket 연결 훅
 *
 * @param options 연결 옵션
 * @returns WebSocket 제어 함수들
 */
export function useSupportWebSocket({
  chatId,
  isAdmin = false,
  enabled = true,
}: UseSupportWebSocketOptions): UseSupportWebSocketReturn {
  const clientRef = useRef<Client | null>(null);
  const { setConnectionStatus, addMessage, handleTypingEvent, setActiveChat } =
    useSupportStore();

  /**
   * WebSocket 연결 설정
   */
  useEffect(() => {
    if (!chatId || !enabled) {
      return;
    }

    const memberInfo = getCookie("member");
    if (!memberInfo?.accessToken) {
      setConnectionStatus("error");
      return;
    }

    setConnectionStatus("connecting");

    const client = new Client({
      webSocketFactory: () => new SockJS(`${ENV.API_URL}/ws`),
      connectHeaders: {
        Authorization: `Bearer ${memberInfo.accessToken}`,
      },
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,

      onConnect: () => {
        setConnectionStatus("connected");

        // 상담 메시지 구독
        client.subscribe(`/topic/support/${chatId}`, (message: IMessage) => {
          const data = JSON.parse(message.body);

          // 이벤트 메시지 (관리자 배정, 상담 완료 등)
          if (data.type) {
            const event = data as SupportEvent;
            setActiveChat(event.chat);
            return;
          }

          // 일반 메시지
          const supportMessage = data as SupportMessageDTO;
          addMessage(supportMessage);
        });

        // 타이핑 이벤트 구독
        client.subscribe(
          `/topic/support/${chatId}/typing`,
          (message: IMessage) => {
            const typingEvent = JSON.parse(message.body) as SupportTypingEvent;
            handleTypingEvent(typingEvent);
          }
        );

        // 에러 구독
        client.subscribe("/user/queue/errors", (message: IMessage) => {
          const error = JSON.parse(message.body);
          console.error("[SupportWebSocket] 에러:", error);
        });
      },

      onDisconnect: () => {
        setConnectionStatus("disconnected");
      },

      onStompError: (frame) => {
        console.error("[SupportWebSocket] STOMP 에러:", frame.headers.message);
        setConnectionStatus("error");
      },
    });

    client.activate();
    clientRef.current = client;

    return () => {
      client.deactivate();
      clientRef.current = null;
    };
  }, [
    chatId,
    enabled,
    setConnectionStatus,
    addMessage,
    handleTypingEvent,
    setActiveChat,
  ]);

  /**
   * 메시지 전송
   */
  const sendMessage = useCallback(
    (message: string) => {
      if (!clientRef.current?.connected || !chatId) {
        return;
      }

      // 관리자와 사용자의 destination이 다름
      const destination = isAdmin
        ? `/app/support-admin/${chatId}`
        : `/app/support/${chatId}`;

      clientRef.current.publish({
        destination,
        body: JSON.stringify({ message }),
      });
    },
    [chatId, isAdmin]
  );

  /**
   * 타이핑 이벤트 전송
   */
  const sendTyping = useCallback(
    (typing: boolean) => {
      if (!clientRef.current?.connected || !chatId) {
        return;
      }

      clientRef.current.publish({
        destination: `/app/support/${chatId}/typing`,
        body: JSON.stringify({ typing, isAdmin }),
      });
    },
    [chatId, isAdmin]
  );

  const isConnected = useSupportStore(
    (state) => state.connectionStatus === "connected"
  );

  return {
    sendMessage,
    sendTyping,
    isConnected,
  };
}
