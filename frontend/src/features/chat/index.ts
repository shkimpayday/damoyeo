/**
 * ============================================================================
 * Chat Feature Public API
 * ============================================================================
 *
 * [역할]
 * chat feature의 공개 API를 export합니다.
 * 다른 feature나 페이지에서 사용할 컴포넌트, hooks, 타입 등을 제공합니다.
 *
 * [Export 규칙]
 * - 외부에서 사용할 것만 export
 * - 내부 구현 세부사항은 export하지 않음
 *
 * @author damoyeo
 * @since 2025-02-25
 */

// API
export * from "./api/chat-api";

// Types
export type * from "./types";

// Components
export { ChatRoom } from "./components/chat-room";
export { ChatHeader } from "./components/chat-header";
export { MessageList } from "./components/message-list";
export { MessageItem } from "./components/message-item";
export { MessageInput } from "./components/message-input";
export { MeetingChatRoom } from "./components/meeting-chat-room";

// Hooks (모임 채팅)
export { useWebSocket } from "./hooks/use-websocket";
export { useChatMessagesInfinite } from "./hooks/use-chat-messages";
export { useChatRoom } from "./hooks/use-chat-room";

// Hooks (정모 채팅)
export { useMeetingWebSocket } from "./hooks/use-meeting-websocket";
export { useMeetingChatMessagesInfinite } from "./hooks/use-meeting-chat-messages";
export { useMeetingChatRoom } from "./hooks/use-meeting-chat-room";

// Stores
export { useChatStore } from "./stores/chat-store";
