import { create } from "zustand";
import type {
  SupportChatDTO,
  SupportMessageDTO,
  SupportConnectionStatus,
  SupportTypingEvent,
} from "../types";

interface SupportState {
  activeChat: SupportChatDTO | null;
  messages: SupportMessageDTO[];
  connectionStatus: SupportConnectionStatus;
  typingUsers: SupportTypingEvent[];
  isOpen: boolean;

  setActiveChat: (chat: SupportChatDTO | null) => void;
  setMessages: (messages: SupportMessageDTO[]) => void;
  addMessage: (message: SupportMessageDTO) => void;
  prependMessages: (messages: SupportMessageDTO[]) => void;
  setConnectionStatus: (status: SupportConnectionStatus) => void;
  handleTypingEvent: (event: SupportTypingEvent) => void;
  openChat: () => void;
  closeChat: () => void;
  reset: () => void;
}

export const useSupportStore = create<SupportState>((set) => ({
  activeChat: null,
  messages: [],
  connectionStatus: "disconnected",
  typingUsers: [],
  isOpen: false,

  setActiveChat: (chat) => set({ activeChat: chat }),

  setMessages: (messages) => set({ messages }),

  addMessage: (message) =>
    set((state) => {
      // WebSocket 구독이 두 개일 때 중복 메시지 방지
      const isDuplicate = state.messages.some((m) => m.id === message.id);
      if (isDuplicate) return state;
      return { messages: [...state.messages, message] };
    }),

  prependMessages: (messages) =>
    set((state) => ({ messages: [...messages, ...state.messages] })),

  setConnectionStatus: (status) => set({ connectionStatus: status }),

  handleTypingEvent: (event) =>
    set((state) => {
      if (event.typing) {
        const exists = state.typingUsers.some((u) => u.email === event.email);
        if (!exists) {
          return { typingUsers: [...state.typingUsers, event] };
        }
        return state;
      } else {
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
