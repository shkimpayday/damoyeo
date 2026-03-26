import { useEffect } from "react";
import { useNavigate } from "react-router";
import {
  useNotificationStore,
  getNotifications,
  markAsRead as markAsReadApi,
  markAllAsRead as markAllAsReadApi,
  type NotificationDTO,
  removeNotificationApi,
} from "@/features/notifications";
import { EmptyState, Spinner } from "@/components/ui";
import { getRelativeTime } from "@/utils/date";

function NotificationPage() {
  const navigate = useNavigate();
  const { notifications, setNotifications, markAsRead, markAllAsRead, removeNotification } = useNotificationStore();

  useEffect(() => {
    loadNotifications();
  }, []);

  const loadNotifications = async () => {
    try {
      const data = await getNotifications();
      setNotifications(data);
    } catch (error) {
      console.error("Failed to load notifications:", error);
    }
  };

  const handleNotificationClick = async (notification: NotificationDTO) => {
    // 읽지 않은 알림이면 읽음 처리
    if (!notification.isRead) {
      try {
        await markAsReadApi(notification.id);
        markAsRead(notification.id);
      } catch (error) {
        console.error("Failed to mark notification as read:", error);
      }
    }

    // Navigate based on reference type
    switch (notification.referenceType) {
      case "GROUP":
        navigate(`/groups/${notification.referenceId}`);
        break;
      case "MEETING":
        navigate(`/meetings/${notification.referenceId}`);
        break;
      default:
        break;
    }
  };

  const deleteNotification = async(notifications: NotificationDTO) => {
    try {
      await removeNotificationApi(notifications.id);
      removeNotification(notifications.id);
    }catch (error) {
      console.error("deleteNotification error")
    }
  }

  const handleMarkAllAsRead = async () => {
    try {
      await markAllAsReadApi();
      markAllAsRead();
    } catch (error) {
      console.error("Failed to mark all as read:", error);
    }
  };

  if (!Array.isArray(notifications)) {
    return (
      <div className="flex items-center justify-center h-64">
        <Spinner />
      </div>
    );
  }

  return (
    <div className="p-4">
      <div className="flex items-center justify-between mb-4">
        <h1 className="text-xl font-bold text-gray-900">알림</h1>
        {notifications.length > 0 && (
          <button
            onClick={handleMarkAllAsRead}
            className="text-sm text-primary-600 cursor-pointer hover:text-primary-700"
          >
            모두 읽음
          </button>
        )}
      </div>

      {notifications.length === 0 ? (
        <EmptyState
          icon="🔔"
          title="알림이 없습니다"
          description="새로운 알림이 오면 여기에 표시됩니다"
        />
      ) : (
        <div className="space-y-2">
          {notifications.map((notification) => (
            <div
              key={notification.id}
              className={`group relative p-4 rounded-xl transition-colors cursor-pointer ${
                notification.isRead
                  ? "bg-white hover:bg-gray-50"
                  : "bg-primary-50 border-l-4 border-primary-500"
              }`}
              onClick={() => handleNotificationClick(notification)}
            >
              <div className="flex justify-between items-start">
                <div className="flex-1">
                  <h3 className="font-medium text-gray-900">{notification.title}</h3>
                  <p className="text-sm text-gray-600 mt-1">{notification.content}</p>
                  <p className="mt-2 text-xs text-gray-400">
                    {getRelativeTime(notification.createdAt)}
                  </p>
                </div>

                {/* 호버 시에만 보이는 X 버튼 */}
                <div className="mt-3">
                  <button
                    onClick={(e) => {
                      e.stopPropagation(); // 부모 클릭 이벤트 방지
                      deleteNotification(notification)
                    }}
                    className="opacity-0 group-hover:opacity-100 transition-opacity
                              text-gray-400 hover:text-gray-600 p-1 ml-2"
                    aria-label="알림 삭제"
                  >
                    ✕
                  </button>
                </div>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}

export default NotificationPage;
