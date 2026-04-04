/**
 * ============================================================================
 * 관리자 상담 대시보드 페이지
 * ============================================================================
 *
 * [역할]
 * 관리자가 상담 목록을 관리하고 실시간 채팅을 진행하는 페이지입니다.
 *
 * [기능]
 * - 대기 중인 상담 목록 조회
 * - 내가 담당 중인 상담 목록 조회
 * - 상담 배정 (클릭 한 번으로)
 * - 실시간 채팅 응답
 * - 상담 완료 처리
 *
 * @author damoyeo
 * @since 2025-03-16
 */

import { useState, useCallback } from "react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import {
  MessageCircle,
  Clock,
  CheckCircle2,
  User,
  RefreshCw,
  ChevronRight,
} from "lucide-react";
import { Spinner } from "@/components/ui/spinner";
import { Avatar } from "@/components/ui/avatar";
import { SupportChatRoom } from "@/features/support/components/support-chat-room";
import * as supportApi from "@/features/support/api/support-api";
import type { SupportChatDTO } from "@/features/support/types";
import { useSupportStore } from "@/features/support/stores/support-store";
import { formatDateTime, getRelativeTime } from "@/utils/date";

type TabType = "waiting" | "my-chats" | "all";

/**
 * 상담 상태 배지
 */
function StatusBadge({ status }: { status: string }) {
  const config = {
    WAITING: { label: "대기 중", className: "bg-yellow-100 text-yellow-700" },
    IN_PROGRESS: {
      label: "진행 중",
      className: "bg-green-100 text-green-700",
    },
    COMPLETED: { label: "완료", className: "bg-gray-100 text-gray-500" },
  }[status] ?? { label: status, className: "bg-gray-100 text-gray-500" };

  return (
    <span
      className={`px-2 py-0.5 rounded-full text-xs font-medium ${config.className}`}
    >
      {config.label}
    </span>
  );
}

/**
 * 상담 목록 아이템
 */
function SupportChatItem({
  chat,
  onSelect,
  onAssign,
  isSelected,
}: {
  chat: SupportChatDTO;
  onSelect: (chat: SupportChatDTO) => void;
  onAssign: (chatId: number) => void;
  isSelected: boolean;
}) {
  return (
    <div
      className={`p-4 border-b border-gray-100 cursor-pointer transition-colors ${
        isSelected ? "bg-primary-50 border-l-4 border-l-primary-500" : "hover:bg-gray-50"
      }`}
      onClick={() => onSelect(chat)}
    >
      <div className="flex items-start justify-between gap-2">
        <div className="flex items-start gap-3 flex-1 min-w-0">
          <Avatar
            src={chat.user?.profileImage}
            alt={chat.user?.nickname}
            size="sm"
          />
          <div className="flex-1 min-w-0">
            <div className="flex items-center gap-2 mb-0.5">
              <span className="font-medium text-sm text-gray-900 truncate">
                {chat.user?.nickname}
              </span>
              <StatusBadge status={chat.status} />
            </div>
            <p className="text-sm text-gray-700 truncate font-medium">
              {chat.title}
            </p>
            {chat.latestMessage && (
              <p className="text-xs text-gray-500 truncate mt-0.5">
                {chat.latestMessage.message}
              </p>
            )}
            <div className="flex items-center gap-2 mt-1">
              {chat.admin && (
                <span className="text-xs text-blue-500">
                  담당: {chat.admin.nickname}
                </span>
              )}
              <span className="text-xs text-gray-400">
                {getRelativeTime(chat.createdAt)}
              </span>
            </div>
          </div>
        </div>

        <div className="flex items-center gap-1 flex-shrink-0">
          {/* 배정 버튼 */}
          {chat.status === "WAITING" && (
            <button
              onClick={(e) => {
                e.stopPropagation();
                onAssign(chat.id);
              }}
              className="px-2.5 py-1 bg-primary-500 text-white text-xs rounded-lg hover:bg-primary-600 transition-colors font-medium"
            >
              배정받기
            </button>
          )}
          <ChevronRight size={16} className="text-gray-400" />
        </div>
      </div>
    </div>
  );
}

/**
 * 관리자 상담 대시보드 페이지
 */
export default function AdminSupportPage() {
  const queryClient = useQueryClient();
  const { setActiveChat, openChat } = useSupportStore();
  const [activeTab, setActiveTab] = useState<TabType>("waiting");
  const [selectedChat, setSelectedChat] = useState<SupportChatDTO | null>(null);
  const [page, setPage] = useState(1);

  // ========================================================================
  // Queries
  // ========================================================================

  const { data: waitingData, isLoading: isLoadingWaiting } = useQuery({
    queryKey: ["support", "admin", "waiting", page],
    queryFn: () => supportApi.getWaitingSupportChats(page, 20),
    refetchInterval: 30 * 1000,
  });

  const { data: myChatsData, isLoading: isLoadingMyChats } = useQuery({
    queryKey: ["support", "admin", "my-chats", page],
    queryFn: () => supportApi.getMyAssignedChats(page, 20),
    refetchInterval: 30 * 1000,
  });

  const { data: allChatsData, isLoading: isLoadingAll } = useQuery({
    queryKey: ["support", "admin", "all", page],
    queryFn: () => supportApi.getAllSupportChats(page, 20),
    refetchInterval: 30 * 1000,
  });

  const { data: waitingCount } = useQuery({
    queryKey: ["support", "admin", "waiting-count"],
    queryFn: () => supportApi.getWaitingCount(),
    refetchInterval: 30 * 1000,
  });

  // ========================================================================
  // Mutations
  // ========================================================================

  const assignMutation = useMutation({
    mutationFn: (chatId: number) => supportApi.assignSupportChat(chatId),
    onSuccess: (updatedChat) => {
      queryClient.invalidateQueries({ queryKey: ["support", "admin"] });
      setSelectedChat(updatedChat);
      setActiveChat(updatedChat);
      openChat();
      setActiveTab("my-chats");
    },
  });

  const completeMutation = useMutation({
    mutationFn: (chatId: number) => supportApi.completeSupportChat(chatId),
    onSuccess: (updatedChat) => {
      queryClient.invalidateQueries({ queryKey: ["support", "admin"] });
      setSelectedChat(updatedChat);
    },
  });

  // ========================================================================
  // 핸들러
  // ========================================================================

  const handleSelectChat = useCallback(
    (chat: SupportChatDTO) => {
      setSelectedChat(chat);
      setActiveChat(chat);
      openChat();
    },
    [setActiveChat, openChat]
  );

  const handleAssign = useCallback(
    (chatId: number) => {
      assignMutation.mutate(chatId);
    },
    [assignMutation]
  );

  const handleComplete = useCallback(() => {
    if (!selectedChat) return;
    completeMutation.mutate(selectedChat.id);
  }, [selectedChat, completeMutation]);

  const handleRefresh = useCallback(() => {
    queryClient.invalidateQueries({ queryKey: ["support", "admin"] });
  }, [queryClient]);

  // ========================================================================
  // 현재 탭 데이터
  // ========================================================================

  const currentData =
    activeTab === "waiting"
      ? waitingData
      : activeTab === "my-chats"
        ? myChatsData
        : allChatsData;

  const isLoading =
    activeTab === "waiting"
      ? isLoadingWaiting
      : activeTab === "my-chats"
        ? isLoadingMyChats
        : isLoadingAll;

  // ========================================================================
  // 렌더링
  // ========================================================================

  return (
    <div className="flex h-full overflow-hidden">
      {/* 왼쪽: 상담 목록 */}
      <div className="w-96 border-r border-gray-200 flex flex-col bg-white">
        {/* 헤더 */}
        <div className="px-4 py-3 border-b border-gray-200">
          <div className="flex items-center justify-between mb-3">
            <h2 className="text-base font-bold text-gray-900 flex items-center gap-2">
              <MessageCircle size={18} className="text-primary-500" />
              채팅 상담 관리
            </h2>
            <button
              onClick={handleRefresh}
              className="p-1.5 hover:bg-gray-100 rounded-lg transition-colors"
              title="새로고침"
            >
              <RefreshCw size={16} className="text-gray-500" />
            </button>
          </div>

          {/* 탭 */}
          <div className="flex gap-1 bg-gray-100 rounded-lg p-1">
            {(
              [
                {
                  key: "waiting",
                  label: "대기 중",
                  icon: <Clock size={14} />,
                  count: waitingCount,
                },
                {
                  key: "my-chats",
                  label: "내 상담",
                  icon: <User size={14} />,
                  count: myChatsData?.totalCount,
                },
                {
                  key: "all",
                  label: "전체",
                  icon: <CheckCircle2 size={14} />,
                  count: allChatsData?.totalCount,
                },
              ] as const
            ).map((tab) => (
              <button
                key={tab.key}
                onClick={() => {
                  setActiveTab(tab.key as TabType);
                  setPage(1);
                }}
                className={`flex-1 flex items-center justify-center gap-1.5 py-1.5 text-xs font-medium rounded-md transition-colors ${
                  activeTab === tab.key
                    ? "bg-white text-primary-600 shadow-sm"
                    : "text-gray-500 hover:text-gray-700"
                }`}
              >
                {tab.icon}
                {tab.label}
                {tab.count !== undefined && tab.count > 0 && (
                  <span
                    className={`min-w-[18px] h-[18px] px-1 rounded-full text-xs font-bold flex items-center justify-center ${
                      activeTab === tab.key
                        ? "bg-primary-500 text-white"
                        : "bg-gray-300 text-gray-600"
                    }`}
                  >
                    {tab.count > 99 ? "99+" : tab.count}
                  </span>
                )}
              </button>
            ))}
          </div>
        </div>

        {/* 목록 */}
        <div className="flex-1 overflow-y-auto">
          {isLoading ? (
            <div className="flex justify-center items-center h-32">
              <Spinner size="md" />
            </div>
          ) : !currentData?.dtoList?.length ? (
            <div className="flex flex-col items-center justify-center h-32 text-gray-400">
              <MessageCircle size={32} className="mb-2 opacity-30" />
              <p className="text-sm">상담이 없습니다</p>
            </div>
          ) : (
            <>
              {currentData.dtoList.map((chat) => (
                <SupportChatItem
                  key={chat.id}
                  chat={chat}
                  onSelect={handleSelectChat}
                  onAssign={handleAssign}
                  isSelected={selectedChat?.id === chat.id}
                />
              ))}

              {/* 페이지네이션 */}
              {currentData.totalPage > 1 && (
                <div className="flex justify-center gap-2 py-3">
                  <button
                    onClick={() => setPage((p) => Math.max(1, p - 1))}
                    disabled={!currentData.prev}
                    className="px-3 py-1 text-xs border rounded-lg disabled:opacity-40"
                  >
                    이전
                  </button>
                  <span className="text-xs text-gray-500 self-center">
                    {page} / {currentData.totalPage}
                  </span>
                  <button
                    onClick={() =>
                      setPage((p) =>
                        Math.min(currentData.totalPage, p + 1)
                      )
                    }
                    disabled={!currentData.next}
                    className="px-3 py-1 text-xs border rounded-lg disabled:opacity-40"
                  >
                    다음
                  </button>
                </div>
              )}
            </>
          )}
        </div>
      </div>

      {/* 오른쪽: 채팅창 */}
      <div className="flex-1 flex flex-col">
        {selectedChat ? (
          <>
            {/* 상담 정보 헤더 */}
            <div className="px-4 py-3 border-b border-gray-200 bg-white flex items-center justify-between">
              <div className="flex items-center gap-3">
                <Avatar
                  src={selectedChat.user?.profileImage}
                  alt={selectedChat.user?.nickname}
                  size="sm"
                />
                <div>
                  <p className="font-medium text-sm text-gray-900">
                    {selectedChat.user?.nickname}
                  </p>
                  <p className="text-xs text-gray-500">
                    {formatDateTime(selectedChat.createdAt)} 상담 시작
                  </p>
                </div>
              </div>

              <div className="flex items-center gap-2">
                <StatusBadge status={selectedChat.status} />
                {selectedChat.status === "IN_PROGRESS" && (
                  <button
                    onClick={handleComplete}
                    disabled={completeMutation.isPending}
                    className="px-3 py-1.5 bg-green-500 text-white text-xs rounded-lg hover:bg-green-600 transition-colors font-medium disabled:opacity-50"
                  >
                    {completeMutation.isPending ? "처리 중..." : "상담 완료"}
                  </button>
                )}
              </div>
            </div>

            {/* 채팅방 */}
            <div className="flex-1 overflow-hidden">
              <SupportChatRoom
                onClose={() => setSelectedChat(null)}
                isAdmin={true}
                externalChat={selectedChat}
              />
            </div>
          </>
        ) : (
          <div className="flex-1 flex flex-col items-center justify-center text-gray-400">
            <MessageCircle size={48} className="mb-3 opacity-20" />
            <p className="text-sm font-medium">상담을 선택하세요</p>
            <p className="text-xs mt-1">
              왼쪽 목록에서 상담을 선택하면 채팅을 시작할 수 있습니다
            </p>
          </div>
        )}
      </div>
    </div>
  );
}
