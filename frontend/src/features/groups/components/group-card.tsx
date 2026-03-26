import { Link } from "react-router";
import { MapPin, Users, Crown, Shield, User } from "lucide-react";
import type { GroupListDTO } from "../types";
import { getImageUrl } from "@/utils";
import { memo } from "react";

/**
 * 모임 카드 컴포넌트 Props
 *
 * @property group - 모임 정보 DTO
 * @property variant - 카드 표시 형태 (default: 기본 세로형, compact: 가로형 컴팩트)
 */
interface GroupCardProps {
  group: GroupListDTO;
  variant?: "default" | "compact";
}

/**
 * 역할별 배지 설정 (아이콘 + 라벨 + 색상)
 */
const ROLE_CONFIG = {
  OWNER: { icon: Crown, label: "모임장", color: "bg-amber-100 text-amber-700" },
  MANAGER: { icon: Shield, label: "운영진", color: "bg-blue-100 text-blue-700" },
  MEMBER: { icon: User, label: "멤버", color: "bg-gray-100 text-gray-600" },
} as const;

/**
 * 역할에 따른 배지 컴포넌트
 */
function RoleBadge({ role }: { role: string }) {
  const config = ROLE_CONFIG[role as keyof typeof ROLE_CONFIG] ?? ROLE_CONFIG.MEMBER;
  const Icon = config.icon;

  return (
    <span className={`inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-xs font-medium ${config.color}`}>
      <Icon size={12} />
      {config.label}
    </span>
  );
}

/**
 * 모임 카드 컴포넌트
 *
 * @description
 * - default: 기존 세로형 레이아웃 (이미지 상단, 정보 하단)
 * - compact: 가로형 컴팩트 레이아웃 (이미지 좌측 작게, 정보 우측)
 */
export const GroupCard = memo(function GroupCard({
  group,
  variant = "default",
}: GroupCardProps) {
  const categoryIcon = group.category?.icon || "🎉";
  const categoryName = group.category?.name || "";

  // 컴팩트 버전 렌더링
  if (variant === "compact") {
    return (
      <Link
        to={`/groups/${group.id}`}
        className="flex bg-white rounded-xl shadow-[0_1px_3px_rgba(0,0,0,0.08)] hover:shadow-[0_4px_12px_rgba(0,0,0,0.12)] hover:scale-[1.01] transition-all duration-200 overflow-hidden"
      >
        {/* 좌측 썸네일 - 정사각형 */}
        <div className="relative w-24 h-24 flex-shrink-0 bg-gray-100">
          {group.thumbnailImage || group.coverImage ? (
            <img
              src={getImageUrl(group.thumbnailImage) || group.coverImage}
              alt={group.name}
              className="w-full h-full object-cover"
            />
          ) : (
            <div className="w-full h-full flex items-center justify-center bg-gradient-to-br from-primary-50 to-primary-100">
              <span className="text-3xl">{categoryIcon}</span>
            </div>
          )}
        </div>

        {/* 우측 정보 */}
        <div className="flex-1 p-3 min-w-0 flex flex-col justify-between">
          {/* 상단: 카테고리 + 역할 */}
          <div className="flex items-center justify-between gap-2">
            <span className="text-xs text-gray-500 truncate">
              {categoryIcon} {categoryName}
            </span>
            {group.myRole && <RoleBadge role={group.myRole} />}
          </div>

          {/* 모임명 */}
          <h3 className="font-bold text-gray-900 truncate text-sm mt-1">
            {group.name}
          </h3>

          {/* 하단: 위치 + 인원 */}
          <div className="flex items-center justify-between mt-1.5">
            <div className="flex items-center gap-1 text-xs text-gray-500 min-w-0">
              <MapPin size={12} className="text-gray-400 flex-shrink-0" />
              <span className="truncate">{group.address || "위치 미정"}</span>
            </div>
            <div className="flex items-center gap-1 text-xs text-gray-600 flex-shrink-0 ml-2">
              <Users size={12} className="text-primary-500" />
              <span>
                {group.memberCount}/{group.maxMembers}
              </span>
            </div>
          </div>
        </div>
      </Link>
    );
  }

  // 기본 버전 렌더링
  return (
    <Link
      to={`/groups/${group.id}`}
      className="block bg-white rounded-2xl shadow-[0_0_4px_rgba(0,0,0,0.06)] hover:shadow-[0_2px_12px_rgba(0,0,0,0.1)] hover:scale-[1.01] transition-all duration-200 overflow-hidden"
    >
      {/* Thumbnail - 4:3 비율 */}
      <div className="relative aspect-4/3 bg-gray-100">
        {group.thumbnailImage || group.coverImage ? (
          <img
            src={getImageUrl(group.thumbnailImage) || group.coverImage}
            alt={group.name}
            className="w-full h-full object-cover"
          />
        ) : (
          <div className="w-full h-full flex items-center justify-center bg-gradient-to-br from-primary-50 to-primary-100">
            <span className="text-6xl">{categoryIcon}</span>
          </div>
        )}
        {/* Category Badge */}
        <div className="absolute top-3 left-3 px-3 py-1.5 bg-white/95 backdrop-blur-sm rounded-full text-sm font-semibold text-gray-700 shadow-sm">
          {categoryIcon} {categoryName}
        </div>
        {/* Distance Badge */}
        {group.distance !== undefined && (
          <div className="absolute top-3 right-3 px-2.5 py-1.5 bg-primary-500 text-white rounded-full text-sm font-semibold shadow-sm">
            {group.distance < 1
              ? `${Math.round(group.distance * 1000)}m`
              : `${group.distance.toFixed(1)}km`}
          </div>
        )}
        {/* Role Badge */}
        {group.myRole && (
          <div className="absolute bottom-3 left-3">
            <RoleBadge role={group.myRole} />
          </div>
        )}
      </div>

      {/* Content */}
      <div className="p-5">
        <h3 className="font-bold text-gray-900 truncate text-xl">{group.name}</h3>

        <div className="mt-3 flex items-center gap-2 text-lg text-gray-500">
          <MapPin size={18} className="text-gray-400 flex-shrink-0" />
          <span className="truncate">{group.address || "위치 미정"}</span>
        </div>

        <div className="mt-3.5 flex items-center gap-2">
          <Users size={18} className="text-primary-500" />
          <span className="text-lg text-gray-600 font-medium">
            {group.memberCount}
            <span className="text-gray-400">/{group.maxMembers}명</span>
          </span>

          {/* 멤버 수 프로그레스 바 */}
          <div className="flex-1 h-2 bg-gray-100 rounded-full overflow-hidden ml-2">
            <div
              className="h-full bg-primary-400 rounded-full transition-all"
              style={{
                width: `${Math.min((group.memberCount / group.maxMembers) * 100, 100)}%`,
              }}
            />
          </div>
        </div>
      </div>
    </Link>
  );
});
