import { useEffect } from "react";
import { Outlet, useLocation } from "react-router";
import { Header } from "./header";
import { Footer } from "./footer";
import { TopPromoBanner } from "@/features/events";
import { useAuthStore } from "@/features/auth/stores";
import { SessionExpiredModal } from "@/features/auth/components/session-expired-modal";
import { SupportFloatingButton } from "@/features/support/components/support-floating-button";

export function MobileLayout() {
  const { pathname } = useLocation();

  // 페이지 이동 시 스크롤 최상단으로 이동
  useEffect(() => {
    window.scrollTo(0, 0);
  }, [pathname]);
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
      <main className="app-main bg-white app-content">
        <Outlet />
        
      </main>

      {/* 푸터 - 전체 너비*/}
      <Footer />

      {/* 상담 플로팅 버튼 */}
      <SupportFloatingButton />

      {/* 세션 만료 모달 */}
      <SessionExpiredModal />
    </div>
  );
}
