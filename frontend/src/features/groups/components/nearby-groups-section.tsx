import { Link } from "react-router";
import { ChevronRight } from "lucide-react";
import { useNearbyGroups, useCurrentLocation } from "../hooks";
import { GroupCard } from "./group-card";
import { Spinner } from "@/components/ui";

export function NearbyGroupsSection() {
  const { lat, lng, loading: locationLoading, error, requestLocation, hasLocation } = useCurrentLocation();
  const { data: nearbyGroups, isLoading: groupsLoading } = useNearbyGroups(
    lat ?? 0,
    lng ?? 0,
    5 // 반경 5km
  );

  // 위치 권한 요청 전
  if (!hasLocation && !locationLoading && !error) {
    return (
      <div className="app-content">
        <h2 className="text-xl font-bold text-gray-900 mb-4">내 근처 모임</h2>
        <div className="bg-white rounded-2xl p-8 text-center shadow-sm">
          <span className="text-5xl">📍</span>
          <p className="mt-4 text-gray-600">
            위치 권한을 허용하면 근처 모임을 찾을 수 있어요
          </p>
          <button
            onClick={requestLocation}
            className="mt-5 px-8 py-3 bg-primary-500 text-white rounded-full font-semibold hover:bg-primary-600 transition-colors shadow-lg shadow-primary-500/25"
          >
            위치 허용하기
          </button>
        </div>
      </div>
    );
  }

  // 위치 로딩 중
  if (locationLoading) {
    return (
      <div className="app-content">
        <h2 className="text-xl font-bold text-gray-900 mb-4">내 근처 모임</h2>
        <div className="flex items-center justify-center h-40">
          <div className="text-center">
            <Spinner size="lg" />
            <p className="mt-3 text-sm text-gray-500">위치를 확인하고 있어요...</p>
          </div>
        </div>
      </div>
    );
  }

  // 위치 오류
  if (error) {
    return (
      <div className="app-content">
        <h2 className="text-xl font-bold text-gray-900 mb-4">내 근처 모임</h2>
        <div className="bg-red-50 rounded-2xl p-6 text-center">
          <span className="text-3xl">⚠️</span>
          <p className="mt-3 text-sm text-red-600">{error}</p>
          <button
            onClick={requestLocation}
            className="mt-4 px-6 py-2.5 text-sm bg-red-100 text-red-700 rounded-full font-medium hover:bg-red-200 transition-colors"
          >
            다시 시도
          </button>
        </div>
      </div>
    );
  }

  // 모임 로딩 중
  if (groupsLoading) {
    return (
      <div className="app-content">
        <h2 className="text-xl font-bold text-gray-900 mb-4">내 근처 모임</h2>
        <div className="flex items-center justify-center h-40">
          <Spinner size="lg" />
        </div>
      </div>
    );
  }

  // 근처 모임이 없음
  if (!nearbyGroups || nearbyGroups.length === 0) {
    return (
      <div className="app-content">
        <h2 className="text-xl font-bold text-gray-900 mb-4">내 근처 모임</h2>
        <div className="bg-white rounded-2xl p-8 text-center shadow-sm">
          <span className="text-5xl">🔍</span>
          <p className="mt-4 text-gray-600">
            근처에 활동 중인 모임이 없어요
          </p>
          <Link
            to="/groups/create"
            className="inline-block mt-5 px-8 py-3 bg-primary-500 text-white rounded-full font-semibold hover:bg-primary-600 transition-colors shadow-lg shadow-primary-500/25"
          >
            첫 모임 만들기
          </Link>
        </div>
      </div>
    );
  }

  // 근처 모임 표시
  return (
    <div className="app-content">
      <div className="flex items-center justify-between mb-6">
        <h2 className="text-xl font-bold text-gray-900">내 근처 모임</h2>
        <Link
          to={`/groups/list?lat=${lat}&lng=${lng}`}
          className="flex items-center gap-1 text-sm text-gray-500 hover:text-primary-600 transition-colors"
        >
          전체보기
          <ChevronRight size={16} />
        </Link>
      </div>

      <div className="grid-groups">
        {nearbyGroups.slice(0, 5).map((group) => (
          <GroupCard key={group.id} group={group} />
        ))}
      </div>
    </div>
  );
}
