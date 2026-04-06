/**
 * ============================================================================
 * 갤러리 타입 정의
 * ============================================================================
 *
 * [구조 변경]
 * - 기존: 개별 이미지(GalleryImage) 단위
 * - 변경: 게시물(GalleryPost) 단위 (여러 이미지 묶음)
 */

/**
 * 업로더/작성자 정보
 */
export interface GalleryUploader {
  id: number;
  nickname: string;
  profileImage?: string;
}

/**
 * 게시물 내 이미지 정보 (간단 버전)
 */
export interface GalleryImageSimple {
  /** 이미지 ID */
  id: number;
  /** 이미지 URL */
  imageUrl: string;
  /** 원본 파일명 */
  originalFileName?: string;
  /** 파일 크기 (bytes) */
  fileSize?: number;
}

/**
 * 갤러리 게시물 DTO
 *
 * [사용 위치]
 * - 갤러리 그리드
 * - 라이트박스 (캐러셀)
 * - 게시물 업로드 응답
 */
export interface GalleryPostDTO {
  /** 게시물 ID */
  id: number;
  /** 모임 ID */
  groupId: number;
  /** 캡션/설명 */
  caption?: string;
  /** 업로더 정보 */
  uploader: GalleryUploader;
  /** 게시물에 포함된 이미지 목록 */
  images: GalleryImageSimple[];
  /** 이미지 개수 */
  imageCount: number;
  /** 대표 이미지(썸네일) URL */
  thumbnailUrl: string;
  /** 업로드 일시 */
  createdAt: string;
  /** 현재 사용자가 삭제 가능한지 */
  canDelete: boolean;
  /** 좋아요 개수 */
  likeCount: number;
  /** 댓글 개수 */
  commentCount: number;
  /** 현재 사용자가 좋아요를 눌렀는지 */
  liked: boolean;
}

/**
 * 갤러리 게시물 목록 응답 (페이지네이션)
 */
export interface GalleryPageResponse {
  dtoList: GalleryPostDTO[];
  pageNumList: number[];
  prev: boolean;
  next: boolean;
  totalCount: number;
  totalPage: number;
  current: number;
}

/**
 * 갤러리 게시물 개수 응답
 */
export interface GalleryCountResponse {
  count: number;
}

/**
 * 게시물 업로드 요청
 */
export interface GalleryUploadRequest {
  groupId: number;
  files: File[];
  caption?: string;
}

// ========== 좋아요 ==========

/**
 * 좋아요 토글 응답
 */
export interface LikeToggleResponse {
  liked: boolean;
  likeCount: number;
}

// ========== 댓글 ==========

/**
 * 댓글 작성자 정보
 */
export interface CommentWriter {
  id: number;
  nickname: string;
  profileImage?: string;
}

/**
 * 갤러리 댓글 DTO
 */
export interface GalleryCommentDTO {
  /** 댓글 ID */
  id: number;
  /** 게시물 ID */
  postId: number;
  /** 댓글 내용 */
  content: string;
  /** 작성자 정보 */
  writer: CommentWriter;
  /** 작성 일시 */
  createdAt: string;
  /** 현재 사용자가 삭제 가능한지 */
  canDelete: boolean;
}

/**
 * 댓글 작성 요청
 */
export interface CommentCreateRequest {
  content: string;
}

// ========== 하위 호환성을 위한 타입 (deprecated) ==========

/**
 * @deprecated GalleryPostDTO를 사용하세요
 */
export type GalleryImageDTO = GalleryPostDTO;
