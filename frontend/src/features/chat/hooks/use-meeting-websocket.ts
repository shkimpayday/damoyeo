/**
 * ============================================================================
 * 정모 채팅 WebSocket 연결 및 메시지 송수신 Hook
 * ============================================================================
 *
 * [역할]
 * 정모 참석자 전용 채팅을 위한 WebSocket STOMP 클라이언트를 생성합니다.
 *
 * [기능]
 * - JWT 인증을 포함한 WebSocket 연결
 * - 정모 채팅방 구독 (/topic/meeting-chat/{meetingId})
 * - 메시지 전송 (/app/meeting-chat/{meetingId})
 * - 연결 상태 관리
 *
 * [권한]
 * ATTENDING 상태의 참석자만 채팅에 참여할 수 있습니다.
 *
 * @author damoyeo
 * @since 2025-03-16
 */

import { useEffect, useRef, useState } from "react";
import { Client } from "@stomp/stompjs";
import SockJS from "sockjs-client";
import { useAuthStore } from "@/features/auth/stores/auth-store";
import { useChatStore } from "../stores/chat-store";
import { ENV } from "@/config";
import type { ChatMessageDTO } from "../types";

/**
 * 정모 채팅 WebSocket 연결 및 메시지 송수신 Hook
 *
 * @param meetingId 정모 ID
 * @returns {isConnected, sendMessage, sendTyping}
 */
export function useMeetingWebSocket(meetingId: number) {
  const { member } = useAuthStore();
  const { addMessage, setConnectionStatus, addTypingUser, removeTypingUser } = useChatStore();

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
      webSocketFactory: () => new SockJS(`${ENV.API_URL}/ws`),

      connectHeaders: {
        Authorization: `Bearer ${member.accessToken}`,
      },

      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,

      onConnect: () => {
        setIsConnected(true);
        setConnectionStatus("connected");

        // 정모 채팅방 구독: /topic/meeting-chat/{meetingId}
        client.subscribe(
          `/topic/meeting-chat/${meetingId}`,
          (message) => {
            try {
              const chatMessage: ChatMessageDTO = JSON.parse(message.body);
              addMessage(chatMessage);
            } catch (error) {
              console.error("[Meeting WebSocket] Message parse error:", error);
            }
          }
        );

        // 에러 메시지 구독 (개인)
        client.subscribe(`/user/queue/errors`, (message) => {
          try {
            const error = JSON.parse(message.body);
            alert(error.error || "메시지 전송 중 오류가 발생했습니다.");
          } catch (e) {
            console.error("[Meeting WebSocket] Error parse error:", e);
          }
        });

        // 타이핑 인디케이터 구독
        client.subscribe(`/topic/meeting-chat/${meetingId}/typing`, (message) => {
          try {
            const typingEvent: { email: string; typing: boolean } = JSON.parse(
              message.body
            );

            // 본인의 타이핑 이벤트는 무시
            if (typingEvent.email === member?.email) {
              return;
            }

            if (typingEvent.typing) {
              addTypingUser(typingEvent.email);
              setTimeout(() => {
                removeTypingUser(typingEvent.email);
              }, 3000);
            } else {
              removeTypingUser(typingEvent.email);
            }
          } catch (error) {
            console.error("[Meeting WebSocket] Typing event parse error:", error);
          }
        });
      },

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

    // 4. Cleanup
    return () => {
      client.deactivate();
      setIsConnected(false);
      setConnectionStatus("disconnected");
    };
  }, [
    meetingId,
    member?.accessToken,
    member?.email,
    addMessage,
    setConnectionStatus,
    addTypingUser,
    removeTypingUser,
  ]);

  /**
   * 메시지 전송
   */
  const sendMessage = (message: string) => {
    if (!clientRef.current?.connected) {
      alert("연결이 끊어졌습니다. 잠시 후 다시 시도해주세요.");
      return;
    }

    if (!message.trim()) {
      return;
    }

    try {
      clientRef.current.publish({
        destination: `/app/meeting-chat/${meetingId}`,
        body: JSON.stringify({ message: message.trim() }),
      });
    } catch (error) {
      console.error("[Meeting WebSocket] Send error:", error);
      alert("메시지 전송 중 오류가 발생했습니다.");
    }
  };

  /**
   * 타이핑 이벤트 전송
   */
  const sendTyping = (typing: boolean = true) => {
    if (!clientRef.current?.connected) {
      return;
    }

    try {
      clientRef.current.publish({
        destination: `/app/meeting-chat/${meetingId}/typing`,
        body: JSON.stringify({ typing }),
      });
    } catch (error) {
      console.error("[Meeting WebSocket] Typing event send error:", error);
    }
  };

  return {
    isConnected,
    sendMessage,
    sendTyping,
  };
}
