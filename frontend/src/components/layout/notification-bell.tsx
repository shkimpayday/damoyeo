import { Link } from "react-router";
import { useEffect } from "react";
import { Bell } from "lucide-react";
import { useNotificationStore, getUnreadCount } from "@/features/notifications";

export function NotificationBell() {
  const { unreadCount, setUnreadCount } = useNotificationStore();

  // 컴포넌트 마운트 시 안 읽은 알림 수 조회
  useEffect(() => {
    const fetchUnreadCount = async () => {
      try {
        const count = await getUnreadCount();
        setUnreadCount(count);
      } catch {
        // 에러 시 무시 (로그아웃 상태 등)
      }
    };

    fetchUnreadCount();

    // 30초마다 폴링 (실시간 알림 대용)
    const interval = setInterval(fetchUnreadCount, 30000);
    return () => clearInterval(interval);
  }, [setUnreadCount]);

  return (
    <Link
      to="/notifications"
      className="relative p-2.5 text-gray-500 hover:text-gray-900 hover:bg-gray-50 rounded-full transition-colors"
    >
      <Bell size={22} strokeWidth={1.5} />
      {unreadCount > 0 && (
        <span className="absolute -top-0.5 -right-0.5 flex items-center justify-center min-w-[18px] h-[18px] px-1 text-[10px] font-bold text-white bg-red-500 rounded-full border-2 border-white">
          {unreadCount > 99 ? "99+" : unreadCount}
        </span>
      )}
    </Link>
  );
}
