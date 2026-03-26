/**
 * ============================================================================
 * 상담 시작 모달 컴포넌트
 * ============================================================================
 *
 * [역할]
 * 새 상담을 시작할 때 제목과 첫 메시지를 입력하는 모달입니다.
 *
 * @author damoyeo
 * @since 2025-03-16
 */

import { useState, type FormEvent } from "react";
import { X, MessageCircle } from "lucide-react";
import { Spinner } from "@/components/ui/spinner";

interface CreateSupportChatModalProps {
  /**
   * 모달 닫기 핸들러
   */
  onClose: () => void;

  /**
   * 상담 시작 핸들러
   */
  onSubmit: (title: string, message: string) => Promise<void>;

  /**
   * 로딩 상태
   */
  isLoading?: boolean;
}

/**
 * 상담 시작 모달
 */
export function CreateSupportChatModal({
  onClose,
  onSubmit,
  isLoading = false,
}: CreateSupportChatModalProps) {
  const [title, setTitle] = useState("");
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setError("");

    if (!title.trim()) {
      setError("문의 제목을 입력해주세요.");
      return;
    }
    if (!message.trim()) {
      setError("문의 내용을 입력해주세요.");
      return;
    }

    try {
      await onSubmit(title.trim(), message.trim());
    } catch {
      setError("상담 시작에 실패했습니다. 잠시 후 다시 시도해주세요.");
    }
  };

  return (
    <div className="flex flex-col h-full bg-white rounded-t-2xl shadow-2xl overflow-hidden">
      {/* 헤더 */}
      <div className="flex items-center justify-between px-4 py-3 bg-primary-600 text-white">
        <div className="flex items-center gap-2">
          <MessageCircle size={20} />
          <h3 className="font-semibold text-sm">새 상담 시작</h3>
        </div>
        <button
          onClick={onClose}
          className="p-1 hover:bg-primary-700 rounded-lg transition-colors"
        >
          <X size={20} />
        </button>
      </div>

      {/* 폼 */}
      <form onSubmit={handleSubmit} className="flex-1 flex flex-col p-4 gap-4">
        <div className="bg-blue-50 rounded-xl p-3">
          <p className="text-xs text-blue-700">
            💬 상담사가 배정되면 실시간 채팅으로 연결됩니다.
            일반적으로 업무 시간(09:00-18:00) 내에 응답합니다.
          </p>
        </div>

        {/* 제목 */}
        <div className="flex flex-col gap-1.5">
          <label className="text-sm font-medium text-gray-700">
            문의 제목 <span className="text-red-500">*</span>
          </label>
          <input
            type="text"
            value={title}
            onChange={(e) => setTitle(e.target.value)}
            placeholder="예: 결제 관련 문의, 모임 가입 문제"
            maxLength={200}
            className="border border-gray-200 rounded-xl px-3 py-2 text-sm outline-none focus:border-primary-400 transition-colors"
          />
        </div>

        {/* 내용 */}
        <div className="flex flex-col gap-1.5 flex-1">
          <label className="text-sm font-medium text-gray-700">
            문의 내용 <span className="text-red-500">*</span>
          </label>
          <textarea
            value={message}
            onChange={(e) => setMessage(e.target.value)}
            placeholder="문의하실 내용을 자세히 입력해주세요."
            maxLength={2000}
            className="flex-1 border border-gray-200 rounded-xl px-3 py-2 text-sm outline-none focus:border-primary-400 transition-colors resize-none min-h-[120px]"
          />
          <span className="text-xs text-gray-400 text-right">
            {message.length}/2000
          </span>
        </div>

        {/* 에러 메시지 */}
        {error && (
          <p className="text-xs text-red-500 bg-red-50 px-3 py-2 rounded-lg">
            {error}
          </p>
        )}

        {/* 버튼 */}
        <button
          type="submit"
          disabled={isLoading}
          className="py-3 bg-primary-500 text-white rounded-xl font-medium text-sm disabled:opacity-50 flex items-center justify-center gap-2"
        >
          {isLoading ? (
            <>
              <Spinner size="sm" />
              <span>상담 시작 중...</span>
            </>
          ) : (
            "상담 시작하기"
          )}
        </button>
      </form>
    </div>
  );
}
