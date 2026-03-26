/**
 * ============================================================================
 * 상담 채팅 Zustand Store
 * ============================================================================
 *
 * [역할]
 * 상담 채팅의 상태를 관리합니다.
 * - 현재 활성 상담
 * - 메시지 목록
 * - 연결 상태
 * - 타이핑 상태
 *
 * @author damoyeo
 * @since 2025-03-16
 */

import { create } from "zustand";
import type {
  SupportChatDTO,
  SupportMessageDTO,
  SupportConnectionStatus,
  SupportTypingEvent,
} from "../types";

interface SupportState {
  /**
   * 현재 활성 상담
   */
  activeChat: SupportChatDTO | null;

  /**
   * 메시지 목록
   */
  messages: SupportMessageDTO[];

  /**
   * WebSocket 연결 상태
   */
  connectionStatus: SupportConnectionStatus;

  /**
   * 상대방 타이핑 상태
   */
  typingUsers: SupportTypingEvent[];

  /**
   * 채팅방 열림 여부
   */
  isOpen: boolean;

  // ========================================================================
  // Actions
  // ========================================================================

  /**
   * 활성 상담 설정
   */
  setActiveChat: (chat: SupportChatDTO | null) => void;

  /**
   * 메시지 목록 설정
   */
  setMessages: (messages: SupportMessageDTO[]) => void;

  /**
   * 메시지 추가 (새 메시지 수신 시)
   */
  addMessage: (message: SupportMessageDTO) => void;

  /**
   * 메시지 목록 앞에 추가 (히스토리 로드 시)
   */
  prependMessages: (messages: SupportMessageDTO[]) => void;

  /**
   * 연결 상태 설정
   */
  setConnectionStatus: (status: SupportConnectionStatus) => void;

  /**
   * 타이핑 이벤트 처리
   */
  handleTypingEvent: (event: SupportTypingEvent) => void;

  /**
   * 채팅방 열기
   */
  openChat: () => void;

  /**
   * 채팅방 닫기
   */
  closeChat: () => void;

  /**
   * 상태 초기화
   */
  reset: () => void;
}

/**
 * 상담 채팅 Store
 */
export const useSupportStore = create<SupportState>((set) => ({
  // ========================================================================
  // 초기 상태
  // ========================================================================

  activeChat: null,
  messages: [],
  connectionStatus: "disconnected",
  typingUsers: [],
  isOpen: false,

  // ========================================================================
  // Actions
  // ========================================================================

  setActiveChat: (chat) => set({ activeChat: chat }),

  setMessages: (messages) => set({ messages }),

  addMessage: (message) =>
    set((state) => {
      // 중복 메시지 방지 (두 개의 WebSocket 구독 시 발생 가능)
      const isDuplicate = state.messages.some((m) => m.id === message.id);
      if (isDuplicate) return state;
      return { messages: [...state.messages, message] };
    }),

  prependMessages: (messages) =>
    set((state) => ({
      messages: [...messages, ...state.messages],
    })),

  setConnectionStatus: (status) => set({ connectionStatus: status }),

  handleTypingEvent: (event) =>
    set((state) => {
      if (event.typing) {
        // 타이핑 시작
        const exists = state.typingUsers.some((u) => u.email === event.email);
        if (!exists) {
          return { typingUsers: [...state.typingUsers, event] };
        }
        return state;
      } else {
        // 타이핑 종료
        return {
          typingUsers: state.typingUsers.filter((u) => u.email !== event.email),
        };
      }
    }),

  openChat: () => set({ isOpen: true }),

  closeChat: () => set({ isOpen: false }),

  reset: () =>
    set({
      activeChat: null,
      messages: [],
      connectionStatus: "disconnected",
      typingUsers: [],
      isOpen: false,
    }),
}));
