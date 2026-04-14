import { create } from "zustand";
import type { ChatMessageDTO, ConnectionStatus } from "../types";

// Zustand: 실시간 WebSocket 메시지 상태 관리
// 메시지 히스토리(초기 로드)는 TanStack Query가 담당
interface ChatState {
  messages: ChatMessageDTO[];
  connectionStatus: ConnectionStatus;
  // 타이핑 중인 사용자 이메일 목록 (3초간 이벤트 없으면 자동 제거)
  typingUsers: Set<string>;
  errorMessage: string | null;

  setMessages: (messages: ChatMessageDTO[]) => void;
  addMessage: (message: ChatMessageDTO) => void;
  // 무한 스크롤로 이전 메시지를 로드했을 때 배열 앞에 추가
  prependMessages: (messages: ChatMessageDTO[]) => void;
  setConnectionStatus: (status: ConnectionStatus) => void;
  clearMessages: () => void;
  addTypingUser: (email: string) => void;
  removeTypingUser: (email: string) => void;
  clearTypingUsers: () => void;
  setErrorMessage: (msg: string | null) => void;
}

export const useChatStore = create<ChatState>((set) => ({
  messages: [],
  connectionStatus: "disconnected",
  typingUsers: new Set(),
  errorMessage: null,

  setMessages: (messages) => set({ messages }),

  addMessage: (message) =>
    set((state) => ({ messages: [...state.messages, message] })),

  prependMessages: (messages) =>
    set((state) => ({ messages: [...messages, ...state.messages] })),

  setConnectionStatus: (status) => set({ connectionStatus: status }),

  clearMessages: () => set({ messages: [] }),

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

  clearTypingUsers: () => set({ typingUsers: new Set() }),

  setErrorMessage: (msg) => set({ errorMessage: msg }),
}));
