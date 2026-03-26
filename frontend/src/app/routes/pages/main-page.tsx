import { useState } from "react";
import { Link } from "react-router";
import { ChevronRight } from "lucide-react";
import {
  useRecommendedGroups,
  useGroupsList,
  GroupCard,
  DEFAULT_CATEGORIES,
  NearbyGroupsSection,
  CategoryIcons,
  DefaultCategoryIcon,
} from "@/features/groups";
import { useUpcomingMeetings, MeetingCard } from "@/features/meetings";
import { useEventBanners, BannerSlider } from "@/features/events";
import { useAuth } from "@/features/auth";
import { Spinner } from "@/components/ui";

type TabType = "recommend" | "new" | "popular";

function MainPage() {
  const { isLoggedIn } = useAuth();
  const [activeTab, setActiveTab] = useState<TabType>("recommend");

  // 탭별 데이터 조회
  const { data: recommendedGroups, isLoading: recommendLoading } =
    useRecommendedGroups();
  const { data: newGroupsPage, isLoading: newLoading } = useGroupsList({
    sort: "latest",
    size: 10,
    page: 1,
  });
  const { data: popularGroupsPage, isLoading: popularLoading } = useGroupsList({
    sort: "popular",
    size: 10,
    page: 1,
  });

  // 현재 탭에 맞는 데이터 선택
  const groupsLoading =
    activeTab === "recommend"
      ? recommendLoading
      : activeTab === "new"
        ? newLoading
        : popularLoading;

  const displayGroups =
    activeTab === "recommend"
      ? recommendedGroups ?? []
      : activeTab === "new"
        ? (newGroupsPage?.dtoList ?? [])
        : (popularGroupsPage?.dtoList ?? []);

  const { data: upcomingMeetings, isLoading: meetingsLoading } =
    useUpcomingMeetings();
  const { data: eventBanners } = useEventBanners();

  const tabs = [
    { id: "recommend" as TabType, label: "추천" },
    { id: "new" as TabType, label: "신규" },
    { id: "popular" as TabType, label: "인기" },
  ];

  return (
    <div className="pb-8">
      {/* 이벤트 배너 슬라이더 - 전체 너비 breakout */}
      {eventBanners && eventBanners.length > 0 && (
        <div className="full-bleed mb-1.5">
          <BannerSlider banners={eventBanners} autoPlayInterval={4000} />
        </div>
      )}

      {/* 카테고리 섹션 - 흰 배경 전체 너비 */}
      <section className="section-spacing bg-white full-bleed">
        <div className="app-content">
          <div className="flex items-center justify-between mb-6">
            <h2 className="text-xl font-bold text-gray-900">카테고리</h2>
            <Link
              to="/groups/list"
              className="flex items-center gap-1 text-sm text-gray-500 hover:text-primary-600 transition-colors"
            >
              전체보기
              <ChevronRight size={16} />
            </Link>
          </div>
          <div className="grid-categories">
            {DEFAULT_CATEGORIES.slice(0, 10).map((category) => {
              const IconComponent =
                CategoryIcons[category.id] || DefaultCategoryIcon;
              return (
                <Link
                  key={category.id}
                  to={`/groups/list?categoryId=${category.id}`}
                  className="flex flex-col items-center gap-2 p-2 rounded-xl hover:scale-105 transition-transform"
                >
                  <div className="w-16 h-16 sm:w-20 sm:h-20 rounded-3xl overflow-hidden shadow-sm">
                    <IconComponent className="w-full h-full" />
                  </div>
                  <span className="text-xs sm:text-sm text-gray-700 font-medium text-center leading-tight">
                    {category.name.split("/")[0]}
                  </span>
                </Link>
              );
            })}
          </div>
        </div>
      </section>

      {/* Nearby Groups - 회색 배경 전체 너비 */}
      {isLoggedIn && (
        <section className="section-spacing bg-gray-50 full-bleed">
          <div className="app-content">
            <NearbyGroupsSection />
          </div>
        </section>
      )}

      {/* 모임 탭 섹션 - 흰 배경 전체 너비 */}
      <section className="section-spacing bg-white full-bleed">
        <div className="app-content">
          <div className="flex items-center justify-between mb-6">
            <div className="flex items-center gap-6">
              {tabs.map((tab) => (
                <button
                  key={tab.id}
                  onClick={() => setActiveTab(tab.id)}
                  className={`text-xl font-bold transition-colors relative pb-1 ${
                    activeTab === tab.id
                      ? "text-gray-900"
                      : "text-gray-300 hover:text-gray-500"
                  }`}
                >
                  {tab.label}
                  {activeTab === tab.id && (
                    <div className="absolute bottom-0 left-0 right-0 h-0.5 bg-primary-500 rounded-full" />
                  )}
                </button>
              ))}
            </div>
            <Link
              to="/groups/list"
              className="flex items-center gap-1 text-sm text-gray-500 hover:text-primary-600 transition-colors"
            >
              전체보기
              <ChevronRight size={16} />
            </Link>
          </div>

          {groupsLoading ? (
            <div className="flex items-center justify-center h-60">
              <Spinner size="lg" />
            </div>
          ) : displayGroups.length > 0 ? (
            <div className="grid-groups">
              {displayGroups.map((group) => (
                <GroupCard key={group.id} group={group} />
              ))}
            </div>
          ) : (
            <div className="text-center py-16 text-gray-500">
              <div className="text-5xl mb-4">🔍</div>
              <p className="font-semibold text-lg">아직 모임이 없습니다</p>
              <p className="text-sm mt-2 text-gray-400">
                첫 번째 모임을 만들어보세요!
              </p>
              <Link
                to="/groups/create"
                className="inline-block mt-6 px-8 py-3 bg-primary-500 text-white rounded-full font-semibold hover:bg-primary-600 transition-colors"
              >
                모임 만들기
              </Link>
            </div>
          )}
        </div>
      </section>

      {/* 다가오는 정모 - 회색 배경 전체 너비 */}
      {isLoggedIn && (
        <section className="section-spacing bg-gray-50 full-bleed">
          <div className="app-content">
            <div className="flex items-center justify-between mb-6">
              <h2 className="text-xl font-bold text-gray-900">다가오는 정모</h2>
              <Link
                to="/meetings"
                className="flex items-center gap-1 text-sm text-gray-500 hover:text-primary-600 transition-colors"
              >
                전체보기
                <ChevronRight size={16} />
              </Link>
            </div>

            {meetingsLoading ? (
              <div className="flex items-center justify-center h-40">
                <Spinner />
              </div>
            ) : upcomingMeetings && upcomingMeetings.length > 0 ? (
              <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
                {upcomingMeetings.slice(0, 6).map((meeting) => (
                  <MeetingCard key={meeting.id} meeting={meeting} />
                ))}
              </div>
            ) : (
              <div className="text-center py-12 text-gray-500">
                <p>예정된 정모가 없습니다.</p>
              </div>
            )}
          </div>
        </section>
      )}

      {/* 비로그인 사용자용 CTA */}
      {!isLoggedIn && (
        <section className="section-spacing bg-white full-bleed">
          <div className="app-content">
          <div className="p-8 sm:p-12 bg-gradient-to-br from-primary-50 to-primary-100 rounded-3xl text-center">
            <div className="w-20 h-20 mx-auto mb-6 bg-white rounded-full flex items-center justify-center shadow-md">
              <span className="text-4xl">🎉</span>
            </div>
            <h3 className="font-bold text-gray-900 text-2xl">
              다모여와 함께 시작하세요
            </h3>
            <p className="mt-3 text-gray-600 max-w-md mx-auto">
              취향이 맞는 사람들과 함께하는 즐거운 모임,
              <br />
              지금 바로 시작해보세요!
            </p>
            <Link
              to="/member/login"
              className="inline-block mt-6 px-10 py-4 bg-primary-500 text-white rounded-full font-semibold text-lg hover:bg-primary-600 transition-colors shadow-lg shadow-primary-500/25"
            >
              시작하기
            </Link>
          </div>
          </div>
        </section>
      )}
    </div>
  );
}

export default MainPage;
