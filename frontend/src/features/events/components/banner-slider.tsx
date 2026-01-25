import { useState, useEffect, useCallback } from "react";
import { Link } from "react-router";
import type { EventBanner } from "../types";

interface BannerSliderProps {
  banners: EventBanner[];
  autoPlayInterval?: number;
}

export function BannerSlider({
  banners,
  autoPlayInterval = 4000,
}: BannerSliderProps) {
  const [currentIndex, setCurrentIndex] = useState(0);
  const [isAutoPlaying, setIsAutoPlaying] = useState(true);

  const goToNext = useCallback(() => {
    setCurrentIndex((prev) => (prev + 1) % banners.length);
  }, [banners.length]);

  const goToPrev = useCallback(() => {
    setCurrentIndex((prev) => (prev - 1 + banners.length) % banners.length);
  }, [banners.length]);

  const goToSlide = (index: number) => {
    setCurrentIndex(index);
    setIsAutoPlaying(false);
    // 5초 후 자동 재생 재개
    setTimeout(() => setIsAutoPlaying(true), 5000);
  };

  // 자동 슬라이드
  useEffect(() => {
    if (!isAutoPlaying || banners.length <= 1) return;

    const timer = setInterval(goToNext, autoPlayInterval);
    return () => clearInterval(timer);
  }, [isAutoPlaying, autoPlayInterval, goToNext, banners.length]);

  if (banners.length === 0) return null;

  return (
    <div className="relative w-full overflow-hidden bg-gray-100">
      {/* 슬라이드 컨테이너 */}
      <div
        className="flex transition-transform duration-500 ease-out"
        style={{ transform: `translateX(-${currentIndex * 100}%)` }}
      >
        {banners.map((banner) => (
          <Link
            key={banner.id}
            to={banner.linkUrl}
            className="w-full flex-shrink-0"
          >
            <div className="relative aspect-5/1">
              <img
                src={banner.imageUrl}
                alt={banner.title}
                className="w-full h-full object-cover"
              />
              {/* 그라데이션 오버레이 */}
              <div className="absolute inset-0 bg-gradient-to-t from-black/60 via-transparent to-transparent" />
              {/* 텍스트 - clamp()로 부드러운 반응형 */}
              <div
                className="absolute right-[4%] tracking-tighter text-white"
                style={{
                  bottom: 'clamp(1rem, 4vw, 5rem)',
                  left: 'clamp(1rem, 15vw, 22.5rem)',
                  padding: 'clamp(0.5rem, 1vw, 1rem)',
                }}
              >
                <h3
                  className="font-bold tracking-tighter line-clamp-2"
                  style={{
                    fontSize: 'clamp(1.125rem, 4vw, 3rem)',
                    paddingBottom: 'clamp(0.25rem, 1vw, 1.25rem)',
                  }}
                >
                  {banner.title}
                </h3>
                <p
                  className="text-white/80 line-clamp-1 tracking-tighter"
                  style={{
                    fontSize: 'clamp(0.875rem, 2.5vw, 1.875rem)',
                    marginTop: 'clamp(0.125rem, 0.5vw, 0.25rem)',
                  }}
                >
                  {banner.description}
                </p>
                <p
                  className="text-white/80 line-clamp-1"
                  style={{
                    fontSize: 'clamp(0.75rem, 1.2vw, 1.125rem)',
                    marginTop: 'clamp(0.25rem, 1vw, 1.25rem)',
                  }}
                >
                  자세히 보기 {`>`}
                </p>
              </div>
            </div>
          </Link>
        ))}
      </div>

      {/* 좌우 화살표 */}
      {banners.length > 1 && (
        <>
          <button
            onClick={(e) => {
              e.preventDefault();
              goToPrev();
              setIsAutoPlaying(false);
              setTimeout(() => setIsAutoPlaying(true), 5000);
            }}
            className="absolute top-1/2 -translate-y-1/2 bg-black/30 hover:bg-black/50 rounded-full flex items-center justify-center text-white transition-colors"
            style={{
              left: 'clamp(0.5rem, 12vw, 17.5rem)',
              width: 'clamp(2rem, 3vw, 2.5rem)',
              height: 'clamp(2rem, 3vw, 2.5rem)',
            }}
            aria-label="이전 배너"
          >
            <svg
              className="w-6 h-6"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={2}
                d="M15 19l-7-7 7-7"
              />
            </svg>
          </button>
          <button
            onClick={(e) => {
              e.preventDefault();
              goToNext();
              setIsAutoPlaying(false);
              setTimeout(() => setIsAutoPlaying(true), 5000);
            }}
            className="absolute top-1/2 -translate-y-1/2 bg-black/30 hover:bg-black/50 rounded-full flex items-center justify-center text-white transition-colors"
            style={{
              right: 'clamp(0.5rem, 12vw, 17.5rem)',
              width: 'clamp(2rem, 3vw, 2.5rem)',
              height: 'clamp(2rem, 3vw, 2.5rem)',
            }}
            aria-label="다음 배너"
          >
            <svg
              className="w-6 h-6"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={2}
                d="M9 5l7 7-7 7"
              />
            </svg>
          </button>
        </>
      )}

      {/* 인디케이터 */}
      {banners.length > 1 && (
        <div
          className="absolute left-1/2 -translate-x-1/2 flex gap-1.5"
          style={{ bottom: 'clamp(1.5rem, 3vw, 4rem)' }}
        >
          {banners.map((_, index) => (
            <button
              key={index}
              onClick={() => goToSlide(index)}
              className={`w-2 h-2 rounded-full transition-all ${
                index === currentIndex
                  ? "bg-white w-4"
                  : "bg-white/50 hover:bg-white/70"
              }`}
              aria-label={`${index + 1}번 배너로 이동`}
            />
          ))}
        </div>
      )}

      {/* 페이지 번호 */}
      {banners.length > 1 && (
        <div className="absolute bottom-4 right-4 px-2 py-1 bg-black/40 rounded-full text-white text-sm">
          {currentIndex + 1} / {banners.length}
        </div>
      )}
    </div>
  );
}
