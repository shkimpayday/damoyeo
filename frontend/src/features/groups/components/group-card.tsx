import { Link } from "react-router";
import { MapPin, Users } from "lucide-react";
import type { GroupListDTO } from "../types";

interface GroupCardProps {
  group: GroupListDTO;
}

export function GroupCard({ group }: GroupCardProps) {
  // 카테고리 정보 추출 (중첩 객체)
  const categoryIcon = group.category?.icon || "🎉";
  const categoryName = group.category?.name || "";

  return (
    <Link
      to={`/groups/${group.id}`}
      className="block bg-white rounded-2xl shadow-[0_0_4px_rgba(0,0,0,0.06)] hover:shadow-[0_2px_12px_rgba(0,0,0,0.1)] hover:scale-[1.01] transition-all duration-200 overflow-hidden"
    >
      {/* Thumbnail - 4:3 비율로 세로 줄임 */}
      <div className="relative aspect-4/3 bg-gray-100">
        {group.thumbnailImage || group.coverImage ? (
          <img
            src={group.thumbnailImage || group.coverImage}
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
}
