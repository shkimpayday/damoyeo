/**
 * ============================================================================
 * 정모 채팅 메시지 TanStack Query Hooks
 * ============================================================================
 *
 * [역할]
 * 정모 참석자 전용 채팅 메시지 히스토리 조회를 TanStack Query로 관리합니다.
 *
 * [권한]
 * ATTENDING 상태의 참석자만 조회 가능합니다.
 *
 * @author damoyeo
 * @since 2025-03-16
 */

import { useInfiniteQuery } from "@tanstack/react-query";
import { getMeetingMessages } from "../api/chat-api";

/**
 * 정모 채팅 메시지 무한 스크롤 조회
 *
 * @param meetingId 정모 ID
 * @param size 페이지당 메시지 수 (기본 50)
 * @returns TanStack Infinite Query 결과
 */
export function useMeetingChatMessagesInfinite(meetingId: number, size: number = 50) {
  return useInfiniteQuery({
    queryKey: ["meeting-chat", "messages", "infinite", meetingId, size],
    queryFn: ({ pageParam }) => getMeetingMessages(meetingId, pageParam, size),
    initialPageParam: 1,
    getNextPageParam: (lastPage) => {
      if (lastPage.current < lastPage.totalPage) {
        return lastPage.current + 1;
      }
      return undefined;
    },
    staleTime: 0,
    gcTime: 5 * 60 * 1000,
    retry: 1,
  });
}
