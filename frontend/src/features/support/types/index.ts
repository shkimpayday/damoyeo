/**
 * ============================================================================
 * 상담 채팅 TypeScript 타입 정의
 * ============================================================================
 *
 * [역할]
 * 상담 관련 모든 타입을 정의합니다.
 * 백엔드 DTO와 일치하도록 작성되었습니다.
 *
 * @author damoyeo
 * @since 2025-03-16
 */

import type { MemberSummary } from "@/features/auth/types";

// ========================================================================
// 상담 상태
// ========================================================================

/**
 * 상담 상태
 *
 * - WAITING: 대기 중 (관리자 배정 대기)
 * - IN_PROGRESS: 진행 중
 * - COMPLETED: 완료
 */
export type SupportChatStatus = "WAITING" | "IN_PROGRESS" | "COMPLETED";

// ========================================================================
// 상담 채팅 타입
// ========================================================================

/**
 * 상담 채팅 DTO
 *
 * [백엔드 DTO와 일치]
 * SupportChatDTO.java
 */
export interface SupportChatDTO {
  /**
   * 상담 채팅 ID
   */
  id: number;

  /**
   * 상담 요청 사용자
   */
  user: MemberSummary;

  /**
   * 담당 관리자 (null 가능)
   */
  admin: MemberSummary | null;

  /**
   * 상담 제목
   */
  title: string;

  /**
   * 상담 상태
   */
  status: SupportChatStatus;

  /**
   * 최신 메시지 (미리보기용)
   */
  latestMessage?: SupportMessageDTO;

  /**
   * 읽지 않은 메시지 개수
   */
  unreadCount: number;

  /**
   * 상담 생성 시각
   */
  createdAt: string;

  /**
   * 상담 종료 시각
   */
  completedAt?: string;

  /**
   * 만족도 평가 (1-5)
   */
  rating?: number;
}

/**
 * 상담 메시지 DTO
 *
 * [백엔드 DTO와 일치]
 * SupportMessageDTO.java
 */
export interface SupportMessageDTO {
  /**
   * 메시지 ID
   */
  id: number;

  /**
   * 소속 상담 채팅 ID
   */
  supportChatId: number;

  /**
   * 발신자 정보
   */
  sender: MemberSummary;

  /**
   * 메시지 내용
   */
  message: string;

  /**
   * 관리자 메시지 여부
   */
  isAdmin: boolean;

  /**
   * 전송 시각
   */
  createdAt: string;
}

// ========================================================================
// 요청 타입
// ========================================================================

/**
 * 상담 생성 요청
 */
export interface CreateSupportChatRequest {
  /**
   * 상담 제목
   */
  title: string;

  /**
   * 첫 메시지 내용
   */
  message: string;
}

/**
 * 메시지 전송 요청
 */
export interface SendSupportMessageRequest {
  /**
   * 메시지 내용
   */
  message: string;
}

// ========================================================================
// WebSocket 타입
// ========================================================================

/**
 * WebSocket 연결 상태
 */
export type SupportConnectionStatus =
  | "connecting"
  | "connected"
  | "disconnected"
  | "error";

/**
 * 타이핑 이벤트
 */
export interface SupportTypingEvent {
  /**
   * 타이핑 중인 사용자 이메일
   */
  email: string;

  /**
   * 타이핑 중 여부
   */
  typing: boolean;

  /**
   * 관리자 여부
   */
  isAdmin: boolean;
}

/**
 * 상담 이벤트 타입 (WebSocket으로 수신)
 */
export type SupportEventType = "ADMIN_ASSIGNED" | "CHAT_COMPLETED";

/**
 * 상담 이벤트 메시지
 */
export interface SupportEvent {
  type: SupportEventType;
  chat: SupportChatDTO;
}

// ========================================================================
// 페이지네이션
// ========================================================================

/**
 * 페이지 응답 DTO
 */
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
