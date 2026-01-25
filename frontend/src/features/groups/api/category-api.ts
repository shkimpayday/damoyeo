import { publicAxios } from "@/lib/axios";
import { ENV } from "@/config";
import type { Category } from "../types";

const prefix = `${ENV.API_URL}/api/categories`;

// 카테고리별 이미지 (Unsplash 사용)
const getCategoryImage = (id: number) =>
  `https://picsum.photos/seed/category${id}/400/300`;

// 기본 카테고리 데이터 (백엔드 없을 때 사용)
export const DEFAULT_CATEGORIES: Category[] = [
  { id: 1, name: "운동/스포츠", icon: "⚽", displayOrder: 1, image: getCategoryImage(1) },
  { id: 2, name: "사교/인맥", icon: "🤝", displayOrder: 2, image: getCategoryImage(2) },
  { id: 3, name: "아웃도어/여행", icon: "🏕️", displayOrder: 3, image: getCategoryImage(3) },
  { id: 4, name: "문화/공연", icon: "🎭", displayOrder: 4, image: getCategoryImage(4) },
  { id: 5, name: "음악/악기", icon: "🎵", displayOrder: 5, image: getCategoryImage(5) },
  { id: 6, name: "외국어", icon: "🌍", displayOrder: 6, image: getCategoryImage(6) },
  { id: 7, name: "독서", icon: "📚", displayOrder: 7, image: getCategoryImage(7) },
  { id: 8, name: "스터디", icon: "📖", displayOrder: 8, image: getCategoryImage(8) },
  { id: 9, name: "게임/오락", icon: "🎮", displayOrder: 9, image: getCategoryImage(9) },
  { id: 10, name: "사진/영상", icon: "📷", displayOrder: 10, image: getCategoryImage(10) },
  { id: 11, name: "요리", icon: "🍳", displayOrder: 11, image: getCategoryImage(11) },
  { id: 12, name: "공예", icon: "🎨", displayOrder: 12, image: getCategoryImage(12) },
  { id: 13, name: "자기계발", icon: "💪", displayOrder: 13, image: getCategoryImage(13) },
  { id: 14, name: "봉사활동", icon: "💝", displayOrder: 14, image: getCategoryImage(14) },
  { id: 15, name: "반려동물", icon: "🐾", displayOrder: 15, image: getCategoryImage(15) },
  { id: 16, name: "IT/개발", icon: "💻", displayOrder: 16, image: getCategoryImage(16) },
  { id: 17, name: "금융/재테크", icon: "💰", displayOrder: 17, image: getCategoryImage(17) },
  { id: 18, name: "기타", icon: "✨", displayOrder: 18, image: getCategoryImage(18) },
];

/**
 * 카테고리 목록 조회
 */
export const getCategories = async (): Promise<Category[]> => {
  try {
    const res = await publicAxios.get(prefix);
    return res.data;
  } catch {
    // API 실패 시 기본 카테고리 반환
    return DEFAULT_CATEGORIES;
  }
};
