import { useEffect } from "react";
import { useNavigate, useSearchParams } from "react-router";
import { kakaoLogin, useAuthStore } from "@/features/auth";
import { setCookie } from "@/lib/cookie";
import { Spinner } from "@/components/ui";

function KakaoRedirectPage() {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const { save } = useAuthStore();

  useEffect(() => {
    const code = searchParams.get("code");

    if (code) {
      handleKakaoLogin(code);
    } else {
      navigate("/member/login");
    }
  }, [searchParams]);

  const handleKakaoLogin = async (code: string) => {
    try {
      const data = await kakaoLogin(code);

      // 상태 저장
      save(data);
      setCookie("member", JSON.stringify(data), 1);

      // 홈으로 이동
      navigate("/", { replace: true });
    } catch (error) {
      console.error("Kakao login failed:", error);
      navigate("/member/login");
    }
  };

  return (
    <div className="min-h-screen flex flex-col items-center justify-center">
      <Spinner size="lg" />
      <p className="mt-4 text-gray-500">카카오 로그인 처리 중...</p>
    </div>
  );
}

export default KakaoRedirectPage;
