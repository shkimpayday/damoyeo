import { useQuery } from "@tanstack/react-query";
import { eventsApi } from "../api";

export const useEventBanners = () => {
  return useQuery({
    queryKey: ["events", "banners"],
    queryFn: eventsApi.getBanners,
    staleTime: 1000 * 60 * 5, // 5분
  });
};

export const useEventDetail = (eventId: number) => {
  return useQuery({
    queryKey: ["events", eventId],
    queryFn: () => eventsApi.getDetail(eventId),
    enabled: !!eventId,
  });
};
