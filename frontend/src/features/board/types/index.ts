/**
 * ============================================================================
 * 모임 게시판 타입 정의
 * ============================================================================
 *
 * [게시판 카테고리]
 * - GREETING: 가입인사 - 신규 멤버가 인사를 남기는 공간
 * - REVIEW: 모임후기 - 정모 후기, 활동 소감 등
 * - FREE: 자유게시판 - 자유로운 주제의 게시글
 * - NOTICE: 공지사항 - 운영진만 작성 가능
 */

/** 게시글 카테고리 */
export type BoardCategory = "GREETING" | "REVIEW" | "FREE" | "NOTICE";

/** 모임 내 사용자 역할 */
export type GroupRole = "OWNER" | "MANAGER" | "MEMBER";

/** 게시글 카테고리 한글 레이블 */
export const BOARD_CATEGORY_LABELS: Record<BoardCategory, string> = {
  GREETING: "가입인사",
  REVIEW: "모임후기",
  FREE: "자유게시판",
  NOTICE: "공지사항",
};

/** 게시글 카테고리 색상 (Tailwind) */
export const BOARD_CATEGORY_COLORS: Record<BoardCategory, { bg: string; text: string; border: string }> = {
  GREETING: { bg: "bg-green-50", text: "text-green-700", border: "border-green-200" },
  REVIEW: { bg: "bg-blue-50", text: "text-blue-700", border: "border-blue-200" },
  FREE: { bg: "bg-gray-100", text: "text-gray-600", border: "border-gray-200" },
  NOTICE: { bg: "bg-red-50", text: "text-red-700", border: "border-red-200" },
};

/**
 * 게시글 작성자 정보
 */
export interface BoardAuthor {
  id: number;
  nickname: string;
  profileImage?: string;
}

/**
 * 게시글 내 이미지 정보
 */
export interface BoardImage {
  id: number;
  imageUrl: string;
  originalFileName?: string;
}

/**
 * 게시글 DTO (목록 / 상세 공용)
 */
export interface BoardPostDTO {
  /** 게시글 ID */
  id: number;
  /** 모임 ID */
  groupId: number;
  /** 카테고리 */
  category: BoardCategory;
  /** 제목 */
  title: string;
  /** 본문 */
  content: string;
  /** 첨부 이미지 목록 */
  images: BoardImage[];
  /** 이미지 개수 */
  imageCount: number;
  /** 대표 이미지 URL (첫 번째 이미지) */
  thumbnailUrl?: string;
  /** 작성자 정보 */
  author: BoardAuthor;
  /** 좋아요 수 */
  likeCount: number;
  /** 댓글 수 */
  commentCount: number;
  /** 현재 사용자 좋아요 여부 */
  liked: boolean;
  /** 상단 고정 여부 (공지) */
  isPinned: boolean;
  /** 삭제 가능 여부 (본인 or 운영진) */
  canDelete: boolean;
  /** 작성 일시 */
  createdAt: string;
  /** 수정 일시 */
  updatedAt: string;
}

/**
 * 게시글 목록 응답 (페이지네이션)
 */
export interface BoardPageResponse {
  dtoList: BoardPostDTO[];
  pageNumList: number[];
  prev: boolean;
  next: boolean;
  totalCount: number;
  totalPage: number;
  current: number;
}

/**
 * 게시글 작성 요청
 */
export interface BoardPostCreateRequest {
  category: BoardCategory;
  title: string;
  content: string;
  files?: File[];
}

/**
 * 좋아요 토글 응답
 */
export interface BoardLikeToggleResponse {
  liked: boolean;
  likeCount: number;
}

/**
 * 댓글 작성자 정보
 */
export interface CommentAuthor {
  id: number;
  nickname: string;
  profileImage?: string;
}

/**
 * 게시글 댓글 DTO
 */
export interface BoardCommentDTO {
  id: number;
  postId: number;
  content: string;
  author: CommentAuthor;
  createdAt: string;
  canDelete: boolean;
}
