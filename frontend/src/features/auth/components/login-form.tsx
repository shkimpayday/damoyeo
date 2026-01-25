import { useState, useEffect } from "react";
import { Link, useLocation, useNavigate } from "react-router";
import { useAuth } from "../hooks";
import { ResultModal } from "@/components/ui/result-modal";
import { getCookie, setCookie, removeCookie } from "@/lib/cookie";  // 추가
import { ENV } from "@/config";

export function LoginForm() {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [showModal, setShowModal] = useState(false);
  const [rememberMe, setRememberMe] = useState(false);

  const { doLogin, loginStatus } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();

  // 로그인 성공 후 돌아갈 경로
  const from = location.state?.from?.pathname || "/";

  const redirectTo = from.startsWith("/member/") ? "/" : from;

  useEffect(() => {
      const saved = getCookie("rememberLogin");
      if(saved) {
        setEmail(saved.email || "");
        setPassword(saved.password || "");
        setRememberMe(true);
      }
  },[])


  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!email || !password) return;

    try {
      await doLogin(email, password);

      // 기억하기 처리
      if (rememberMe) {
        setCookie("rememberLogin",JSON.stringify({email, password}),30)
      } else {
        removeCookie("rememberLogin")
      }

      setShowModal(true);
    } catch {
      setShowModal(true);
    }
  };

  const handleModalClose = () => {
    setShowModal(false);
    if (loginStatus === "fulfilled") {
      navigate(redirectTo, { replace: true });
    }
  };

  // 카카오 로그인 URL
  const kakaoRedirectUri = `${window.location.origin}/member/kakao`;
  const kakaoAuthUrl = `https://kauth.kakao.com/oauth/authorize?client_id=${ENV.KAKAO_CLIENT_ID}&redirect_uri=${kakaoRedirectUri}&response_type=code`;

  return (
    <>
      <div className="w-full max-w-sm">
        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              이메일
            </label>
            <input
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              placeholder="이메일을 입력하세요"
              className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-primary-500 outline-none transition-colors"
              required
            />
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              비밀번호
            </label>
            <input
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              placeholder="비밀번호를 입력하세요"
              className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-primary-500 outline-none transition-colors"
              required
            />
          </div>

          {/* 기억하기 체크박스 */}
          <div className="flex items-center mt-2">
            <input
              type="checkbox"
              id="rememberMe"
              checked={rememberMe}
              onChange={(e) => setRememberMe(e.target.checked)}
              className="h-4 w-4 text-primary-500 focus:ring-primary-500 border-gray-300 rounded cursor-pointer"
            />
            <label
              htmlFor="rememberMe"
              className="ml-2 text-sm text-gray-600 cursor-pointer select-none"
            >
            로그인 정보를 저장할게요.
            </label>
          </div>

          <button
            type="submit"
            disabled={loginStatus === "pending"}
            className="w-full py-3 bg-primary-500 text-white font-medium rounded-lg hover:bg-primary-600 disabled:bg-gray-300 disabled:cursor-not-allowed transition-colors"
          >
            {loginStatus === "pending" ? "로그인 중..." : "로그인"}
          </button>
        </form>

        {/* Divider */}
        <div className="my-6 flex items-center">
          <div className="flex-1 border-t border-gray-300"></div>
          <span className="px-4 text-sm text-gray-500">또는</span>
          <div className="flex-1 border-t border-gray-300"></div>
        </div>

        {/* Social Login */}
        <a
          href={kakaoAuthUrl}
          className="flex items-center justify-center w-full py-3 bg-[#FEE500] text-[#191919] font-medium rounded-lg hover:bg-[#FDD835] transition-colors"
        >
          <span className="mr-2">💬</span>
          카카오로 시작하기
        </a>

        {/* Sign Up Link */}
        <p className="mt-6 text-center text-sm text-gray-500">
          아직 회원이 아니신가요?{" "}
          <Link
            to="/member/signup"
            className="text-primary-600 font-medium hover:underline"
          >
            회원가입
          </Link>
        </p>

        {/* Back to Home */}
        <Link
          to="/"
          className="block mt-4 text-center text-sm text-gray-500 hover:text-gray-700"
        >
          홈으로 돌아가기
        </Link>
      </div>

      {/* Result Modal */}
      {showModal && (
        <ResultModal
          title={loginStatus === "fulfilled" ? "로그인 성공" : "로그인 실패"}
          content={
            loginStatus === "fulfilled"
              ? "환영합니다!"
              : "이메일 또는 비밀번호를 확인해주세요."
          }
          callbackFn={handleModalClose}
        />
      )}
    </>
  );
}
