import { useQuery } from "@tanstack/react-query";
import { jwtAxios } from "@/lib/axios";
import { ENV } from "@/config";
import { Spinner } from "@/components/ui";

/**
 * 관리자 대시보드 페이지
 *
 * [표시 정보]
 * - 전체 회원 수
 * - 전체 모임 수
 * - 전체 정모 수
 * - 오늘 신규 가입자 수
 * - 최근 활동 요약
 */

interface DashboardStats {
  totalMembers: number;
  totalGroups: number;
  totalMeetings: number;
  todayNewMembers: number;
  activeGroups: number;
  upcomingMeetings: number;
}

/**
 * 대시보드 통계 조회 API
 */
const fetchDashboardStats = async (): Promise<DashboardStats> => {
  const res = await jwtAxios.get(`${ENV.API_URL}/api/admin/dashboard/stats`);
  return res.data;
};

/**
 * 대시보드 통계 조회 Hook
 */
const useDashboardStats = () => {
  return useQuery({
    queryKey: ["admin", "dashboard", "stats"],
    queryFn: fetchDashboardStats,
    staleTime: 60 * 1000, // 1분
    retry: 1,
  });
};

// 통계 카드 컴포넌트
function StatCard({
  title,
  value,
  icon,
  color,
  subtext,
}: {
  title: string;
  value: number | string;
  icon: string;
  color: string;
  subtext?: string;
}) {
  return (
    <div className="bg-white rounded-xl shadow-sm p-6 border border-gray-100">
      <div className="flex items-start justify-between">
        <div>
          <p className="text-sm text-gray-500 mb-1">{title}</p>
          <p className="text-3xl font-bold text-gray-800">{value}</p>
          {subtext && <p className="text-xs text-gray-400 mt-1">{subtext}</p>}
        </div>
        <div className={`w-12 h-12 rounded-lg ${color} flex items-center justify-center`}>
          <span className="text-2xl">{icon}</span>
        </div>
      </div>
    </div>
  );
}

export function DashboardPage() {
  const { data: stats, isLoading, isError } = useDashboardStats();

  if (isLoading) {
    return (
      <div className="flex items-center justify-center h-64">
        <Spinner size="lg" />
      </div>
    );
  }

  const displayStats: DashboardStats = stats || {
    totalMembers: 0,
    totalGroups: 0,
    totalMeetings: 0,
    todayNewMembers: 0,
    activeGroups: 0,
    upcomingMeetings: 0,
  };

  return (
    <div>
      {/* 페이지 헤더 */}
      <div className="mb-6">
        <h1 className="text-2xl font-bold text-gray-800">대시보드</h1>
        <p className="text-gray-500 mt-1">다모여 서비스 현황을 한눈에 확인하세요</p>
      </div>

      {/* API 에러 표시 */}
      {isError && (
        <div className="bg-yellow-50 border border-yellow-200 rounded-lg p-4 mb-6">
          <p className="text-yellow-700">
            ⚠️ 데이터를 불러오는 데 실패했습니다. 잠시 후 다시 시도해주세요.
          </p>
        </div>
      )}

      {/* 통계 카드 그리드 */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6 mb-8">
        <StatCard
          title="전체 회원"
          value={displayStats.totalMembers.toLocaleString()}
          icon="👥"
          color="bg-blue-100"
          subtext={`오늘 +${displayStats.todayNewMembers}명`}
        />
        <StatCard
          title="전체 모임"
          value={displayStats.totalGroups.toLocaleString()}
          icon="🏠"
          color="bg-green-100"
          subtext={`활성 ${displayStats.activeGroups}개`}
        />
        <StatCard
          title="전체 정모"
          value={displayStats.totalMeetings.toLocaleString()}
          icon="📅"
          color="bg-purple-100"
          subtext={`예정 ${displayStats.upcomingMeetings}개`}
        />
      </div>

      {/* 빠른 액션 */}
      <div className="bg-white rounded-xl shadow-sm p-6 border border-gray-100">
        <h2 className="text-lg font-semibold text-gray-800 mb-4">빠른 액션</h2>
        <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
          <a
            href="/admin/members"
            className="flex flex-col items-center p-4 bg-gray-50 rounded-lg hover:bg-gray-100 transition-colors"
          >
            <span className="text-2xl mb-2">👤</span>
            <span className="text-sm text-gray-600">회원 관리</span>
          </a>
          <a
            href="/admin/groups"
            className="flex flex-col items-center p-4 bg-gray-50 rounded-lg hover:bg-gray-100 transition-colors"
          >
            <span className="text-2xl mb-2">🏠</span>
            <span className="text-sm text-gray-600">모임 관리</span>
          </a>
          <a
            href="/admin/events"
            className="flex flex-col items-center p-4 bg-gray-50 rounded-lg hover:bg-gray-100 transition-colors"
          >
            <span className="text-2xl mb-2">🎉</span>
            <span className="text-sm text-gray-600">이벤트 관리</span>
          </a>
          <a
            href="/"
            target="_blank"
            className="flex flex-col items-center p-4 bg-gray-50 rounded-lg hover:bg-gray-100 transition-colors"
          >
            <span className="text-2xl mb-2">🌐</span>
            <span className="text-sm text-gray-600">사이트 보기</span>
          </a>
        </div>
      </div>
    </div>
  );
}

export default DashboardPage;
