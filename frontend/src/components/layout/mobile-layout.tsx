import { Outlet } from "react-router";
import { Header } from "./header";
import { Footer } from "./footer";
import { TopPromoBanner } from "@/features/events";
import { useAuthStore } from "@/features/auth/stores";

export function MobileLayout() {
  const member = useAuthStore((state) => state.member);
  const isLoggedIn = !!member.email;

  return (
    <div className="app-wrapper">
      {/* 프로모션 배너 - 비로그인 사용자에게만 표시 */}
      {!isLoggedIn && (
        <TopPromoBanner
          id="welcome-2025"
          linkUrl="/events/1"
          backgroundColor="#12B886"
        >
          <span className="font-bold text-lg pt-2">🎉 신규 가입 이벤트</span>
          <span className="ml-2 text-lg pt-2">지금 가입하면 프리미엄 30일 무료!</span>
        </TopPromoBanner>
      )}

      {/* 헤더 - 전체 너비, sticky */}
      <Header />

      {/* 메인 콘텐츠 영역 */}
      <main className="app-main bg-white">
        <Outlet />
        <Footer />
      </main>
    </div>
  );
}
