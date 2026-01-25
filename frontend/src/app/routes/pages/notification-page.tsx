import { useEffect } from "react";
import { useNavigate } from "react-router";
import {
  useNotificationStore,
  getNotifications,
  markAsRead as markAsReadApi,
  markAllAsRead as markAllAsReadApi,
} from "@/features/notifications";
import { EmptyState, Spinner } from "@/components/ui";
import { getRelativeTime } from "@/utils/date";

function NotificationPage() {
  const navigate = useNavigate();
  const { notifications, setNotifications, markAsRead, markAllAsRead } = useNotificationStore();

  console.log("notifications>>>>", notifications)

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

  const handleNotificationClick = async (notification: {
    id: number;
    referenceType: string;
    referenceId: number;
    isRead: boolean;
  }) => {
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
            <button
              key={notification.id}
              onClick={() => handleNotificationClick(notification)}
              className={`w-full text-left p-4 rounded-xl transition-colors ${
                notification.isRead
                  ? "bg-white"
                  : "bg-primary-50 border-l-4 border-primary-500"
              }`}
            >
              <div className="flex items-start justify-between">
                <div className="flex-1">
                  <h3 className="font-medium text-gray-900">
                    {notification.title}
                  </h3>
                  <p className="mt-1 text-sm text-gray-600">
                    {notification.content}
                  </p>
                </div>
                {!notification.isRead && (
                  <span className="w-2 h-2 bg-primary-500 rounded-full ml-2 mt-2" />
                )}
              </div>
              <p className="mt-2 text-xs text-gray-400">
                {getRelativeTime(notification.createdAt)}
              </p>
            </button>
          ))}
        </div>
      )}
    </div>
  );
}

export default NotificationPage;
