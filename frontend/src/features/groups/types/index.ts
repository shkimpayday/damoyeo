import type { MemberSummary, LocationInfo } from "@/features/auth/types";

// 카테고리
export interface Category {
  id: number;
  name: string;
  icon: string;
  displayOrder: number;
  image?: string;
  color?: string;
}

// 카테고리 이름 타입
export type CategoryName =
  | "운동/스포츠"
  | "사교/인맥"
  | "아웃도어/여행"
  | "문화/공연"
  | "음악/악기"
  | "외국어"
  | "독서"
  | "스터디"
  | "게임/오락"
  | "사진/영상"
  | "요리"
  | "공예"
  | "자기계발"
  | "봉사활동"
  | "반려동물"
  | "IT/개발"
  | "금융/재테크"
  | "기타";

// 모임 상태
export type GroupStatus = "ACTIVE" | "INACTIVE" | "DELETED";

// 모임 멤버 역할
export type GroupRole = "OWNER" | "MANAGER" | "MEMBER";

// 모임 멤버 상태 (가입 시 즉시 APPROVED, BANNED는 강퇴된 상태)
export type GroupMemberStatus = "APPROVED" | "BANNED";

// 모임 상세 DTO
export interface GroupDTO {
  id: number;
  name: string;
  description: string;
  category: Category;
  coverImage: string;
  thumbnailImage: string;
  location: LocationInfo | null;
  address: string;
  maxMembers: number;
  memberCount: number;
  isPublic: boolean;
  status: GroupStatus;
  owner: MemberSummary;
  myRole?: GroupRole | null;
  createdAt: string;
  updatedAt: string;
}

// 모임 목록용 DTO (GroupDTO와 동일한 구조)
export interface GroupListDTO {
  id: number;
  name: string;
  description?: string;
  category: Category;
  coverImage?: string;
  thumbnailImage?: string;
  location?: LocationInfo | null;
  address?: string;
  maxMembers: number;
  memberCount: number;
  isPublic?: boolean;
  status?: GroupStatus;
  owner?: MemberSummary;
  myRole?: GroupRole | null;
  createdAt?: string;
  updatedAt?: string;
  distance?: number;
}

// 모임 생성 요청
export interface GroupCreateRequest {
  name: string;
  description: string;
  categoryId: number;
  coverImage?: File;
  address: string;
  lat?: number;
  lng?: number;
  maxMembers: number;
  isPublic: boolean;
}

// 모임 수정 요청
export interface GroupUpdateRequest {
  name?: string;
  description?: string;
  categoryId?: number;
  coverImage?: File;
  address?: string;
  lat?: number;
  lng?: number;
  maxMembers?: number;
  isPublic?: boolean;
}

// 모임 검색 파라미터
export interface GroupSearchParams {
  page?: number;
  size?: number;
  keyword?: string;
  categoryId?: number;
  lat?: number;
  lng?: number;
  radius?: number;
  sort?: "latest" | "popular" | "distance";
}

// 모임 멤버 DTO
export interface GroupMemberDTO {
  id: number;
  member: MemberSummary;
  role: GroupRole;
  joinedAt: string;
}

// 멤버 역할 변경 요청
export interface MemberRoleUpdateRequest {
  role?: GroupRole;
}

// 페이지 응답 DTO
export interface PageResponseDTO<T> {
  dtoList: T[];
  pageNumList: number[];
  prev: boolean;
  next: boolean;
  totalCount: number;
  prevPage: number;
  nextPage: number;
  totalPage: number;
  current: number;
}
