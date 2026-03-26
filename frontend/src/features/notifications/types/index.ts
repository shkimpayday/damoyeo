/**
 * 알림 타입
 *
 * [정모 관련]
 * - NEW_MEETING: 새 정모 등록
 * - MEETING_REMINDER: 정모 리마인더 (1일 전)
 * - MEETING_IMMINENT: 정모 임박 (3시간 전)
 * - MEETING_UPDATED: 정모 정보 변경 (장소/시간)
 * - MEETING_CANCELLED: 정모 취소
 *
 * [멤버/모임 관련]
 * - NEW_MEMBER: 새 멤버 가입
 * - MEMBER_LEFT: 멤버 탈퇴
 * - GROUP_UPDATE: 모임 정보 변경
 * - ROLE_CHANGED: 역할 변경
 * - MEMBER_KICKED: 멤버 강퇴
 * - GROUP_DISBANDED: 모임 해체
 *
 * [시스템 알림]
 * - WELCOME: 회원가입 환영
 */
export type NotificationType =
  // 정모 관련
  | "NEW_MEETING"
  | "MEETING_REMINDER"
  | "MEETING_IMMINENT"
  | "MEETING_UPDATED"
  | "MEETING_CANCELLED"
  // 멤버/모임 관련
  | "NEW_MEMBER"
  | "MEMBER_LEFT"
  | "GROUP_UPDATE"
  | "ROLE_CHANGED"
  | "MEMBER_KICKED"
  | "GROUP_DISBANDED"
  // 시스템 알림
  | "WELCOME";

// 참조 타입
export type NotificationRefType = "GROUP" | "MEETING" | "SYSTEM";

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
