/**
 * ============================================================================
 * 프리미엄 뱃지 컴포넌트
 * ============================================================================
 *
 * [역할]
 * 프리미엄 회원임을 나타내는 뱃지를 표시합니다.
 */

import { Crown } from "lucide-react";

interface PremiumBadgeProps {
  size?: "sm" | "md" | "lg";
  showText?: boolean;
}

const sizeClasses = {
  sm: {
    container: "px-1.5 py-0.5 text-xs gap-0.5",
    icon: 10,
  },
  md: {
    container: "px-2 py-1 text-xs gap-1",
    icon: 12,
  },
  lg: {
    container: "px-3 py-1.5 text-sm gap-1.5",
    icon: 16,
  },
};

export function PremiumBadge({ size = "md", showText = true }: PremiumBadgeProps) {
  const classes = sizeClasses[size];

  return (
    <span
      className={`inline-flex items-center bg-gradient-to-r from-amber-400 to-orange-500 text-white font-semibold rounded-full ${classes.container}`}
    >
      <Crown size={classes.icon} />
      {showText && <span>프리미엄</span>}
    </span>
  );
}
