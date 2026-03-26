import { publicAxios } from "@/lib/axios";
import { ENV } from "@/config";
import type { EventBanner, EventDetail } from "../types";

const API_BASE = `${ENV.API_URL}/api/events`;

export const eventsApi = {
  // 이벤트 배너 목록
  getBanners: async (): Promise<EventBanner[]> => {
    const response = await publicAxios.get(`${API_BASE}/banners`);
    return response.data;
  },

  // 이벤트 상세
  getDetail: async (eventId: number): Promise<EventDetail> => {
    const response = await publicAxios.get(`${API_BASE}/${eventId}`);
    return response.data;
  },
};
