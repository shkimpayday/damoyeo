/**
 * ============================================================================
 * 상담 플로팅 버튼 컴포넌트
 * ============================================================================
 *
 * [역할]
 * 화면 하단 우측에 고정 표시되는 상담 버튼입니다.
 * - 클릭 시 상담 채팅창 팝업
 * - 활성 상담이 있으면 바로 채팅방으로 이동
 * - 없으면 새 상담 시작 폼 표시
 *
 * [주의] WebSocket 연결은 이 컴포넌트에서 직접 하지 않습니다.
 * WebSocket은 SupportChatRoom이 마운트될 때 단 1개만 생성됩니다.
 * 만약 여기서 useSupportChat()을 호출하면 WebSocket이 2개 생성되어
 * 메시지가 중복 수신됩니다.
 *
 * @author damoyeo
 * @since 2025-03-16
 */

import { useState, useCallback, useEffect } from "react";
import { MessageCircle } from "lucide-react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useSupportStore } from "../stores/support-store";
import { SupportChatRoom } from "./support-chat-room";
import { CreateSupportChatModal } from "./create-support-chat-modal";
import { useAuthStore } from "@/features/auth/stores/auth-store";
import * as supportApi from "../api/support-api";
import type { CreateSupportChatRequest } from "../types";

/**
 * 상담 플로팅 버튼 + 팝업 컨테이너
 *
 * WebSocket 연결을 하지 않고 Zustand store만 사용합니다.
 * WebSocket은 SupportChatRoom 내부에서만 생성합니다.
 */
export function SupportFloatingButton() {
  const { member } = useAuthStore();
  const queryClient = useQueryClient();
  const { activeChat, isOpen, openChat, closeChat, setActiveChat } =
    useSupportStore();

  /**
   * 팝업 표시 여부 (버튼에서 제어)
   */
  const [showPopup, setShowPopup] = useState(false);

  /**
   * 새 상담 폼 표시 여부
   */
  const [showCreateForm, setShowCreateForm] = useState(false);

  /**
   * 활성 상담 조회 (비로그인 시 비활성화)
   *
   * SupportFloatingButton에서 한 번만 조회하고, 결과를 store에 동기화합니다.
   * useSupportChat 훅을 사용하지 않으므로 여기서 직접 쿼리합니다.
   */
  const isLoggedIn = !!member?.email;
  const { data: activeChatData } = useQuery({
    queryKey: ["support", "active"],
    queryFn: () => supportApi.getActiveSupportChat(),
    enabled: isLoggedIn,
    staleTime: 30 * 1000,
  });

  // 활성 상담을 store에 동기화
  useEffect(() => {
    if (activeChatData !== undefined) {
      setActiveChat(activeChatData);
    }
  }, [activeChatData, setActiveChat]);

  /**
   * 상담 생성 mutation (WebSocket 연결 없이 REST API만 사용)
   */
  const createChatMutation = useMutation({
    mutationFn: (request: CreateSupportChatRequest) =>
      supportApi.createSupportChat(request),
    onSuccess: (newChat) => {
      setActiveChat(newChat);
      queryClient.invalidateQueries({ queryKey: ["support"] });
    },
  });

  // isOpen 상태와 팝업 동기화
  useEffect(() => {
    if (!isOpen) {
      setShowPopup(false);
      setShowCreateForm(false);
    }
  }, [isOpen]);

  const handleButtonClick = useCallback(() => {
    if (showPopup) {
      setShowPopup(false);
      closeChat();
      return;
    }

    if (activeChat && activeChat.status !== "COMPLETED") {
      // 진행 중인 상담이 있으면 채팅방으로
      openChat();
      setShowPopup(true);
      setShowCreateForm(false);
    } else {
      // 새 상담 시작
      setShowPopup(true);
      setShowCreateForm(true);
    }
  }, [showPopup, activeChat, openChat, closeChat]);

  const handleClosePopup = useCallback(() => {
    setShowPopup(false);
    closeChat();
    setShowCreateForm(false);
  }, [closeChat]);

  /**
   * 새 상담 시작 핸들러
   *
   * useSupportChat의 startNewChat 대신 직접 mutation 호출.
   * WebSocket 연결 없이 REST API로만 상담을 생성합니다.
   */
  const handleStartChat = useCallback(
    async (title: string, message: string) => {
      await createChatMutation.mutateAsync({ title, message });
      setShowCreateForm(false);
      openChat();
    },
    [createChatMutation, openChat]
  );

  // 로그인한 사용자만 표시
  if (!isLoggedIn) {
    return null;
  }

  return (
    <>
      {/* 팝업 오버레이 */}
      {showPopup && (
        <>
          {/* 배경 클릭 닫기 */}
          <div
            className="fixed inset-0 bg-black/20 z-40"
            onClick={handleClosePopup}
          />

          {/* 팝업 채팅창 */}
          <div
            className="fixed bottom-20 right-4 w-80 h-[500px] z-50 rounded-2xl shadow-2xl overflow-hidden"
            onClick={(e) => e.stopPropagation()}
          >
            {showCreateForm || !activeChat ? (
              <CreateSupportChatModal
                onClose={handleClosePopup}
                onSubmit={handleStartChat}
                isLoading={createChatMutation.isPending}
              />
            ) : (
              <SupportChatRoom onClose={handleClosePopup} />
            )}
          </div>
        </>
      )}

      {/* 플로팅 버튼 */}
      <button
        onClick={handleButtonClick}
        className="fixed bottom-20 right-4 w-14 h-14 bg-primary-500 text-white rounded-full shadow-lg hover:bg-primary-600 transition-all duration-200 z-30 flex items-center justify-center hover:scale-105 active:scale-95"
        aria-label="상담 채팅"
        style={{ bottom: showPopup ? "calc(500px + 5rem + 16px)" : undefined }}
      >
        <MessageCircle size={24} />

        {/* 미읽은 메시지 배지 */}
        {activeChat && activeChat.unreadCount > 0 && (
          <span className="absolute -top-1 -right-1 bg-red-500 text-white text-xs w-5 h-5 rounded-full flex items-center justify-center font-bold">
            {activeChat.unreadCount > 9 ? "9+" : activeChat.unreadCount}
          </span>
        )}
      </button>
    </>
  );
}
