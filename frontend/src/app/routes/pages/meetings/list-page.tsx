import { useState } from "react";
import { useUpcomingMeetings, useMyMeetings, MeetingCard } from "@/features/meetings";
import { EmptyState, Spinner } from "@/components/ui";

type TabType = "upcoming" | "my";

function MeetingListPage() {
  const [activeTab, setActiveTab] = useState<TabType>("upcoming");

  const { data: upcomingMeetings, isLoading: upcomingLoading } =
    useUpcomingMeetings();
  const { data: myMeetings, isLoading: myLoading } = useMyMeetings();

  const isLoading = activeTab === "upcoming" ? upcomingLoading : myLoading;
  const meetings = activeTab === "upcoming" ? upcomingMeetings : myMeetings;

  const tabs: { key: TabType; label: string }[] = [
    { key: "upcoming", label: "다가오는 정모" },
    { key: "my", label: "내 정모" },
  ];

  return (
    <div className="p-4">
      {/* Tabs */}
      <div className="flex gap-2 mb-6">
        {tabs.map((tab) => (
          <button
            key={tab.key}
            onClick={() => setActiveTab(tab.key)}
            className={`flex-1 py-2 text-sm font-medium rounded-lg transition-colors ${
              activeTab === tab.key
                ? "bg-primary-500 text-white"
                : "bg-gray-100 text-gray-600"
            }`}
          >
            {tab.label}
          </button>
        ))}
      </div>

      {/* Content */}
      {isLoading ? (
        <div className="flex items-center justify-center h-40">
          <Spinner />
        </div>
      ) : meetings && meetings.length > 0 ? (
        <div className="space-y-4">
          {meetings.map((meeting) => (
            <MeetingCard key={meeting.id} meeting={meeting} />
          ))}
        </div>
      ) : (
        <EmptyState
          icon="📅"
          title={
            activeTab === "upcoming"
              ? "다가오는 정모가 없습니다"
              : "참석 예정인 정모가 없습니다"
          }
          description="모임에 가입하고 정모에 참석해보세요!"
        />
      )}
    </div>
  );
}

export default MeetingListPage;
