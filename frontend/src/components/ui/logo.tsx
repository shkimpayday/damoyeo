interface LogoProps {
  className?: string;
  size?: "sm" | "md" | "lg";
}

/**
 * 다모여 SVG 텍스트 로고
 * - 굵고 날카로운 이탤릭 스타일
 * - Teal 그라데이션
 */
export function Logo({ className = "", size = "md" }: LogoProps) {
  const heights = {
    sm: "h-9",
    md: "h-11",
    lg: "h-13",
  };

  return (
    <svg
      viewBox="0 0 150 40"
      className={`${heights[size]} ${className}`}
      fill="none"
      xmlns="http://www.w3.org/2000/svg"
      aria-label="다모여 로고"
    >
      <defs>
        <linearGradient id="logoGradient" x1="0%" y1="0%" x2="100%" y2="100%">
          <stop offset="0%" stopColor="#12B886" />
          <stop offset="100%" stopColor="#087F5B" />
        </linearGradient>
      </defs>
      <text
        x="4"
        y="30"
        fill="url(#logoGradient)"
        fontFamily="'Pretendard', 'Noto Sans KR', sans-serif"
        fontSize="28"
        fontWeight="900"
        fontStyle="italic"
        letterSpacing="-1"
        style={{
          transform: "skewX(-8deg)",
          transformOrigin: "center",
        }}
      >
        DAMOYEO
      </text>
    </svg>
  );
}
