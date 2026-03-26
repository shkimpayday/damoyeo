/**
 * ============================================================================
 * 메시지 리스트 컴포넌트
 * ============================================================================
 *
 * [역할]
 * 메시지 배열을 스크롤 가능한 목록으로 렌더링합니다.
 *
 * [기능]
 * - 메시지 배열 렌더링
 * - 스마트 자동 스크롤
 *   → 내가 보낸 메시지: 무조건 아래로 스크롤
 *   → 다른 사람 메시지: 내가 아래에 있을 때만 스크롤
 * - 무한 스크롤 (위로 스크롤 시 이전 메시지 로드)
 * - 이전 메시지 로드 시 스크롤 위치 유지
 * - 빈 상태 표시
 *
 * @author damoyeo
 * @since 2025-02-25
 */

import { useEffect, useLayoutEffect, useRef, useCallback } from "react";
import { MessageItem } from "./message-item";
import { EmptyState } from "@/components/ui/empty-state";
import { Spinner } from "@/components/ui/spinner";
import { useAuthStore } from "@/features/auth/stores/auth-store";
import type { ChatMessageDTO } from "../types";

interface MessageListProps {
  messages: ChatMessageDTO[];
  onLoadPrevious?: () => void;
  hasPrevious?: boolean;
  isLoadingPrevious?: boolean;
}

export function MessageList({
  messages,
  onLoadPrevious,
  hasPrevious = false,
  isLoadingPrevious = false,
}: MessageListProps) {
  const containerRef = useRef<HTMLDivElement>(null);
  const { member } = useAuthStore();

  // 스크롤 위치 관리용
  const prevScrollHeightRef = useRef(0);
  const firstMessageIdRef = useRef<number | null>(null);  // 첫 번째 메시지 ID 추적

  /**
   * 사용자가 맨 아래에 있는지 확인
   */
  const isNearBottom = () => {
    if (!containerRef.current) return true;

    const { scrollTop, scrollHeight, clientHeight } = containerRef.current;
    return scrollHeight - scrollTop - clientHeight < 100;
  };

  /**
   * 사용자가 맨 위에 있는지 확인
   */
  const isNearTop = () => {
    if (!containerRef.current) return false;

    const { scrollTop } = containerRef.current;
    return scrollTop < 100;
  };

  /**
   * 스크롤 이벤트 핸들러
   * 상단에 도달하면 이전 메시지 로드
   */
  const handleScroll = useCallback(() => {
    if (!onLoadPrevious || !hasPrevious || isLoadingPrevious) {
      return;
    }
    if (isNearTop()) {
      // 로드 전 스크롤 높이 저장
      if (containerRef.current) {
        prevScrollHeightRef.current = containerRef.current.scrollHeight;
      }
      onLoadPrevious();
    }
  }, [onLoadPrevious, hasPrevious, isLoadingPrevious]);

  /**
   * 스크롤 이벤트 리스너 등록
   */
  useEffect(() => {
    const container = containerRef.current;
    if (!container) return;

    container.addEventListener("scroll", handleScroll);
    return () => {
      container.removeEventListener("scroll", handleScroll);
    };
  }, [handleScroll]);

  /**
   * 메시지 변경 시 스크롤 처리 (통합)
   * - 초기 로드: 맨 아래로
   * - 이전 메시지 로드 완료: 스크롤 위치 유지 (첫 번째 메시지 ID 변경으로 감지)
   * - 새 메시지 추가: 조건부 스크롤
   *
   * useLayoutEffect 사용 이유:
   * DOM 업데이트 직후 동기로 실행되어 스크롤 위치 조정이 깜빡임 없이 적용됨
   */
  useLayoutEffect(() => {
    if (!containerRef.current || messages.length === 0) return;

    const currentFirstId = messages[0]?.id;

    // 1. 초기 로드 → 맨 아래로
    if (firstMessageIdRef.current === null) {
      containerRef.current.scrollTop = containerRef.current.scrollHeight;
      firstMessageIdRef.current = currentFirstId;
      prevScrollHeightRef.current = containerRef.current.scrollHeight;
      return;
    }

    // 2. 이전 메시지 로드 완료 (첫 번째 메시지 ID가 바뀜 = 앞에 메시지 추가됨)
    if (currentFirstId !== firstMessageIdRef.current) {
      const newScrollHeight = containerRef.current.scrollHeight;
      const scrollDiff = newScrollHeight - prevScrollHeightRef.current;
      containerRef.current.scrollTop = containerRef.current.scrollTop + scrollDiff;
      firstMessageIdRef.current = currentFirstId;
      prevScrollHeightRef.current = newScrollHeight;
      return;
    }

    // 3. 새 메시지 추가 → 조건부 스크롤
    const lastMessage = messages[messages.length - 1];
    const isMyMessage = lastMessage.sender?.id === member?.id;

    if (isMyMessage || isNearBottom()) {
      containerRef.current.scrollTop = containerRef.current.scrollHeight;
    }
    prevScrollHeightRef.current = containerRef.current.scrollHeight;
  }, [messages, member?.id]);

  return (
    <div
      ref={containerRef}
      className="flex-1 overflow-y-auto px-4 py-4 space-y-1"
      style={{ overflowAnchor: "none" }}
    >
      {/* 메시지가 없을 때 */}
      {messages.length === 0 ? (
        <div className="flex h-full items-center justify-center">
          <EmptyState title="아직 메시지가 없습니다" icon="💬" />
        </div>
      ) : (
        <>
          {/* 이전 메시지 로딩 인디케이터 */}
          {isLoadingPrevious && (
            <div className="flex justify-center py-2">
              <Spinner size="sm" />
            </div>
          )}

          {/* 더 불러올 메시지가 있음을 표시 */}
          {hasPrevious && !isLoadingPrevious && (
            <div className="flex justify-center py-2">
              <button
                onClick={onLoadPrevious}
                className="text-sm text-gray-500 hover:text-gray-700"
              >
                이전 메시지 더 보기
              </button>
            </div>
          )}

          {/* 메시지 목록 */}
          {messages.map((message) => (
            <MessageItem key={message.id} message={message} />
          ))}
        </>
      )}
    </div>
  );
}
