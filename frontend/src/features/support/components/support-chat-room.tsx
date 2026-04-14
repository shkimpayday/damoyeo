/**
 * ============================================================================
 * 상담 채팅방 컴포넌트
 * ============================================================================
 *
 * [역할]
 * 실시간 상담 채팅방 UI를 렌더링합니다.
 * - 메시지 목록 표시
 * - 메시지 입력창
 * - 연결 상태 표시
 * - 상담 완료 후 평가
 *
 * @author damoyeo
 * @since 2025-03-16
 */

import {
  useEffect,
  useRef,
  useState,
  useCallback,
  type KeyboardEvent,
} from "react";
import { X, Send, Wifi, WifiOff, Star } from "lucide-react";
import { useSupportChat } from "../hooks/use-support-chat";
import { SupportMessageItem } from "./support-message-item";
import { useAuthStore } from "@/features/auth/stores/auth-store";
import type { SupportChatDTO } from "../types";
import { Spinner } from "@/components/ui/spinner";

interface SupportChatRoomProps {
  /**
   * 채팅방 닫기 핸들러
   */
  onClose: () => void;

  /**
   * 관리자 여부
   */
  isAdmin?: boolean;

  /**
   * 외부에서 지정된 상담 (관리자 대시보드용)
   */
  externalChat?: SupportChatDTO;
}

/**
 * 상담 채팅방
 */
export function SupportChatRoom({
  onClose,
  isAdmin = false,
  externalChat,
}: SupportChatRoomProps) {
  const { member } = useAuthStore();
  const {
    activeChat,
    messages,
    connectionStatus,
    typingUsers,
    isLoadingMessages,
    sendMessage,
    handleTyping,
    rateChat,
  } = useSupportChat({ isAdmin });

  const chat = externalChat ?? activeChat;
  const messagesEndRef = useRef<HTMLDivElement>(null);
  const [inputValue, setInputValue] = useState("");
  const [rating, setRating] = useState(0);
  const [showRating, setShowRating] = useState(false);


  // 자동 스크롤


  const scrollToBottom = useCallback(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: "smooth" });
  }, []);

  useEffect(() => {
    scrollToBottom();
  }, [messages, scrollToBottom]);

  // 상담 완료 시 평가 모달 표시
  useEffect(() => {
    if (
      chat?.status === "COMPLETED" &&
      !isAdmin &&
      !chat.rating
    ) {
      setShowRating(true);
    }
  }, [chat?.status, chat?.rating, isAdmin]);


  // 핸들러


  const handleSend = useCallback(() => {
    const trimmed = inputValue.trim();
    if (!trimmed) return;
    sendMessage(trimmed);
    setInputValue("");
  }, [inputValue, sendMessage]);

  const handleKeyDown = useCallback(
    (e: KeyboardEvent<HTMLTextAreaElement>) => {
      if (e.key === "Enter" && !e.shiftKey) {
        e.preventDefault();
        handleSend();
      }
    },
    [handleSend]
  );

  const handleInputChange = useCallback(
    (e: React.ChangeEvent<HTMLTextAreaElement>) => {
      setInputValue(e.target.value);
      handleTyping();
    },
    [handleTyping]
  );

  const handleRate = useCallback(() => {
    if (rating === 0) return;
    rateChat(rating);
    setShowRating(false);
  }, [rating, rateChat]);


  // 렌더링


  const isCompleted = chat?.status === "COMPLETED";
  const isWaiting = chat?.status === "WAITING";
  const isConnected = connectionStatus === "connected";

  return (
    <div className="flex flex-col h-full bg-white rounded-t-2xl shadow-2xl overflow-hidden">
      {/* 헤더 */}
      <div className="flex items-center justify-between px-4 py-3 bg-primary-600 text-white flex-shrink-0">
        <div className="flex items-center gap-2">
          {/* 연결 상태 아이콘 */}
          {isConnected ? (
            <Wifi size={16} className="text-green-300" />
          ) : (
            <WifiOff size={16} className="text-red-300 animate-pulse" />
          )}
          <div>
            <h3 className="font-semibold text-sm">
              {chat ? chat.title : "채팅 상담"}
            </h3>
            <p className="text-xs text-primary-200">
              {isWaiting
                ? "상담사 배정 대기 중..."
                : isCompleted
                  ? "상담 완료"
                  : chat?.admin
                    ? `상담사: ${chat.admin.nickname}`
                    : "연결 중..."}
            </p>
          </div>
        </div>
        <button
          onClick={onClose}
          className="p-1 hover:bg-primary-700 rounded-lg transition-colors"
          aria-label="닫기"
        >
          <X size={20} />
        </button>
      </div>

      {/* 메시지 목록 */}
      <div className="flex-1 overflow-y-auto p-4 bg-gray-50 space-y-1">
        {isLoadingMessages ? (
          <div className="flex justify-center items-center h-32">
            <Spinner size="md" />
          </div>
        ) : messages.length === 0 ? (
          <div className="flex flex-col items-center justify-center h-32 text-center">
            <p className="text-sm text-gray-500">
              {isWaiting
                ? "상담사가 곧 응답할 예정입니다."
                : "메시지를 입력해보세요."}
            </p>
          </div>
        ) : (
          <>
            {messages.map((msg) => (
              <SupportMessageItem
                key={msg.id}
                message={msg}
                isMine={msg.sender?.id === (isAdmin ? chat?.admin?.id : undefined) ||
                  msg.sender?.nickname === member?.nickname}
              />
            ))}
          </>
        )}

        {/* 타이핑 인디케이터 */}
        {typingUsers.length > 0 && (
          <div className="flex items-center gap-2 text-xs text-gray-500 py-1 px-2">
            <div className="flex gap-1">
              <span className="animate-bounce" style={{ animationDelay: "0ms" }}>•</span>
              <span className="animate-bounce" style={{ animationDelay: "150ms" }}>•</span>
              <span className="animate-bounce" style={{ animationDelay: "300ms" }}>•</span>
            </div>
            <span>
              {typingUsers[0].isAdmin ? "상담사" : typingUsers[0].email}님이 입력 중...
            </span>
          </div>
        )}

        <div ref={messagesEndRef} />
      </div>

      {/* 상담 완료 평가 모달 */}
      {showRating && (
        <div className="bg-yellow-50 border-t border-yellow-200 p-4">
          <p className="text-sm font-medium text-gray-700 mb-2">
            상담이 완료되었습니다. 만족도를 평가해주세요.
          </p>
          <div className="flex items-center gap-1 mb-3">
            {[1, 2, 3, 4, 5].map((star) => (
              <button
                key={star}
                onClick={() => setRating(star)}
                className="transition-colors"
              >
                <Star
                  size={28}
                  className={star <= rating ? "fill-yellow-400 text-yellow-400" : "text-gray-300"}
                />
              </button>
            ))}
          </div>
          <div className="flex gap-2">
            <button
              onClick={handleRate}
              disabled={rating === 0}
              className="flex-1 py-2 bg-primary-500 text-white rounded-lg text-sm font-medium disabled:opacity-50"
            >
              평가 제출
            </button>
            <button
              onClick={() => setShowRating(false)}
              className="px-4 py-2 bg-gray-100 text-gray-600 rounded-lg text-sm"
            >
              나중에
            </button>
          </div>
        </div>
      )}

      {/* 입력창 */}
      {!isCompleted && (
        <div className="px-3 py-3 bg-white border-t border-gray-100 flex gap-2 items-end flex-shrink-0">
          <textarea
            value={inputValue}
            onChange={handleInputChange}
            onKeyDown={handleKeyDown}
            placeholder={
              isWaiting && !isAdmin
                ? "상담사 배정 대기 중... 메시지를 남겨두세요."
                : "메시지를 입력하세요... (Enter: 전송, Shift+Enter: 줄바꿈)"
            }
            rows={1}
            className="flex-1 resize-none border border-gray-200 rounded-xl px-3 py-2 text-sm outline-none focus:border-primary-400 transition-colors max-h-24"
            style={{ minHeight: "40px" }}
          />
          <button
            onClick={handleSend}
            disabled={!inputValue.trim() || !isConnected}
            className="p-2.5 bg-primary-500 text-white rounded-xl disabled:opacity-40 transition-opacity flex-shrink-0"
            aria-label="전송"
          >
            <Send size={18} />
          </button>
        </div>
      )}
    </div>
  );
}
