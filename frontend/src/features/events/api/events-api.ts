import axios from "axios";
import type { EventBanner, EventDetail } from "../types";

const API_BASE = "/api/events";

export const eventsApi = {
  // 이벤트 배너 목록
  getBanners: async (): Promise<EventBanner[]> => {
    const response = await axios.get(`${API_BASE}/banners`);
    return response.data;
  },

  // 이벤트 상세
  getDetail: async (eventId: number): Promise<EventDetail> => {
    const response = await axios.get(`${API_BASE}/${eventId}`);
    return response.data;
  },
};
