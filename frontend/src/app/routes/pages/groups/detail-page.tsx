import { useState } from "react";
import { useParams, useNavigate, Link } from "react-router";
import { MapPin, Users, ChevronLeft, Share2, Settings } from "lucide-react";
import {
  useGroupDetail,
  useGroupMembers,
  useJoinGroup,
  useLeaveGroup,
} from "@/features/groups";
import { useMeetingsByGroup, MeetingCard } from "@/features/meetings";
import { useAuth } from "@/features/auth";
import { Avatar, EmptyState, Spinner, ResultModal } from "@/components/ui";

type TabType = "info" | "meetings" | "members";

function GroupDetailPage() {
  const { groupId } = useParams<{ groupId: string }>();
  const navigate = useNavigate();
  const { isLoggedIn } = useAuth();
  const [activeTab, setActiveTab] = useState<TabType>("info");
  const [showModal, setShowModal] = useState(false);
  const [modalContent, setModalContent] = useState({ title: "", content: "" });

  const { data: group, isLoading } = useGroupDetail(Number(groupId));
  const { data: members } = useGroupMembers(Number(groupId));
  const { data: meetings } = useMeetingsByGroup(Number(groupId));
  const joinMutation = useJoinGroup();
  const leaveMutation = useLeaveGroup();

  if (isLoading) {
    return (
      <div className="flex items-center justify-center h-64">
        <Spinner size="lg" />
      </div>
    );
  }

  if (!group) {
    return (
      <div className="p-4">
        <EmptyState
          icon="❌"
          title="모임을 찾을 수 없습니다"
          action={{ label: "돌아가기", onClick: () => navigate(-1) }}
        />
      </div>
    );
  }

  const handleJoin = async () => {
    if (!isLoggedIn) {
      navigate("/member/login");
      return;
    }

    try {
      await joinMutation.mutateAsync(group.id);
      setModalContent({ title: "가입 신청 완료", content: "모임장의 승인을 기다려주세요." });
      setShowModal(true);
    } catch {
      setModalContent({ title: "가입 실패", content: "가입 신청에 실패했습니다." });
      setShowModal(true);
    }
  };

  const handleLeave = async () => {
    try {
      await leaveMutation.mutateAsync(group.id);
      setModalContent({ title: "탈퇴 완료", content: "모임에서 탈퇴했습니다." });
      setShowModal(true);
    } catch {
      setModalContent({ title: "탈퇴 실패", content: "탈퇴에 실패했습니다." });
      setShowModal(true);
    }
  };

  const tabs: { key: TabType; label: string; count?: number }[] = [
    { key: "info", label: "소개" },
    { key: "meetings", label: "정모", count: meetings?.length },
    { key: "members", label: "멤버", count: members?.length },
  ];

  const memberProgress = Math.min((group.memberCount / group.maxMembers) * 100, 100);

  return (
    <div className="pb-24 bg-gray-50 app-content">
      {/* Cover Image with Overlay Header */}
      <div className="relative">
        <div className="aspect-[5/1] bg-gray-200">
          {group.coverImage ? (
            <img
              src={group.coverImage}
              alt={group.name}
              className="w-full h-full object-cover"
            />
          ) : (
            <div className="w-full h-full flex items-center justify-center bg-gradient-to-br from-primary-100 to-primary-200">
              <span className="text-7xl">{group.category?.icon || "🎉"}</span>
            </div>
          )}
        </div>

        {/* Floating Header */}
        <div className="absolute top-0 left-0 right-0 p-4 flex items-center justify-between">
          <button
            onClick={() => navigate(-1)}
            className="w-10 h-10 bg-white/90 backdrop-blur-sm rounded-full flex items-center justify-center shadow-sm"
          >
            <ChevronLeft size={24} className="text-gray-700" />
          </button>
          <div className="flex gap-2">
            <button className="w-10 h-10 bg-white/90 backdrop-blur-sm rounded-full flex items-center justify-center shadow-sm">
              <Share2 size={20} className="text-gray-700" />
            </button>
            {group.myRole && (group.myRole === "OWNER" || group.myRole === "MANAGER") && (
              <Link
                to={`/groups/${group.id}/manage`}
                className="w-10 h-10 bg-white/90 backdrop-blur-sm rounded-full flex items-center justify-center shadow-sm"
              >
                <Settings size={20} className="text-gray-700" />
              </Link>
            )}
          </div>
        </div>

        {/* Category Badge */}
        <div className="absolute bottom-4 left-4">
          <span className="inline-flex items-center px-3 py-1.5 bg-white/95 backdrop-blur-sm rounded-full text-sm font-semibold text-gray-700 shadow-sm">
            {group.category?.icon} {group.category?.name}
          </span>
        </div>
      </div>

      {/* Group Info Card */}
      <div className="bg-white -mt-4 rounded-t-3xl relative z-10 px-4 pt-6 pb-4">
        <h1 className="text-2xl font-bold text-gray-900">{group.name}</h1>

        <div className="mt-3 flex flex-col gap-2">
          <div className="flex items-center gap-2 text-gray-500">
            <MapPin size={16} className="text-gray-400" />
            <span className="text-sm">{group.address || "위치 미정"}</span>
          </div>

          <div className="flex items-center gap-3">
            <div className="flex items-center gap-2">
              <Users size={16} className="text-primary-500" />
              <span className="text-sm font-medium text-gray-700">
                {group.memberCount}
                <span className="text-gray-400">/{group.maxMembers}명</span>
              </span>
            </div>
            <div className="flex-1 h-2 bg-gray-100 rounded-full overflow-hidden">
              <div
                className="h-full bg-primary-400 rounded-full transition-all"
                style={{ width: `${memberProgress}%` }}
              />
            </div>
          </div>
        </div>

        {/* Owner Info */}
        <div className="mt-4 flex items-center gap-3 p-3 bg-gray-50 rounded-xl">
          <Avatar
            src={group.owner.profileImage}
            alt={group.owner.nickname}
            size="md"
          />
          <div>
            <p className="text-xs text-gray-500">모임장</p>
            <p className="font-semibold text-gray-900">{group.owner.nickname}</p>
          </div>
        </div>
      </div>

      {/* Tabs */}
      <div className="sticky top-[104px] z-30 bg-white border-b border-gray-100 shadow-sm">
        <div className="flex">
          {tabs.map((tab) => (
            <button
              key={tab.key}
              onClick={() => setActiveTab(tab.key)}
              className={`flex-1 py-3.5 text-sm font-semibold transition-colors relative ${
                activeTab === tab.key
                  ? "text-primary-600"
                  : "text-gray-400"
              }`}
            >
              {tab.label}
              {tab.count !== undefined && tab.count > 0 && (
                <span className={`ml-1 ${activeTab === tab.key ? "text-primary-400" : "text-gray-300"}`}>
                  {tab.count}
                </span>
              )}
              {activeTab === tab.key && (
                <div className="absolute bottom-0 left-1/4 right-1/4 h-0.5 bg-primary-500 rounded-full" />
              )}
            </button>
          ))}
        </div>
      </div>

      {/* Tab Content*/}
      <div className="p-4">
        {activeTab === "info" && (
          <div className="bg-white rounded-2xl p-4">
            <h3 className="font-bold text-gray-900 mb-3">모임 소개</h3>
            <p className="text-gray-600 whitespace-pre-wrap leading-relaxed">
              {group.description || "소개가 없습니다."}
            </p>
          </div>
        )}

        {activeTab === "meetings" && (
          <div>
            {group.myRole && (group.myRole === "OWNER" || group.myRole === "MANAGER") && (
              <Link
                to={`/meetings/create/${group.id}`}
                className="flex items-center justify-center gap-2 w-full mb-4 py-3.5 bg-primary-500 text-white rounded-xl font-semibold hover:bg-primary-600 transition-colors"
              >
                <span>+</span>
                <span>정모 만들기</span>
              </Link>
            )}

            {meetings && meetings.length > 0 ? (
              <div className="space-y-3">
                {meetings.map((meeting) => (
                  <MeetingCard key={meeting.id} meeting={meeting} />
                ))}
              </div>
            ) : (
              <div className="py-8">
                <EmptyState
                  icon="📅"
                  title="예정된 정모가 없습니다"
                  description="첫 정모를 만들어보세요!"
                />
              </div>
            )}
          </div>
        )}

        {activeTab === "members" && (
          <div>
            {members && members.length > 0 ? (
              <div className="space-y-2">
                {members.map((member) => (
                  <div
                    key={member.id}
                    className="flex items-center justify-between p-3.5 bg-white rounded-xl"
                  >
                    <div className="flex items-center gap-3">
                      <Avatar
                        src={member.member.profileImage}
                        alt={member.member.nickname}
                        size="md"
                      />
                      <div>
                        <p className="font-semibold text-gray-900">{member.member.nickname}</p>
                        <p className="text-xs text-gray-500">
                          {member.role === "OWNER"
                            ? "모임장"
                            : member.role === "MANAGER"
                              ? "운영진"
                              : "멤버"}
                        </p>
                      </div>
                    </div>
                    {member.role === "OWNER" && (
                      <span className="px-2.5 py-1 bg-primary-50 text-primary-600 text-xs font-semibold rounded-full">
                        모임장
                      </span>
                    )}
                    {member.role === "MANAGER" && (
                      <span className="px-2.5 py-1 bg-blue-50 text-blue-600 text-xs font-semibold rounded-full">
                        운영진
                      </span>
                    )}
                  </div>
                ))}
              </div>
            ) : (
              <div className="py-8">
                <EmptyState icon="👥" title="멤버가 없습니다" />
              </div>
            )}
          </div>
        )}
      </div>

      {/* Bottom Action Button */}
      <div className="fixed bottom-16 left-0 right-0 p-4 bg-white/95 backdrop-blur-sm border-t border-gray-100">
        <div className="max-w-[800px] mx-auto">
          {group.myRole ? (
            <div className="flex gap-3">
              {group.myRole !== "OWNER" && (
                <button
                  onClick={handleLeave}
                  className="flex-1 py-3.5 text-center border-2 border-gray-200 text-gray-600 rounded-xl font-semibold hover:bg-gray-50 transition-colors"
                >
                  탈퇴하기
                </button>
              )}
            <button
              onClick={handleJoin}
              disabled={joinMutation.isPending}
              className="w-full py-3.5 bg-primary-500 text-white rounded-xl font-semibold hover:bg-primary-600 transition-colors disabled:opacity-50 shadow-lg shadow-primary-500/25"
            >
              모임수정
            </button>
          </div>
          ) : (
            <button
              onClick={handleJoin}
              disabled={joinMutation.isPending}
              className="w-full py-3.5 bg-primary-500 text-white rounded-xl font-semibold hover:bg-primary-600 transition-colors disabled:opacity-50 shadow-lg shadow-primary-500/25"
            >
              {joinMutation.isPending ? "가입 중..." : "가입하기"}
            </button>
          )}
        </div>
      </div>

      {showModal && (
        <ResultModal
          title={modalContent.title}
          content={modalContent.content}
          callbackFn={() => setShowModal(false)}
        />
      )}
    </div>
  );
}

export default GroupDetailPage;
