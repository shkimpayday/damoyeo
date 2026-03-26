/**
 * ============================================================================
 * 채팅 기능 TypeScript 타입 정의
 * ============================================================================
 *
 * [역할]
 * 채팅 관련 모든 타입을 정의합니다.
 * 백엔드 DTO와 일치하도록 작성되었습니다.
 *
 * @author damoyeo
 * @since 2025-02-25
 */

import type { MemberSummary } from "@/features/auth/types";

// ========================================================================
// 메시지 타입
// ========================================================================

/**
 * 메시지 타입
 *
 * - TEXT: 일반 텍스트 메시지
 * - IMAGE: 이미지 메시지
 * - SYSTEM: 시스템 메시지 (입장/퇴장 알림)
 */
export type MessageType = "TEXT" | "IMAGE" | "SYSTEM";

/**
 * 채팅 메시지 DTO
 *
 * [백엔드 DTO와 일치]
 * ChatMessageDTO.java
 */
export interface ChatMessageDTO {
  /**
   * 메시지 ID (PK)
   */
  id: number;

  /**
   * 소속 모임 ID
   *
   * 정모 채팅인 경우 null/undefined입니다.
   */
  groupId?: number;

  /**
   * 소속 정모 ID
   *
   * 모임 채팅인 경우 null/undefined입니다.
   */
  meetingId?: number;

  /**
   * 발신자 정보
   *
   * SYSTEM 메시지인 경우 null입니다.
   */
  sender: MemberSummary | null;

  /**
   * 메시지 내용
   *
   * 최대 2000자
   */
  message: string;

  /**
   * 메시지 타입
   */
  messageType: MessageType;

  /**
   * 이미지 URL (messageType이 IMAGE일 때 사용)
   */
  imageUrl?: string;

  /**
   * 메시지 전송 시각
   *
   * ISO 8601 형식: "2025-02-25T10:30:00"
   */
  createdAt: string;
}

/**
 * 메시지 전송 요청
 *
 * [백엔드 DTO와 일치]
 * SendMessageRequest.java
 */
export interface SendMessageRequest {
  /**
   * 메시지 내용
   *
   * 최소 1자, 최대 2000자
   */
  message: string;
}

// ========================================================================
// 채팅방 타입
// ========================================================================

/**
 * 채팅방 정보 DTO
 *
 * [용도]
 * 채팅방 목록 화면에서 사용됩니다.
 *
 * [백엔드 DTO와 일치]
 * ChatRoomDTO.java
 */
export interface ChatRoomDTO {
  /**
   * 모임 ID
   */
  groupId: number;

  /**
   * 모임 이름
   */
  groupName: string;

  /**
   * 모임 대표 이미지 URL
   */
  groupImage?: string;

  /**
   * 최신 메시지
   *
   * 아직 메시지가 없으면 undefined
   */
  latestMessage?: ChatMessageDTO;

  /**
   * 읽지 않은 메시지 개수
   */
  unreadCount: number;
}

// ========================================================================
// WebSocket 연결 상태
// ========================================================================

/**
 * WebSocket 연결 상태
 *
 * - connecting: 연결 중
 * - connected: 연결됨 (정상)
 * - disconnected: 연결 해제됨
 * - error: 에러 발생
 */
export type ConnectionStatus =
  | "connecting"
  | "connected"
  | "disconnected"
  | "error";

/**
 * 타이핑 이벤트
 *
 * 다른 사용자가 메시지를 입력 중일 때 수신하는 이벤트입니다.
 */
export interface TypingEvent {
  /**
   * 타이핑 중인 사용자 이메일
   */
  email: string;

  /**
   * 타이핑 중 여부
   */
  typing: boolean;
}

// ========================================================================
// 페이지네이션 (공통)
// ========================================================================

/**
 * 페이지 응답 DTO
 *
 * [백엔드 DTO와 일치]
 * PageResponseDTO.java
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
