/**
 * ============================================================================
 * 채팅 REST API 클라이언트
 * ============================================================================
 *
 * [역할]
 * 채팅 관련 REST API 호출 함수를 제공합니다.
 * (실시간 WebSocket 통신은 별도 hooks/use-websocket.ts에서 처리)
 *
 * [사용하는 API]
 * - GET /api/chat/{groupId}/messages - 메시지 히스토리 조회
 * - GET /api/chat/{groupId}/unread-count - 읽지 않은 메시지 개수
 * - POST /api/chat/{groupId}/read - 읽음 처리
 * - GET /api/chat/my-chats - 내 채팅방 목록
 *
 * @author damoyeo
 * @since 2025-02-25
 */

import { jwtAxios } from "@/lib/axios";
import { ENV } from "@/config";
import type { ChatMessageDTO, ChatRoomDTO, PageResponseDTO } from "../types";

const prefix = `${ENV.API_URL}/api/chat`;

/**
 * 메시지 히스토리 조회 (페이지네이션)
 *
 * [용도]
 * 채팅방 진입 시 최근 메시지를 로드합니다.
 * 무한 스크롤로 과거 메시지를 추가 로드할 수 있습니다.
 *
 * @param groupId 모임 ID
 * @param page 페이지 번호 (1부터 시작)
 * @param size 페이지당 메시지 수 (기본 50)
 * @returns 페이지네이션된 메시지 목록
 */
export const getMessages = async (
  groupId: number,
  page: number = 1,
  size: number = 50
): Promise<PageResponseDTO<ChatMessageDTO>> => {
  const res = await jwtAxios.get(`${prefix}/${groupId}/messages`, {
    params: { page, size },
  });
  return res.data;
};

/**
 * 읽지 않은 메시지 개수 조회
 *
 * [용도]
 * 채팅방 목록에서 읽지 않은 메시지 배지를 표시합니다.
 *
 * @param groupId 모임 ID
 * @returns 읽지 않은 메시지 개수
 */
export const getUnreadCount = async (groupId: number): Promise<number> => {
  const res = await jwtAxios.get<number>(`${prefix}/${groupId}/unread-count`);
  return res.data;
};

/**
 * 메시지 읽음 처리
 *
 * [용도]
 * 채팅방에서 메시지를 읽었을 때 호출합니다.
 * 마지막으로 읽은 메시지 ID를 업데이트하여 unread count를 감소시킵니다.
 *
 * @param groupId 모임 ID
 * @param lastReadMessageId 마지막으로 읽은 메시지 ID
 */
export const markAsRead = async (
  groupId: number,
  lastReadMessageId: number
): Promise<void> => {
  await jwtAxios.post(`${prefix}/${groupId}/read`, {
    lastReadMessageId,
  });
};

/**
 * 내 채팅방 목록 조회
 *
 * [용도]
 * 사용자가 속한 모든 모임의 채팅방 목록을 조회합니다.
 * 각 채팅방의 최신 메시지와 읽지 않은 메시지 개수를 포함합니다.
 *
 * @returns 내 채팅방 목록
 */
export const getMyChatRooms = async (): Promise<ChatRoomDTO[]> => {
  const res = await jwtAxios.get<ChatRoomDTO[]>(`${prefix}/my-chats`);
  return res.data;
};

// 정모 채팅 API (참석자 전용)

/**
 * 정모 메시지 히스토리 조회 (페이지네이션)
 *
 * [권한]
 * ATTENDING 상태의 참석자만 조회 가능합니다.
 *
 * @param meetingId 정모 ID
 * @param page 페이지 번호 (1부터 시작)
 * @param size 페이지당 메시지 수 (기본 50)
 * @returns 페이지네이션된 메시지 목록
 */
export const getMeetingMessages = async (
  meetingId: number,
  page: number = 1,
  size: number = 50
): Promise<PageResponseDTO<ChatMessageDTO>> => {
  const res = await jwtAxios.get(`${prefix}/meeting/${meetingId}/messages`, {
    params: { page, size },
  });
  return res.data;
};

/**
 * 정모 읽지 않은 메시지 개수 조회
 *
 * @param meetingId 정모 ID
 * @returns 읽지 않은 메시지 개수
 */
export const getMeetingUnreadCount = async (meetingId: number): Promise<number> => {
  const res = await jwtAxios.get<number>(`${prefix}/meeting/${meetingId}/unread-count`);
  return res.data;
};

/**
 * 정모 메시지 읽음 처리
 *
 * @param meetingId 정모 ID
 * @param lastReadMessageId 마지막으로 읽은 메시지 ID
 */
export const markMeetingAsRead = async (
  meetingId: number,
  lastReadMessageId: number
): Promise<void> => {
  await jwtAxios.post(`${prefix}/meeting/${meetingId}/read`, {
    lastReadMessageId,
  });
};
