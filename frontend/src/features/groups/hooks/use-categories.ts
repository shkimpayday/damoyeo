import { useQuery } from "@tanstack/react-query";
import { getCategories, DEFAULT_CATEGORIES } from "../api";

/**
 * 카테고리 목록 조회
 */
export const useCategories = () => {
  return useQuery({
    queryKey: ["categories"],
    queryFn: getCategories,
    staleTime: 30 * 60 * 1000, // 30분 캐싱
    placeholderData: DEFAULT_CATEGORIES,
  });
};
