import { useState, useEffect } from "react";
import { Link, useNavigate } from "react-router";
import { signupPost, sendVerificationCode, verifyEmailCode } from "../api";
import { ResultModal } from "@/components/ui/result-modal";

export function SignupForm() {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [nickname, setNickname] = useState("");
  const [status, setStatus] = useState<
    "idle" | "pending" | "success" | "error"
  >("idle");
  const [errorMessage, setErrorMessage] = useState("");

  // 이메일 인증 관련 상태
  const [verificationCode, setVerificationCode] = useState("");
  const [emailVerified, setEmailVerified] = useState(false);
  const [emailSending, setEmailSending] = useState(false);
  const [emailVerifying, setEmailVerifying] = useState(false);
  const [emailMessage, setEmailMessage] = useState("");
  const [emailMessageType, setEmailMessageType] = useState<
    "success" | "error" | ""
  >("");

  // 인증 코드 타이머
  const [timeLeft, setTimeLeft] = useState(0);

  const navigate = useNavigate();

  // 타이머 효과
  useEffect(() => {
    if (timeLeft <= 0) return;

    const timer = setInterval(() => {
      setTimeLeft((prev) => prev - 1);
    }, 1000);

    return () => clearInterval(timer);
  }, [timeLeft]);

  // 타이머 포맷팅
  const formatTime = (seconds: number) => {
    const min = Math.floor(seconds / 60);
    const sec = seconds % 60;
    return `${min}:${sec.toString().padStart(2, "0")}`;
  };

  // 이메일 유효성 검사
  const isValidEmail = (email: string) => {
    return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
  };

  // 인증 코드 발송
  const handleSendCode = async () => {
    if (!isValidEmail(email)) {
      setEmailMessage("올바른 이메일 형식을 입력해주세요.");
      setEmailMessageType("error");
      return;
    }

    setEmailSending(true);
    setEmailMessage("");

    try {
      const result = await sendVerificationCode(email);
      if (result.success) {
        setEmailMessage("인증 코드가 발송되었습니다. 이메일을 확인해주세요.");
        setEmailMessageType("success");
        setTimeLeft(300); // 5분 타이머 시작
      } else {
        setEmailMessage(result.message);
        setEmailMessageType("error");
      }
    } catch {
      setEmailMessage("이메일 발송에 실패했습니다. 잠시 후 다시 시도해주세요.");
      setEmailMessageType("error");
    } finally {
      setEmailSending(false);
    }
  };

  // 인증 코드 검증
  const handleVerifyCode = async () => {
    if (verificationCode.length !== 6) {
      setEmailMessage("6자리 인증 코드를 입력해주세요.");
      setEmailMessageType("error");
      return;
    }

    setEmailVerifying(true);
    setEmailMessage("");

    try {
      const result = await verifyEmailCode(email, verificationCode);
      if (result.success) {
        setEmailVerified(true);
        setEmailMessage("이메일이 인증되었습니다.");
        setEmailMessageType("success");
        setTimeLeft(0); // 타이머 중지
      } else {
        setEmailMessage(result.message);
        setEmailMessageType("error");
      }
    } catch {
      setEmailMessage(
        "인증 코드가 올바르지 않거나 만료되었습니다. 다시 시도해주세요."
      );
      setEmailMessageType("error");
    } finally {
      setEmailVerifying(false);
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    // 이메일 인증 확인
    if (!emailVerified) {
      setErrorMessage("이메일 인증을 완료해주세요.");
      setStatus("error");
      return;
    }

    if (password !== confirmPassword) {
      setErrorMessage("비밀번호가 일치하지 않습니다.");
      setStatus("error");
      return;
    }

    if (password.length < 8) {
      setErrorMessage("비밀번호는 8자 이상이어야 합니다.");
      setStatus("error");
      return;
    }

    setStatus("pending");

    try {
      await signupPost({ email, password, nickname });
      setStatus("success");
    } catch {
      setErrorMessage(
        "회원가입에 실패했습니다. 이미 사용 중인 이메일일 수 있습니다."
      );
      setStatus("error");
    }
  };

  const handleModalClose = () => {
    if (status === "success") {
      navigate("/member/login");
    }
    setStatus("idle");
    setErrorMessage("");
  };

  return (
    <>
      <div className="w-full max-w-sm">
        <form onSubmit={handleSubmit} className="space-y-4">
          {/* 이메일 입력 + 인증 */}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              이메일
            </label>
            <div className="flex gap-2">
              <input
                type="email"
                value={email}
                onChange={(e) => {
                  setEmail(e.target.value);
                  // 이메일 변경시 인증 상태 초기화
                  if (emailVerified) {
                    setEmailVerified(false);
                    setVerificationCode("");
                    setEmailMessage("");
                  }
                }}
                placeholder="이메일을 입력하세요"
                className="flex-1 px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-primary-500 outline-none transition-colors disabled:bg-gray-100"
                required
                disabled={emailVerified}
              />
              <button
                type="button"
                onClick={handleSendCode}
                disabled={emailSending || emailVerified || !email}
                className="px-4 py-3 bg-primary-500 text-white text-sm font-medium rounded-lg hover:bg-primary-600 disabled:bg-gray-300 disabled:cursor-not-allowed transition-colors whitespace-nowrap"
              >
                {emailSending
                  ? "발송 중..."
                  : emailVerified
                    ? "인증완료"
                    : timeLeft > 0
                      ? "재발송"
                      : "인증코드"}
              </button>
            </div>

            {/* 인증 코드 입력 (인증 코드 발송 후 표시) */}
            {timeLeft > 0 && !emailVerified && (
              <div className="mt-2 flex gap-2">
                <input
                  type="text"
                  value={verificationCode}
                  onChange={(e) =>
                    setVerificationCode(e.target.value.replace(/\D/g, ""))
                  }
                  placeholder="6자리 인증 코드"
                  maxLength={6}
                  className="flex-1 px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-primary-500 outline-none transition-colors"
                />
                <button
                  type="button"
                  onClick={handleVerifyCode}
                  disabled={emailVerifying || verificationCode.length !== 6}
                  className="px-4 py-3 bg-gray-800 text-white text-sm font-medium rounded-lg hover:bg-gray-900 disabled:bg-gray-300 disabled:cursor-not-allowed transition-colors whitespace-nowrap"
                >
                  {emailVerifying ? "확인 중..." : "확인"}
                </button>
              </div>
            )}

            {/* 타이머 & 메시지 */}
            <div className="mt-1 flex items-center justify-between">
              {timeLeft > 0 && !emailVerified && (
                <span className="text-sm text-gray-500">
                  남은 시간: {formatTime(timeLeft)}
                </span>
              )}
              {emailMessage && (
                <span
                  className={`text-sm ${emailMessageType === "success" ? "text-green-600" : "text-red-500"}`}
                >
                  {emailMessage}
                </span>
              )}
            </div>
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              닉네임
            </label>
            <input
              type="text"
              value={nickname}
              onChange={(e) => setNickname(e.target.value)}
              placeholder="닉네임을 입력하세요"
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
              placeholder="8자 이상 입력하세요"
              className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-primary-500 outline-none transition-colors"
              required
              minLength={8}
            />
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              비밀번호 확인
            </label>
            <input
              type="password"
              value={confirmPassword}
              onChange={(e) => setConfirmPassword(e.target.value)}
              placeholder="비밀번호를 다시 입력하세요"
              className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-primary-500 outline-none transition-colors"
              required
            />
          </div>

          <button
            type="submit"
            disabled={status === "pending" || !emailVerified}
            className="w-full py-3 bg-primary-500 text-white font-medium rounded-lg hover:bg-primary-600 disabled:bg-gray-300 disabled:cursor-not-allowed transition-colors"
          >
            {status === "pending" ? "가입 중..." : "회원가입"}
          </button>
        </form>

        {/* Login Link */}
        <p className="mt-6 text-center text-sm text-gray-500">
          이미 회원이신가요?{" "}
          <Link
            to="/member/login"
            className="text-primary-600 font-medium hover:underline"
          >
            로그인
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
      {(status === "success" || status === "error") && (
        <ResultModal
          title={status === "success" ? "회원가입 완료" : "회원가입 실패"}
          content={
            status === "success"
              ? "환영합니다! 로그인 페이지로 이동합니다."
              : errorMessage
          }
          callbackFn={handleModalClose}
        />
      )}
    </>
  );
}
