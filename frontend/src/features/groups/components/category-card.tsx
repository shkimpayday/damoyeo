import type { Category } from "../types";
import { CategoryIcons, DefaultCategoryIcon } from "./category-icons";

interface CategoryCardProps {
  category: Category;
  isSelected?: boolean;
  onClick?: () => void;
}

/**
 * 문토 스타일 카테고리 Pill 버튼
 * - 가로 스크롤용 컴팩트 디자인
 * - 선택 시 Teal 배경
 */
export function CategoryCard({ category, isSelected, onClick }: CategoryCardProps) {
  const IconComponent = CategoryIcons[category.id] || DefaultCategoryIcon;

  return (
    <button
      onClick={onClick}
      className={`
        inline-flex items-center gap-2 px-4 py-2.5 rounded-full
        transition-all duration-200 whitespace-nowrap shrink-0
        ${
          isSelected
            ? "bg-primary-500 text-white shadow-md"
            : "bg-gray-50 text-gray-700 hover:bg-gray-100"
        }
      `}
    >
      {/* 작은 아이콘 */}
      <div className="w-6 h-6">
        <IconComponent className="w-full h-full" />
      </div>

      {/* 카테고리명 */}
      <span className={`text-sm font-semibold ${isSelected ? "text-white" : "text-gray-700"}`}>
        {category.name}
      </span>
    </button>
  );
}

/**
 * 문토 스타일 카테고리 아이콘 카드 (메인 페이지용)
 * - 둥근 모서리 배경이 아이콘에 포함됨
 * - 호버 시 살짝 확대
 */
interface CategoryIconCardProps {
  category: Category;
  onClick?: () => void;
}

export function CategoryIconCard({ category, onClick }: CategoryIconCardProps) {
  const IconComponent = CategoryIcons[category.id] || DefaultCategoryIcon;

  return (
    <button
      onClick={onClick}
      className="flex flex-col items-center gap-2 p-1 rounded-xl hover:scale-105 transition-transform"
    >
      {/* 아이콘 (배경 포함) */}
      <div className="w-14 h-14 rounded-2xl overflow-hidden shadow-sm">
        <IconComponent className="w-full h-full" />
      </div>
      {/* 카테고리명 */}
      <span className="text-xs text-gray-700 font-medium text-center leading-tight max-w-[60px] truncate">
        {category.name.split("/")[0]}
      </span>
    </button>
  );
}
