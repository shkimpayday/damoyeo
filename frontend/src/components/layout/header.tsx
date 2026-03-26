import { Link, useLocation, useNavigate } from "react-router";
import { useState, useRef, useEffect } from "react";
import { Search, ChevronDown, User, Users, LogOut, PlusCircle, Settings } from "lucide-react";
import { useAuth } from "@/features/auth";
import { Logo } from "@/components/ui";
import { NotificationBell } from "./notification-bell";
import { getImageUrl } from "@/utils";

const NAV_TABS = [
  { path: "/", label: "홈", exact: true },
  { path: "/groups/list", label: "모임", exact: false },
  { path: "/meetings", label: "정모", exact: false },
];

export function Header() {
  const { isLoggedIn, loginState, doLogout } = useAuth();
  const location = useLocation();
  const navigate = useNavigate();
  const [isDropdownOpen, setIsDropdownOpen] = useState(false);
  const dropdownRef = useRef<HTMLDivElement>(null);

  const isActiveTab = (tab: (typeof NAV_TABS)[0]) => {
    if (tab.exact) {
      return location.pathname === tab.path;
    }
    return location.pathname.startsWith(tab.path);
  };

  // 외부 클릭 시 드롭다운 닫기
  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (dropdownRef.current && !dropdownRef.current.contains(event.target as Node)) {
        setIsDropdownOpen(false);
      }
    };

    document.addEventListener("mousedown", handleClickOutside);
    return () => document.removeEventListener("mousedown", handleClickOutside);
  }, []);

  // 페이지 이동 시 드롭다운 닫기
  useEffect(() => {
    setIsDropdownOpen(false);
  }, [location.pathname]);

  const handleLogout = () => {
    doLogout();
    setIsDropdownOpen(false);
    navigate("/");
  };

  return (
    <header className="app-header border-b border-gray-100">
      {/* 전체 너비 내부 컨테이너 */}
      <div className="app-content">
        {/* 상단 영역: 로고 + 네비게이션 + 액션 (높이 1.5배) */}
        <div className="flex items-center justify-between h-24">
          {/* 좌측: 로고 (더 크게) */}
          <Link to="/" className="hover:opacity-80 transition-opacity shrink-0">
            <Logo size="lg" />
          </Link>

          {/* 중앙: 네비게이션 (데스크톱) */}
          <nav className="hidden md:flex items-center gap-8 absolute left-4/10 -translate-x-1/2">
            {NAV_TABS.map((tab) => (
              <Link
                key={tab.path}
                to={tab.path}
                className={`relative py-3 text-2xl font-semibold transition-colors ${
                  isActiveTab(tab)
                    ? "text-primary-600"
                    : "text-gray-500 hover:text-gray-900"
                }`}
              >
                {tab.label}
                {isActiveTab(tab) && (
                  <div className="absolute bottom-0 left-0 right-0 h-0.5 bg-primary-500 rounded-full" />
                )}
              </Link>
            ))}
          </nav>

          {/* 우측: 검색 + 알림 + 프로필/로그인 */}
          <div className="flex items-center gap-1">
            {/* 검색 버튼 */}
            <Link
              to="/search"
              className="p-2.5 text-gray-500 hover:text-gray-900 hover:bg-gray-50 rounded-full transition-colors"
            >
              <Search size={22} strokeWidth={1.5} />
            </Link>

            {isLoggedIn ? (
              <>
                {/* 모임 만들기 버튼 */}
                <Link
                  to="/groups/create"
                  className="hidden sm:flex items-center gap-1.5 px-3 py-2 text-sm font-medium text-primary-600 hover:bg-primary-50 rounded-lg transition-colors"
                >
                  <PlusCircle size={18} strokeWidth={2} />
                  <span>모임 만들기</span>
                </Link>

                <NotificationBell />

                {/* 프로필 드롭다운 */}
                <div className="relative ml-1" ref={dropdownRef}>
                  <button
                    onClick={() => setIsDropdownOpen(!isDropdownOpen)}
                    className="flex items-center gap-1 p-1 rounded-full hover:bg-gray-50 transition-colors"
                  >
                    <div className="w-9 h-9 rounded-full bg-gray-100 flex items-center justify-center overflow-hidden ring-2 ring-transparent hover:ring-primary-200 transition-all">
                      {loginState.profileImage ? (
                        <img
                          src={getImageUrl(loginState.profileImage)}
                          alt={loginState.nickname}
                          className="w-full h-full object-cover"
                        />
                      ) : (
                        <span className="text-sm text-gray-500 font-medium">
                          {loginState.nickname?.charAt(0) || "?"}
                        </span>
                      )}
                    </div>
                    <ChevronDown
                      size={16}
                      className={`text-gray-400 transition-transform ${isDropdownOpen ? "rotate-180" : ""}`}
                    />
                  </button>

                  {/* 드롭다운 메뉴 */}
                  {isDropdownOpen && (
                    <div className="absolute right-0 mt-2 w-56 bg-white rounded-xl shadow-lg border border-gray-100 py-2 z-50">
                      {/* 사용자 정보 */}
                      <div className="px-4 py-3 border-b border-gray-100">
                        <p className="text-sm font-semibold text-gray-900 truncate">
                          {loginState.nickname}
                        </p>
                        <p className="text-xs text-gray-500 truncate">
                          {loginState.email}
                        </p>
                      </div>

                      {/* 메뉴 아이템 */}
                      <div className="py-1">
                        <Link
                          to="/member/profile"
                          className="flex items-center gap-3 px-4 py-2.5 text-sm text-gray-700 hover:bg-gray-50 transition-colors"
                        >
                          <User size={18} className="text-gray-400" />
                          마이페이지
                        </Link>
                        <Link
                          to="/member/my-groups"
                          className="flex items-center gap-3 px-4 py-2.5 text-sm text-gray-700 hover:bg-gray-50 transition-colors"
                        >
                          <Users size={18} className="text-gray-400" />
                          내 모임
                        </Link>
                        {/* 모바일에서만 모임 만들기 표시 */}
                        <Link
                          to="/groups/create"
                          className="sm:hidden flex items-center gap-3 px-4 py-2.5 text-sm text-gray-700 hover:bg-gray-50 transition-colors"
                        >
                          <PlusCircle size={18} className="text-gray-400" />
                          모임 만들기
                        </Link>
                        {/* 관리자 메뉴 (ADMIN 권한 시) */}
                        {loginState.roleNames?.includes("ADMIN") && (
                          <Link
                            to="/admin"
                            className="flex items-center gap-3 px-4 py-2.5 text-sm text-indigo-600 hover:bg-indigo-50 transition-colors"
                          >
                            <Settings size={18} className="text-indigo-500" />
                            관리자 페이지
                          </Link>
                        )}
                      </div>

                      {/* 로그아웃 */}
                      <div className="border-t border-gray-100 pt-1">
                        <button
                          onClick={handleLogout}
                          className="flex items-center gap-3 w-full px-4 py-2.5 text-sm text-red-600 hover:bg-red-50 transition-colors"
                        >
                          <LogOut size={18} />
                          로그아웃
                        </button>
                      </div>
                    </div>
                  )}
                </div>
              </>
            ) : (
              <Link
                to="/member/login"
                className="ml-2 px-5 py-2 text-sm font-semibold text-white bg-primary-500 rounded-full hover:bg-primary-600 transition-colors"
              >
                로그인
              </Link>
            )}
          </div>
        </div>

        {/* 모바일 탭 네비게이션 */}
        <nav className="md:hidden flex items-center gap-6 -mb-px">
          {NAV_TABS.map((tab) => (
            <Link
              key={tab.path}
              to={tab.path}
              className={`relative py-3 text-sm font-semibold transition-colors ${
                isActiveTab(tab)
                  ? "text-primary-600"
                  : "text-gray-400 hover:text-gray-600"
              }`}
            >
              {tab.label}
              {isActiveTab(tab) && (
                <div className="absolute bottom-0 left-0 right-0 h-0.5 bg-primary-500 rounded-full" />
              )}
            </Link>
          ))}
        </nav>
      </div>
    </header>
  );
}
