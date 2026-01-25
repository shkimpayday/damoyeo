import { useState, useEffect } from "react";
import { Link } from "react-router";
import { X } from "lucide-react";

interface TopPromoBannerProps {
  id: string; // 배너 고유 ID (localStorage 키로 사용)
  linkUrl: string;
  backgroundColor?: string;
  textColor?: string;
  children: React.ReactNode;
}

export function TopPromoBanner({
  id,
  linkUrl,
  backgroundColor = "#7C3AED", // 기본 보라색
  textColor = "white",
  children,
}: TopPromoBannerProps) {
  const [isVisible, setIsVisible] = useState(false);
  const storageKey = `promo-banner-closed-${id}`;

  // 마운트 시 localStorage 확인
  useEffect(() => {
    const isClosed = localStorage.getItem(storageKey);
    if (!isClosed) {
      setIsVisible(true);
    }
  }, [storageKey]);

  // 닫기 버튼 클릭
  const handleClose = (e: React.MouseEvent) => {
    e.preventDefault();
    e.stopPropagation();
    setIsVisible(false);
    // 24시간 동안 숨기기 (또는 영구히 숨기려면 "true"만 저장)
    localStorage.setItem(storageKey, Date.now().toString());
  };

  if (!isVisible) return null;

  return (
    <div
      className="relative w-full h-16"
      style={{ backgroundColor }}
    >
      {/* 중앙 정렬 컨테이너 */}
      <div className="max-w-[1024px] mx-auto w-full lg:w-[80%] lg:max-w-[1200px] relative">
        <Link
          to={linkUrl}
          className="flex items-center justify-center py-2.5 px-10 text-sm"
          style={{ color: textColor }}
        >
          {children}
        </Link>
        <button
          onClick={handleClose}
          className="absolute right-3 top-1/2 -translate-y-1/2 mt- p-1 pt-2 rounded-full hover:bg-white/20 transition-colors"
          aria-label="배너 닫기"
          style={{ color: textColor }}
        >
          <X size={25} />
        </button>
      </div>
    </div>
  );
}
