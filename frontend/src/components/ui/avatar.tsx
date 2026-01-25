import { getImageUrl } from "@/utils";

interface AvatarProps {
  src?: string;
  alt?: string;
  size?: "sm" | "md" | "lg" | "xl";
  className?: string;
}

const sizeClasses = {
  sm: "w-8 h-8 text-xs",
  md: "w-10 h-10 text-sm",
  lg: "w-12 h-12 text-base",
  xl: "w-16 h-16 text-lg",
};

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
      className={`rounded-full bg-gray-200 flex items-center justify-center overflow-hidden ${sizeClasses[size]} ${className}`}
    >
      {imageSrc ? (
        <img src={imageSrc} alt={alt} className="w-full h-full object-cover" />
      ) : (
        <span className="text-gray-500 font-medium">{initial}</span>
      )}
    </div>
  );
}
