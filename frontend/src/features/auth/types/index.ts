// 회원 기본 정보 (JWT 토큰 포함)
export interface MemberInfo {
  id?: number;          // JWT 토큰에서 받아오는 회원 ID
  email: string;
  nickname: string;
  profileImage: string;
  accessToken: string;
  refreshToken: string;
  roleNames: string[];
  social?: boolean;
}

// 프로필 정보 (상세)
export interface ProfileInfo {
  email: string;
  nickname: string;
  profileImage: string;
  introduction: string;
  location: LocationInfo | null;
  address: string;
  interests: Category[];
  createdAt: string;
  showJoinedGroups: boolean;  // 활동 모임 공개 여부 (프리미엄 전용)
  roleNames: string[];        // 권한 목록 (PREMIUM 확인용)
}

// 위치 정보
export interface LocationInfo {
  lat: number;
  lng: number;
}

// 회원 요약 정보 (목록 표시용)
export interface MemberSummary {
  id: number;
  nickname: string;
  profileImage?: string;
}

// 회원가입 요청
export interface SignupRequest {
  email: string;
  password: string;
  nickname: string;
}

// 프로필 수정 요청
export interface ProfileUpdateRequest {
  nickname?: string;
  introduction?: string;
  interests?: number[];
  showJoinedGroups?: boolean;  // 활동 모임 공개 여부 (프리미엄 전용)
}

// 위치 업데이트 요청
export interface LocationUpdateRequest {
  lat: number;
  lng: number;
  address: string;
}

// Category 타입 import
import type { Category } from "@/features/groups/types";

// 공개 프로필 (다른 회원이 볼 수 있는 정보)
export interface PublicProfile {
  id: number;
  nickname: string;
  profileImage?: string;
  introduction?: string;
  address?: string;
  createdAt: string;
  groupCount: number;
  joinedGroups: JoinedGroup[];
  showJoinedGroups: boolean;  // 활동 모임 공개 여부
}

// 가입한 모임 정보 (공개 프로필용)
export interface JoinedGroup {
  id: number;
  name: string;
  thumbnailImage?: string;
  categoryName: string;
}
