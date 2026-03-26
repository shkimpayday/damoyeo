/**
 * ============================================================================
 * 채팅 Zustand Store
 * ============================================================================
 *
 * [역할]
 * WebSocket으로 수신한 실시간 메시지 상태를 관리합니다.
 *
 * [상태 관리 전략]
 * - Zustand: 실시간 메시지 배열, WebSocket 연결 상태
 * - TanStack Query: 메시지 히스토리 (서버 데이터 캐싱)
 *
 * [사용 위치]
 * - hooks/use-websocket.ts: 메시지 수신 시 addMessage 호출
 * - components/chat-room.tsx: 메시지 목록 표시
 *
 * @author damoyeo
 * @since 2025-02-25
 */

import { create } from "zustand";
import type { ChatMessageDTO, ConnectionStatus } from "../types";

/**
 * 채팅 상태 인터페이스
 */
interface ChatState {
  // ========================================================================
  // 상태
  // ========================================================================

  /**
   * 현재 채팅방의 메시지 배열
   *
   * [주의]
   * - WebSocket으로 수신한 메시지만 포함
   * - 초기 히스토리는 TanStack Query로 관리
   * - 채팅방 전환 시 clearMessages() 호출 필요
   */
  messages: ChatMessageDTO[];

  /**
   * WebSocket 연결 상태
   *
   * - connecting: 연결 중
   * - connected: 연결됨 (정상)
   * - disconnected: 연결 해제됨
   * - error: 에러 발생
   */
  connectionStatus: ConnectionStatus;

  /**
   * 타이핑 중인 사용자 목록
   *
   * Set<email>로 관리
   * 3초간 타이핑 이벤트가 없으면 자동 제거
   */
  typingUsers: Set<string>;

  // ========================================================================
  // Actions
  // ========================================================================

  /**
   * 메시지 배열 전체 설정
   *
   * [사용 시점]
   * 초기 메시지 히스토리를 로드했을 때
   */
  setMessages: (messages: ChatMessageDTO[]) => void;

  /**
   * 새 메시지 추가 (배열 끝에)
   *
   * [사용 시점]
   * WebSocket으로 새 메시지를 수신했을 때
   * 배열 끝에 추가됩니다.
   */
  addMessage: (message: ChatMessageDTO) => void;

  /**
   * 이전 메시지 추가 (배열 앞에)
   *
   * [사용 시점]
   * 무한 스크롤로 이전 메시지를 로드했을 때
   * 배열 앞에 추가됩니다.
   */
  prependMessages: (messages: ChatMessageDTO[]) => void;

  /**
   * WebSocket 연결 상태 변경
   */
  setConnectionStatus: (status: ConnectionStatus) => void;

  /**
   * 메시지 배열 초기화
   *
   * [사용 시점]
   * 채팅방을 나가거나 다른 채팅방으로 전환할 때
   */
  clearMessages: () => void;

  /**
   * 타이핑 중인 사용자 추가
   */
  addTypingUser: (email: string) => void;

  /**
   * 타이핑 중인 사용자 제거
   */
  removeTypingUser: (email: string) => void;

  /**
   * 타이핑 사용자 목록 초기화
   */
  clearTypingUsers: () => void;

  /**
   * WebSocket 에러 메시지 (alert 대신 UI에 표시)
   *
   * null이면 에러 없음
   */
  errorMessage: string | null;

  /**
   * 에러 메시지 설정
   * @param msg null 전달 시 에러 클리어
   */
  setErrorMessage: (msg: string | null) => void;
}

/**
 * 채팅 Store
 *
 * [사용 예시]
 * ```typescript
 * const { messages, addMessage, connectionStatus } = useChatStore();
 *
 * // 새 메시지 추가
 * addMessage(newMessage);
 *
 * // 연결 상태 확인
 * if (connectionStatus === 'connected') {
 *   // 메시지 전송 가능
 * }
 * ```
 */
export const useChatStore = create<ChatState>((set) => ({
  // ========================================================================
  // 초기 상태
  // ========================================================================

  messages: [],
  connectionStatus: "disconnected",
  typingUsers: new Set(),

  // ========================================================================
  // Actions
  // ========================================================================

  setMessages: (messages) =>
    set({ messages }),

  addMessage: (message) =>
    set((state) => ({
      messages: [...state.messages, message],
    })),

  prependMessages: (messages) =>
    set((state) => ({
      messages: [...messages, ...state.messages],
    })),

  setConnectionStatus: (status) =>
    set({ connectionStatus: status }),

  clearMessages: () =>
    set({ messages: [] }),

  addTypingUser: (email) =>
    set((state) => {
      const newTypingUsers = new Set(state.typingUsers);
      newTypingUsers.add(email);
      return { typingUsers: newTypingUsers };
    }),

  removeTypingUser: (email) =>
    set((state) => {
      const newTypingUsers = new Set(state.typingUsers);
      newTypingUsers.delete(email);
      return { typingUsers: newTypingUsers };
    }),

  clearTypingUsers: () =>
    set({ typingUsers: new Set() }),

  errorMessage: null,

  setErrorMessage: (msg) =>
    set({ errorMessage: msg }),
}));
