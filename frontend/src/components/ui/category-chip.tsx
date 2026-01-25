import type { Category } from "@/features/groups/types";

interface CategoryChipProps {
  category: Category;
  isSelected?: boolean;
  onClick?: () => void;
  size?: "sm" | "md";
}

export function CategoryChip({
  category,
  isSelected = false,
  onClick,
  size = "md",
}: CategoryChipProps) {
  const baseClasses =
    "inline-flex items-center gap-1 rounded-full transition-colors";

  const sizeClasses = {
    sm: "px-2 py-1 text-xs",
    md: "px-3 py-1.5 text-sm",
  };

  const stateClasses = isSelected
    ? "bg-primary-100 text-primary-700 border-primary-300"
    : "bg-gray-100 text-gray-700 border-gray-200 hover:bg-gray-200";

  const clickableClasses = onClick ? "cursor-pointer border" : "border";

  return (
    <span
      onClick={onClick}
      className={`${baseClasses} ${sizeClasses[size]} ${stateClasses} ${clickableClasses}`}
    >
      <span>{category.icon}</span>
      <span>{category.name}</span>
    </span>
  );
}
