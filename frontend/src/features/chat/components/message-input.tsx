/**
 * ============================================================================
 * 메시지 입력 컴포넌트
 * ============================================================================
 *
 * [역할]
 * 메시지 입력 및 전송 UI를 제공합니다.
 *
 * [기능]
 * - Textarea (자동 높이 조절)
 * - Enter 키로 전송 (Shift+Enter는 줄바꿈)
 * - 빈 메시지 전송 방지
 * - 연결 해제 시 비활성화
 *
 * @author damoyeo
 * @since 2025-02-25
 */

import { useState, useRef, useCallback, type KeyboardEvent, type ChangeEvent } from "react";

/** 메시지 최대 길이 (백엔드와 동기화) */
const MAX_MESSAGE_LENGTH = 2000;

interface MessageInputProps {
  onSendMessage: (message: string) => void;
  onTyping?: (typing: boolean) => void;
  disabled?: boolean;
}

export function MessageInput({
  onSendMessage,
  onTyping,
  disabled = false,
}: MessageInputProps) {
  const [message, setMessage] = useState("");
  const textareaRef = useRef<HTMLTextAreaElement>(null);
  const typingTimeoutRef = useRef<ReturnType<typeof setTimeout> | null>(null);

  /**
   * 메시지 전송 처리
   *
   * [검증]
   * - 빈 메시지 (공백만 있는 경우) 전송 방지
   * - 전송 후 입력창 초기화
   * - 연결 해제 시 전송 불가
   * - 스크롤 위치 유지 (메인 페이지 스크롤 방지)
   * - 타이핑 종료 알림
   */
  const handleSend = () => {
    const trimmedMessage = message.trim();

    // 빈 메시지 검증
    if (!trimmedMessage) {
      return;
    }

    // 현재 스크롤 위치 저장
    const scrollY = window.scrollY;

    // 메시지 전송
    onSendMessage(trimmedMessage);

    // 타이핑 종료 알림
    if (onTyping && typingTimeoutRef.current) {
      clearTimeout(typingTimeoutRef.current);
      onTyping(false);
    }

    // 입력창 초기화
    setMessage("");

    // Textarea 높이 리셋
    if (textareaRef.current) {
      textareaRef.current.style.height = "auto";
    }

    // 스크롤 위치 복원
    requestAnimationFrame(() => {
      window.scrollTo(0, scrollY);
    });
  };

  /**
   * Enter 키 처리
   *
   * [키보드 단축키]
   * - Enter: 메시지 전송
   * - Shift + Enter: 줄바꿈
   */
  const handleKeyDown = (e: KeyboardEvent<HTMLTextAreaElement>) => {
    if (e.key === "Enter" && !e.shiftKey) {
      e.preventDefault(); // 기본 줄바꿈 방지
      handleSend();
    }
  };

  /**
   * 타이핑 이벤트 전송 (디바운싱)
   *
   * [디바운싱]
   * - 입력이 시작되면 즉시 typing=true 전송
   * - 3초간 입력이 없으면 typing=false 전송
   * - 과도한 이벤트 방지
   */
  const notifyTyping = useCallback(() => {
    if (!onTyping) return;

    // 타이핑 시작 알림
    onTyping(true);

    // 이전 타이머 취소
    if (typingTimeoutRef.current) {
      clearTimeout(typingTimeoutRef.current);
    }

    // 3초 후 타이핑 종료 알림
    typingTimeoutRef.current = setTimeout(() => {
      onTyping(false);
    }, 3000);
  }, [onTyping]);

  /**
   * 텍스트 변경 처리 (자동 높이 조절)
   *
   * [자동 높이 조절]
   * - 내용에 맞춰 Textarea 높이 동적 조절
   * - 최대 높이는 CSS로 제한 (max-h-32 = 8rem)
   * - 스크롤 위치 유지 (메인 페이지 스크롤 방지)
   *
   * [타이핑 이벤트]
   * - 입력 시 타이핑 이벤트 전송 (디바운싱)
   */
  const handleChange = (e: ChangeEvent<HTMLTextAreaElement>) => {
    const newValue = e.target.value;
    setMessage(newValue);

    // 타이핑 이벤트 전송 (빈 문자열 아닐 때만)
    if (newValue.trim()) {
      notifyTyping();
    }

    // 현재 스크롤 위치 저장
    const scrollY = window.scrollY;

    // 자동 높이 조절
    if (textareaRef.current) {
      textareaRef.current.style.height = "auto";
      textareaRef.current.style.height = `${textareaRef.current.scrollHeight}px`;
    }

    // 스크롤 위치 복원 (브라우저의 자동 스크롤 방지)
    requestAnimationFrame(() => {
      window.scrollTo(0, scrollY);
    });
  };

  /**
   * Focus 처리 (스크롤 방지)
   *
   * [스크롤 방지]
   * - textarea에 포커스될 때 브라우저가 자동으로 스크롤하는 것을 방지
   * - 메인 페이지 스크롤 위치 유지
   */
  const handleFocus = () => {
    // 현재 스크롤 위치 저장
    const scrollY = window.scrollY;

    // 포커스 후 스크롤 위치 복원
    setTimeout(() => {
      window.scrollTo(0, scrollY);
    }, 0);
  };

  return (
    <div className="border-t border-gray-200 bg-white p-4">
      <div className="flex items-end gap-2">
        {/* Textarea */}
        <textarea
          ref={textareaRef}
          value={message}
          onChange={handleChange}
          onKeyDown={handleKeyDown}
          onFocus={handleFocus}
          placeholder={disabled ? "연결 중..." : "메시지를 입력하세요 (Shift+Enter로 줄바꿈)"}
          disabled={disabled}
          maxLength={MAX_MESSAGE_LENGTH}
          rows={1}
          className="flex-1 resize-none rounded-lg border border-gray-300 px-4 py-2
                     focus:outline-none focus:ring-2 focus:ring-blue-500
                     disabled:bg-gray-100 disabled:text-gray-400
                     max-h-32 overflow-y-auto"
        />

        {/* 전송 버튼 */}
        <button
          onClick={handleSend}
          disabled={disabled || !message.trim()}
          className="rounded-lg bg-blue-500 px-4 py-2 text-white
                     hover:bg-blue-600 active:bg-blue-700
                     disabled:bg-gray-300 disabled:cursor-not-allowed
                     transition-colors duration-200
                     whitespace-nowrap"
        >
          전송
        </button>
      </div>

      {/* 힌트 텍스트 / 글자 수 */}
      <div className="mt-1 flex justify-between text-xs text-gray-500">
        <span>Enter로 전송, Shift+Enter로 줄바꿈</span>
        {message.length > MAX_MESSAGE_LENGTH * 0.8 && (
          <span className={message.length >= MAX_MESSAGE_LENGTH ? "text-red-500" : ""}>
            {message.length}/{MAX_MESSAGE_LENGTH}
          </span>
        )}
      </div>
    </div>
  );
}
