// 회원 기본 정보 (JWT 토큰 포함)
export interface MemberInfo {
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
}

// 위치 업데이트 요청
export interface LocationUpdateRequest {
  lat: number;
  lng: number;
  address: string;
}

// Category 타입 import
import type { Category } from "@/features/groups/types";
