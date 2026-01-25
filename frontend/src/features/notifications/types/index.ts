// 알림 타입
export type NotificationType =
  | "GROUP_INVITE"
  | "GROUP_JOIN_REQUEST"
  | "GROUP_JOIN_APPROVED"
  | "GROUP_JOIN_REJECTED"
  | "MEETING_REMINDER"
  | "MEETING_CREATED"
  | "NEW_MESSAGE"
  | "MEMBER_JOINED";

// 참조 타입
export type NotificationRefType = "GROUP" | "MEETING" | "CHAT";

// 알림 DTO
export interface NotificationDTO {
  id: number;
  type: NotificationType;
  title: string;
  content: string;
  referenceId: number;
  referenceType: NotificationRefType;
  isRead: boolean;
  createdAt: string;
}

// 알림 목록 응답
export interface NotificationListResponse {
  notifications: NotificationDTO[];
  unreadCount: number;
}
