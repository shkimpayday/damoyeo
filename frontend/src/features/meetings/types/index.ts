import type { MemberSummary, LocationInfo } from "@/features/auth/types";

// 정모 상태
export type MeetingStatus = "SCHEDULED" | "ONGOING" | "COMPLETED" | "CANCELLED";

// 참석 상태
export type AttendStatus = "ATTENDING" | "MAYBE" | "NOT_ATTENDING";

// 정모 상세 DTO
export interface MeetingDTO {
  id: number;
  groupId: number;
  groupName: string;
  title: string;
  description?: string;
  location?: LocationInfo | null;
  address?: string;
  meetingDate: string;
  endDate?: string;
  maxAttendees: number;
  currentAttendees: number;
  fee: number;
  status: MeetingStatus;
  myStatus?: AttendStatus | null;
  /** 모임 멤버십 여부 (true: 멤버, false: 비멤버, null/undefined: 비로그인) */
  isGroupMember?: boolean | null;
  /** 현재 사용자의 정모 수정/취소 권한 여부 (생성자 또는 OWNER/MANAGER) */
  canEdit?: boolean;
  createdBy: MemberSummary;
  createdAt: string;
}

// 정모 목록용 DTO (간소화)
export interface MeetingListDTO {
  id: number;
  groupId: number;
  groupName: string;
  title: string;
  address: string;
  meetingDate: string;
  maxAttendees: number;
  currentAttendees: number;
  status: MeetingStatus;
}

// 정모 생성 요청
export interface MeetingCreateRequest {
  groupId?: number;
  title: string;
  description: string;
  address: string;
  lat?: number;
  lng?: number;
  meetingDate: string;
  endDate?: string;
  maxAttendees: number;
  fee?: number;
}

// 정모 수정 요청
export interface MeetingUpdateRequest {
  title?: string;
  description?: string;
  address?: string;
  lat?: number;
  lng?: number;
  meetingDate?: string;
  endDate?: string;
  maxAttendees?: number;
  fee?: number;
  status?: MeetingStatus;
}

// 참석자 DTO
export interface MeetingAttendeeDTO {
  id: number;
  member: MemberSummary;
  status: AttendStatus;
  checkedIn?: boolean;
  registeredAt: string;
}

// 참석 등록 요청
export interface AttendRequest {
  status: AttendStatus;
}
