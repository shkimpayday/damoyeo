/**
 * ============================================================================
 * WebSocket 연결 및 메시지 송수신 Hook
 * ============================================================================
 *
 * [역할]
 * WebSocket STOMP 클라이언트를 생성하고 채팅방을 구독합니다.
 *
 * [기능]
 * - JWT 인증을 포함한 WebSocket 연결
 * - 특정 그룹 채팅방 구독 (/topic/chat/{groupId})
 * - 메시지 전송 (/app/chat/{groupId})
 * - 연결 상태 관리
 * - 토큰 갱신 시 자동 재연결
 *
 * [사용 예시]
 * ```typescript
 * const { isConnected, sendMessage } = useWebSocket(groupId);
 *
 * if (isConnected) {
 *   sendMessage('안녕하세요');
 * }
 * ```
 *
 * @author damoyeo
 * @since 2025-02-25
 */

import { useEffect, useRef, useState } from "react";
import { Client } from "@stomp/stompjs";
import SockJS from "sockjs-client";
import { useAuthStore } from "@/features/auth/stores/auth-store";
import { useChatStore } from "../stores/chat-store";
import { ENV } from "@/config";
import type { ChatMessageDTO } from "../types";

/**
 * WebSocket 연결 및 메시지 송수신 Hook
 *
 * @param groupId 채팅방 그룹 ID
 * @returns {isConnected, sendMessage}
 */
export function useWebSocket(groupId: number) {
  const { member } = useAuthStore();
  const { addMessage, setConnectionStatus, addTypingUser, removeTypingUser, setErrorMessage } = useChatStore();

  const clientRef = useRef<Client | null>(null);
  const [isConnected, setIsConnected] = useState(false);

  useEffect(() => {
    // 1. JWT 토큰이 없으면 연결하지 않음
    if (!member?.accessToken) {
      setConnectionStatus("disconnected");
      return;
    }

    // 2. STOMP 클라이언트 생성
    const client = new Client({
      // SockJS를 WebSocket 팩토리로 사용 (폴백 지원)
      webSocketFactory: () => new SockJS(`${ENV.API_URL}/ws`),

      // CONNECT 프레임에 JWT 토큰 포함
      connectHeaders: {
        Authorization: `Bearer ${member.accessToken}`,
      },

      // 재연결 설정
      reconnectDelay: 5000, // 5초 후 재연결 시도
      heartbeatIncoming: 4000, // 서버 → 클라이언트 heartbeat
      heartbeatOutgoing: 4000, // 클라이언트 → 서버 heartbeat

      // 연결 성공 시
      onConnect: () => {
        setIsConnected(true);
        setConnectionStatus("connected");

        // 채팅방 구독: /topic/chat/{groupId}
        // 이 경로로 오는 모든 메시지를 수신합니다.
        client.subscribe(
          `/topic/chat/${groupId}`,
          (message) => {
            try {
              const chatMessage: ChatMessageDTO = JSON.parse(message.body);
              // Zustand store에 메시지 추가
              addMessage(chatMessage);
            } catch (error) {
              console.error("[WebSocket] Message parse error:", error);
            }
          }
        );

        // 에러 메시지 구독 (개인)
        // 서버에서 발생한 에러를 수신합니다.
        client.subscribe(`/user/queue/errors`, (message) => {
          try {
            const error = JSON.parse(message.body);
            setErrorMessage(error.error || "메시지 전송 중 오류가 발생했습니다.");
          } catch (e) {
            console.error("[WebSocket] Error parse error:", e);
          }
        });

        // 타이핑 인디케이터 구독
        // 다른 사용자가 타이핑 중일 때 이벤트를 수신합니다.
        client.subscribe(`/topic/chat/${groupId}/typing`, (message) => {
          try {
            const typingEvent: { email: string; typing: boolean } = JSON.parse(
              message.body
            );

            // 본인의 타이핑 이벤트는 무시
            if (typingEvent.email === member?.email) {
              return;
            }

            if (typingEvent.typing) {
              // 타이핑 시작
              addTypingUser(typingEvent.email);

              // 3초 후 자동 제거
              setTimeout(() => {
                removeTypingUser(typingEvent.email);
              }, 3000);
            } else {
              // 타이핑 종료
              removeTypingUser(typingEvent.email);
            }
          } catch (error) {
            console.error("[WebSocket] Typing event parse error:", error);
          }
        });
      },

      // 연결 해제 시
      onDisconnect: () => {
        setIsConnected(false);
        setConnectionStatus("disconnected");
      },

      onStompError: (_frame) => {
        setConnectionStatus("error");
      },

      onWebSocketError: (_event) => {
        setConnectionStatus("error");
      },
    });

    // 3. WebSocket 연결 시작
    client.activate();
    clientRef.current = client;
    setConnectionStatus("connecting");

    // 4. Cleanup: 컴포넌트 언마운트 시 연결 해제
    return () => {
      client.deactivate();
      setIsConnected(false);
      setConnectionStatus("disconnected");
    };
  }, [
    groupId,
    member?.accessToken,
    member?.email,
    addMessage,
    setConnectionStatus,
    addTypingUser,
    removeTypingUser,
    setErrorMessage,
  ]);

  /**
   * 메시지 전송
   *
   * [처리 흐름]
   * 1. 클라이언트: SEND → /app/chat/{groupId}
   * 2. 서버: ChatController.sendMessage() 호출
   * 3. 서버: 검증 및 저장 후 브로드캐스트 → /topic/chat/{groupId}
   * 4. 모든 구독자: 메시지 수신 (본인 포함)
   *
   * @param message 메시지 내용
   */
  const sendMessage = (message: string) => {
    if (!clientRef.current?.connected) {
      setErrorMessage("연결이 끊어졌습니다. 잠시 후 다시 시도해주세요.");
      return;
    }

    if (!message.trim()) {
      return;
    }

    try {
      // STOMP SEND 프레임 전송
      clientRef.current.publish({
        destination: `/app/chat/${groupId}`,
        body: JSON.stringify({ message: message.trim() }),
      });
    } catch (error) {
      console.error("[WebSocket] Send error:", error);
      setErrorMessage("메시지 전송 중 오류가 발생했습니다.");
    }
  };

  /**
   * 타이핑 이벤트 전송
   *
   * [처리 흐름]
   * 1. 클라이언트: SEND → /app/chat/{groupId}/typing
   * 2. 서버: 브로드캐스트 → /topic/chat/{groupId}/typing
   * 3. 다른 구독자: 타이핑 인디케이터 표시
   *
   * @param typing 타이핑 중 여부
   */
  const sendTyping = (typing: boolean = true) => {
    if (!clientRef.current?.connected) {
      return;
    }

    try {
      clientRef.current.publish({
        destination: `/app/chat/${groupId}/typing`,
        body: JSON.stringify({ typing }),
      });
    } catch (error) {
      console.error("[WebSocket] Typing event send error:", error);
    }
  };

  return {
    isConnected,
    sendMessage,
    sendTyping,
  };
}
