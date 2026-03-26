import { getImageUrl } from "@/utils";

interface AvatarProps {
  src?: string;
  alt?: string;
  size?: "sm" | "md" | "lg" | "xl" | "2xl";
  className?: string;
}

/**
 * 아바타 크기별 Tailwind 클래스 설정
 */
const SIZE_CLASSES = {
  sm: "w-8 h-8 text-xs",
  md: "w-10 h-10 text-sm",
  lg: "w-12 h-12 text-base",
  xl: "w-16 h-16 text-lg",
  "2xl": "w-24 h-24 text-xl",
} as const;

export function Avatar({
  src,
  alt = "",
  size = "md",
  className = "",
}: AvatarProps) {
  const initial = alt?.charAt(0) || "?";
  const imageSrc = getImageUrl(src);

  return (
    <div
      className={`rounded-full bg-gray-200 flex items-center justify-center overflow-hidden ${SIZE_CLASSES[size]} ${className}`}
    >
      {imageSrc ? (
        <img src={imageSrc} alt={alt} className="w-full h-full object-cover" />
      ) : (
        <span className="text-gray-500 font-medium">{initial}</span>
      )}
    </div>
  );
}
