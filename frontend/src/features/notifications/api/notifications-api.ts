import { jwtAxios } from "@/lib/axios";
import { ENV } from "@/config";
import type { NotificationDTO } from "../types";

const prefix = `${ENV.API_URL}/api/notifications`;

/**
 * 알림 목록 조회
 */
export const getNotifications = async (): Promise<NotificationDTO[]> => {
  const res = await jwtAxios.get(prefix);
  // 백엔드가 PageResponseDTO 형태로 반환하므로 dtoList 추출
  return res.data.dtoList || [];
};

/**
 * 안 읽은 알림 수 조회
 */
export const getUnreadCount = async (): Promise<number> => {
  const res = await jwtAxios.get(`${prefix}/unread/count`);
  // 백엔드가 { count: N } 형태로 반환
  return res.data.count ?? 0;
};

/**
 * 알림 읽음 처리
 */
export const markAsRead = async (
  notificationId: number
): Promise<void> => {
  const res = await jwtAxios.patch(`${prefix}/${notificationId}/read`);
  return res.data;
};

/**
 * 전체 알림 읽음 처리
 */
export const markAllAsRead = async (): Promise<void> => {
  const res = await jwtAxios.patch(`${prefix}/read-all`);
  return res.data;
};

/**
 * 알림 삭제(안보이기)
 */
export const removeNotificationApi = async (
  notificationId: number
): Promise<void> => {
  const res = await jwtAxios.patch(`${prefix}/${notificationId}/delete`);
  return res.data;
}