export { SupportFloatingButton } from "./components/support-floating-button";
export { SupportChatRoom } from "./components/support-chat-room";
export { SupportMessageItem } from "./components/support-message-item";
export { CreateSupportChatModal } from "./components/create-support-chat-modal";

export { useSupportChat } from "./hooks/use-support-chat";
export { useSupportWebSocket } from "./hooks/use-support-websocket";

export { useSupportStore } from "./stores/support-store";

export * as supportApi from "./api/support-api";

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
