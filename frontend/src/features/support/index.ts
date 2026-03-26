/**
 * Support Feature Public API
 *
 * 상담 채팅 기능의 공개 인터페이스입니다.
 */

// Components
export { SupportFloatingButton } from "./components/support-floating-button";
export { SupportChatRoom } from "./components/support-chat-room";
export { SupportMessageItem } from "./components/support-message-item";
export { CreateSupportChatModal } from "./components/create-support-chat-modal";

// Hooks
export { useSupportChat } from "./hooks/use-support-chat";
export { useSupportWebSocket } from "./hooks/use-support-websocket";

// Store
export { useSupportStore } from "./stores/support-store";

// API
export * as supportApi from "./api/support-api";

// Types
export type {
  SupportChatDTO,
  SupportMessageDTO,
  SupportChatStatus,
  CreateSupportChatRequest,
  SendSupportMessageRequest,
  SupportConnectionStatus,
  SupportTypingEvent,
  SupportEvent,
  SupportEventType,
  PageResponseDTO,
} from "./types";
