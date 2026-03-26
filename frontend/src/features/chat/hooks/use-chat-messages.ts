/**
 * ============================================================================
 * 채팅 메시지 TanStack Query Hooks
 * ============================================================================
 *
 * [역할]
 * 채팅 메시지 히스토리 조회를 TanStack Query로 관리합니다.
 *
 * [사용 위치]
 * - hooks/use-chat-room.ts: 초기 메시지 히스토리 로드
 * - components/message-list.tsx: 무한 스크롤로 과거 메시지 로드
 *
 * @author damoyeo
 * @since 2025-02-25
 */

import { useInfiniteQuery } from "@tanstack/react-query";
import { getMessages } from "../api/chat-api";

/**
 * 채팅 메시지 무한 스크롤 조회
 *
 * [용도]
 * 채팅방 진입 시 최근 메시지를 로드하고,
 * 스크롤 상단에서 이전 메시지를 추가 로드합니다.
 *
 * [페이지네이션 방식]
 * - 백엔드는 최신순(내림차순)으로 반환
 * - page=1: 가장 최신 메시지
 * - page=2: 그 이전 메시지
 * - 위로 스크롤 → 다음 페이지(더 오래된 메시지) 로드
 *
 * [캐싱 전략]
 * - staleTime: 0 (항상 최신 데이터로 취급)
 * - gcTime: 5분 (5분간 캐시 유지)
 *
 * [사용 예시]
 * ```typescript
 * const {
 *   data,
 *   fetchNextPage,
 *   hasNextPage,
 *   isFetchingNextPage,
 *   isLoading,
 *   error
 * } = useChatMessagesInfinite(groupId);
 *
 * // 첫 페이지 메시지 (가장 최신)
 * const firstPageMessages = data?.pages[0]?.dtoList || [];
 *
 * // 모든 페이지 메시지 합치기 (오래된 순)
 * const allMessages = data?.pages
 *   .flatMap(page => page.dtoList)
 *   .reverse() || [];
 * ```
 *
 * @param groupId 모임 ID
 * @param size 페이지당 메시지 수 (기본 50)
 * @returns TanStack Infinite Query 결과
 */
export function useChatMessagesInfinite(groupId: number, size: number = 50) {
  return useInfiniteQuery({
    queryKey: ["chat", "messages", "infinite", groupId, size],
    queryFn: ({ pageParam }) => getMessages(groupId, pageParam, size),
    initialPageParam: 1,
    getNextPageParam: (lastPage) => {
      // 더 로드할 페이지가 있는지 확인
      // 백엔드 next 값이 부정확할 수 있으므로 current < totalPage로 판단
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
