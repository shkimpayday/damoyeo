import { useState, useEffect } from "react";
import { useNavigate } from "react-router";
import { removeCookie } from "@/lib/cookie";
import { useAuthStore } from "../stores";
import { SESSION_EXPIRED_EVENT } from "@/lib/axios";

/**
 * 세션 만료 모달
 *
 * Refresh Token이 만료되었을 때 표시됩니다.
 * axios.ts의 응답 인터셉터에서 session-expired 이벤트를 발생시키면 이 모달이 표시됩니다.
 */
export function SessionExpiredModal() {
  const [show, setShow] = useState(false);
  const navigate = useNavigate();
  const logout = useAuthStore((s) => s.logout);

  useEffect(() => {
    const handleExpired = () => {
      setShow(true);
    };

    window.addEventListener(SESSION_EXPIRED_EVENT, handleExpired);
    return () => {
      window.removeEventListener(SESSION_EXPIRED_EVENT, handleExpired);
    };
  }, []);

  const handleConfirm = () => {
    setShow(false);
    logout();
    removeCookie("member");
    navigate("/member/login");
  };

  if (!show) return null;

  return (
    <div className="fixed inset-0 z-[100] flex items-center justify-center bg-black/50 backdrop-blur-sm">
      <div className="bg-white rounded-2xl p-6 mx-4 max-w-sm w-full shadow-xl">
        <div className="text-center">
          <div className="text-4xl mb-3">🔒</div>
          <h2 className="text-lg font-bold text-gray-900 mb-2">
            세션이 만료되었습니다
          </h2>
          <p className="text-sm text-gray-500 mb-6">
            로그인 유효 기간이 만료되었습니다.
            <br />
            다시 로그인해주세요.
          </p>
          <button
            onClick={handleConfirm}
            className="w-full py-3 bg-primary-500 text-white rounded-xl font-semibold hover:bg-primary-600 transition-colors"
          >
            로그인하기
          </button>
        </div>
      </div>
    </div>
  );
}
