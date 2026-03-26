import { Link, Outlet, useLocation, Navigate } from "react-router";
import { useAuth } from "@/features/auth";

/**
 * 관리자 레이아웃
 *
 * [기능]
 * - ADMIN 권한 체크
 * - 사이드바 네비게이션
 * - 메인 콘텐츠 영역
 *
 * [라우팅]
 * /admin/dashboard - 대시보드
 * /admin/members - 회원 관리
 * /admin/groups - 모임 관리
 * /admin/events - 이벤트 관리
 */

// 관리자 메뉴 항목
const ADMIN_MENUS = [
  { path: "/admin/dashboard", label: "대시보드", icon: "📊" },
  { path: "/admin/members", label: "회원 관리", icon: "👥" },
  { path: "/admin/groups", label: "모임 관리", icon: "🏠" },
  { path: "/admin/events", label: "이벤트 관리", icon: "🎉" },
  { path: "/admin/support", label: "상담 관리", icon: "💬" },
];

export function AdminLayout() {
  const { loginState, isLoggedIn } = useAuth();
  const location = useLocation();

  // 로그인 체크
  if (!isLoggedIn) {
    return <Navigate to="/member/login" state={{ from: location }} replace />;
  }

  // ADMIN 권한 체크
  const isAdmin = loginState.roleNames?.includes("ADMIN");
  if (!isAdmin) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gray-50">
        <div className="text-center p-8">
          <div className="text-6xl mb-4">🚫</div>
          <h1 className="text-2xl font-bold text-gray-800 mb-2">접근 권한이 없습니다</h1>
          <p className="text-gray-600 mb-4">관리자만 접근할 수 있는 페이지입니다.</p>
          <Link
            to="/"
            className="inline-block px-4 py-2 bg-primary-500 text-white rounded-lg hover:bg-primary-600"
          >
            홈으로 돌아가기
          </Link>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-100">
      {/* 상단 헤더 */}
      <header className="bg-white shadow-sm border-b border-gray-200">
        <div className="flex items-center justify-between px-6 h-16">
          <div className="flex items-center gap-4">
            <Link to="/admin/dashboard" className="text-xl font-bold text-primary-600">
              다모여 관리자
            </Link>
          </div>
          <div className="flex items-center gap-4">
            <span className="text-sm text-gray-600">
              {loginState.nickname} ({loginState.email})
            </span>
            <Link
              to="/"
              className="text-sm text-gray-500 hover:text-gray-700"
            >
              사이트로 이동 →
            </Link>
          </div>
        </div>
      </header>

      <div className="flex h-[calc(100vh-64px)] overflow-hidden">
        {/* 사이드바 */}
        <aside className="w-64 bg-white shadow-sm border-r border-gray-200 shrink-0">
          <nav className="p-4">
            <ul className="space-y-1">
              {ADMIN_MENUS.map((menu) => {
                const isActive = location.pathname === menu.path;
                return (
                  <li key={menu.path}>
                    <Link
                      to={menu.path}
                      className={`flex items-center gap-3 px-4 py-3 rounded-lg transition-colors ${
                        isActive
                          ? "bg-primary-50 text-primary-600 font-medium"
                          : "text-gray-600 hover:bg-gray-50"
                      }`}
                    >
                      <span className="text-lg">{menu.icon}</span>
                      <span>{menu.label}</span>
                    </Link>
                  </li>
                );
              })}
            </ul>
          </nav>
        </aside>

        {/* 메인 콘텐츠 */}
        <main
          className={`flex-1 overflow-auto ${
            location.pathname === "/admin/support" ? "p-0" : "p-6"
          }`}
        >
          <Outlet />
        </main>
      </div>
    </div>
  );
}

export default AdminLayout;
