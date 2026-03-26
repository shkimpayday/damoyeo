import { useState, useRef, useEffect, useCallback } from "react";
import { useParams, useNavigate, Link } from "react-router";
import { MapPin, Users, ChevronLeft, Share2, Settings, Calendar, Clock, Crown, Shield } from "lucide-react";
import { ENV } from "@/config";
import { useGroupDetail, useGroupMembers, useJoinGroup, useLeaveGroup } from "@/features/groups";
import { useUpcomingMeetingsByGroup, usePastMeetingsByGroup } from "@/features/meetings";
import { useAuth, MemberProfileModal } from "@/features/auth";
import { Avatar, EmptyState, Spinner, ResultModal } from "@/components/ui";
import { formatDateTime, getDayOfWeek, getRelativeTime } from "@/utils/date";
import type { GroupMemberDTO } from "@/features/groups/types";

type TabType = "info" | "meetings" | "gallery" | "chat" | "members";

function GroupDetailPage() {
  const { groupId } = useParams<{ groupId: string }>();
  const navigate = useNavigate();
  const { isLoggedIn } = useAuth();
  const [activeTab, setActiveTab] = useState<TabType>("info");
  const [showModal, setShowModal] = useState(false);
  const [modalContent, setModalContent] = useState({ title: "", content: "" });
  const [showAllMembers, setShowAllMembers] = useState(false);
  const [selectedMemberId, setSelectedMemberId] = useState<number | null>(null);

  const infoRef = useRef<HTMLDivElement>(null);
  const meetingsRef = useRef<HTMLDivElement>(null);
  const membersRef = useRef<HTMLDivElement>(null);

  const scrollToSection = useCallback((tab: TabType) => {
    // 채팅, 갤러리는 별도 페이지로 이동
    if (tab === "chat") {
      navigate(`/groups/${groupId}/chat`);
      return;
    }
    if (tab === "gallery") {
      navigate(`/groups/${groupId}/gallery`);
      return;
    }

    setActiveTab(tab);
    const refMap = { info: infoRef, meetings: meetingsRef, members: membersRef };
    const targetRef = refMap[tab as keyof typeof refMap];
    if (targetRef && targetRef.current) {
      const headerOffset = 160;
      const elementPosition = targetRef.current.getBoundingClientRect().top;
      const offsetPosition = elementPosition + window.scrollY - headerOffset;
      window.scrollTo({ top: offsetPosition, behavior: "smooth" });
    }
  }, [navigate, groupId]);

  useEffect(() => {
    const handleScroll = () => {
      const headerOffset = 180;
      const scrollPosition = window.scrollY + headerOffset;
      const sections = [
        { ref: infoRef, tab: "info" as TabType },
        { ref: meetingsRef, tab: "meetings" as TabType },
        { ref: membersRef, tab: "members" as TabType },
      ];
      for (let i = sections.length - 1; i >= 0; i--) {
        const section = sections[i];
        if (section.ref.current && scrollPosition >= section.ref.current.offsetTop) {
          setActiveTab(section.tab);
          break;
        }
      }
    };
    window.addEventListener("scroll", handleScroll);
    return () => window.removeEventListener("scroll", handleScroll);
  }, []);

  const { data: group, isLoading } = useGroupDetail(Number(groupId));
  const { data: members } = useGroupMembers(Number(groupId));
  const { data: upcomingMeetings } = useUpcomingMeetingsByGroup(Number(groupId));
  const { data: pastMeetings } = usePastMeetingsByGroup(Number(groupId));
  const joinMutation = useJoinGroup();
  const leaveMutation = useLeaveGroup();

  // 정모 탭 상태 (예정/지난)
  const [meetingTab, setMeetingTab] = useState<"upcoming" | "past">("upcoming");

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
      setModalContent({ title: "가입 신청 완료", content: "가입인사를 작성해주세요." });
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

  // 전체 정모 수 (예정 + 지난)
  const totalMeetingsCount = (upcomingMeetings?.length || 0) + (pastMeetings?.length || 0);

  // 탭 목록 (갤러리, 채팅은 멤버만 접근 가능)
  const tabs: { key: TabType; label: string; count?: number }[] = [
    { key: "info", label: "홈" },
    { key: "meetings", label: "정모", count: totalMeetingsCount },
    { key: "members", label: "멤버", count: members?.length },
    ...(group.myRole ? [{ key: "gallery" as TabType, label: "갤러리" }] : []),
    ...(group.myRole ? [{ key: "chat" as TabType, label: "채팅" }] : []),
  ];

  // Separate staff (OWNER + MANAGER) from regular members
  const staffMembers = members?.filter(
    (m) => m.role === "OWNER" || m.role === "MANAGER"
  ) || [];
  const regularMembers = members?.filter((m) => m.role === "MEMBER") || [];
  const displayedMembers = showAllMembers ? regularMembers : regularMembers.slice(0, 10);

  // Check if member joined recently (within 7 days)
  const isNewMember = (joinedAt: string) => {
    const joinDate = new Date(joinedAt);
    const now = new Date();
    const diffDays = (now.getTime() - joinDate.getTime()) / (1000 * 60 * 60 * 24);
    return diffDays <= 7;
  };

  const memberProgress = Math.min((group.memberCount / group.maxMembers) * 100, 100);

  return (
    <div className="pb-24 bg-gray-50 full-bleed">
      {/* Cover Image with Overlay Header */}
      <div className="relative">
        <div className="h-52 sm:h-64 md:h-72 bg-gray-200 overflow-hidden">
          {group.coverImage ? (
            <img
              src={
                group.coverImage.startsWith("/uploads")
                  ? `${ENV.API_URL}${group.coverImage}`
                  : group.coverImage
              }
              alt={group.name}
              className="w-full h-full object-cover"
            />
          ) : (
            <div className="w-full h-full flex items-center justify-center bg-gradient-to-br from-primary-400 via-primary-500 to-primary-700">
              <span className="text-8xl drop-shadow-lg">{group.category?.icon || "🎉"}</span>
            </div>
          )}
          {/* Gradient overlay */}
          <div className="absolute inset-0 bg-gradient-to-t from-black/50 via-transparent to-black/20" />
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
      </div>

      {/* Group Info Card - overlaps cover */}
      <div className="bg-white -mt-6 rounded-t-3xl relative z-10 px-5 pt-7 pb-5 max-w-300 mx-auto">
        <div className="flex items-start gap-4">
          {/* Group thumbnail */}
          <div className="w-25 h-25 flex-shrink-0 rounded-2xl overflow-hidden bg-gray-100 shadow-sm">
            {group.thumbnailImage || group.coverImage ? (
              <img
                src={
                  (group.thumbnailImage || group.coverImage).startsWith("/uploads")
                    ? `${ENV.API_URL}${group.thumbnailImage || group.coverImage}`
                    : group.thumbnailImage || group.coverImage
                }
                alt={group.name}
                className="w-full h-full object-cover"
              />
            ) : (
              <div className="w-full h-full flex items-center justify-center bg-primary-100">
                <span className="text-3xl">{group.category?.icon || "🎉"}</span>
              </div>
            )}
          </div>
          {/* Title + meta */}
          <div>
            <h1 className="text-3xl font-black mt-5 text-gray-900 leading-snug">{group.name}</h1>
            <div className="mt-1 flex items-center gap-1.5 text-sm text-gray-500">
              <div>
                <span className="inline-flex items-center px-3 py-1 bg-primary-50 text-primary-700 rounded-full text-xs font-semibold">
                  {group.category?.icon} {group.category?.name}
                </span>
                {group.isPublic == false && (
                  <span className="inline-flex items-center px-2.5 py-1 bg-amber-50 text-amber-700 rounded-full text-xs font-semibold">
                    🔒 비공개
                  </span>
                )}
            </div>
              {group.address && (
                <>
                  <MapPin size={14} className="text-gray-400" />
                  <span>{group.address}</span>
                  <span className="text-gray-300">·</span>
                </>
              )}
              <Users size={14} className="text-gray-400" />
              <span>멤버 {group.memberCount}</span>
            </div>
            
          </div>
          {/* Category + Public badge */}
        </div>


        {/* Member progress bar */}
        <div className="mt-4 flex items-center gap-3">
          <div className="flex-1 h-2.5 bg-gray-100 rounded-full overflow-hidden">
            <div
              className="h-full rounded-full transition-all duration-500"
              style={{
                width: `${memberProgress}%`,
                background:
                  memberProgress > 80
                    ? "linear-gradient(90deg, #f59e0b, #ef4444)"
                    : "linear-gradient(90deg, #6366f1, #8b5cf6)",
              }}
            />
          </div>
          <span className="text-xs text-gray-500 whitespace-nowrap font-medium">
            {group.memberCount}/{group.maxMembers}명
          </span>
        </div>
      </div>

      {/* Tabs - Sticky Navigation */}
      <div className="sticky top-[104px] z-30 bg-white border-b border-gray-100 shadow-sm max-w-300 mx-auto">
        <div className="flex">
          {tabs.map((tab) => (
            <button
              key={tab.key}
              onClick={() => scrollToSection(tab.key)}
              className={`flex-1 py-3.5 text-sm font-semibold transition-colors relative ${
                activeTab === tab.key ? "text-primary-600" : "text-gray-400"
              }`}
            >
              {tab.label}
              {tab.count != null && tab.count > 0 && (
                <span className="ml-1 text-xs">{tab.count}</span>
              )}
              {activeTab === tab.key && (
                <div className="absolute bottom-0 left-1/2 -translate-x-1/2 w-12 h-0.5 bg-primary-500 rounded-full" />
              )}
            </button>
          ))}
        </div>
      </div>

      {/* All Sections */}
      <div className="space-y-3 max-w-300 mx-auto">
        {/* ── Info Section ── */}
        <div ref={infoRef} className="scroll-mt-44">
          <div className="bg-white px-5 py-5">
            <h2 className="text-lg font-bold text-gray-900 mb-4">모임 소개</h2>
            <div className="text-[15px] text-gray-700 whitespace-pre-wrap leading-7">
              {group.description || "소개가 없습니다."}
            </div>
          </div>

          {/* Staff (운영진) section - owner included */}
          <div className="bg-white px-5 py-5 mt-3">
            <h2 className="text-lg font-bold text-gray-900 mb-4">운영진</h2>
            <div className="grid grid-cols-4 gap-3">
              {staffMembers.map((staff) => (
                <StaffCard
                  key={staff.id}
                  member={staff}
                  onClickProfile={() => setSelectedMemberId(staff.member.id)}
                />
              ))}
            </div>
          </div>
        </div>

        {/* ── Meetings Section ── */}
        <div ref={meetingsRef} className="scroll-mt-44">
          <div className="bg-white px-5 py-5">
            <div className="flex items-center justify-between mb-4">
              <h2 className="text-lg font-bold text-gray-900">
                정모 일정
                {totalMeetingsCount > 0 && (
                  <span className="ml-2 text-primary-500">{totalMeetingsCount}</span>
                )}
              </h2>
              {group.myRole && (group.myRole === "OWNER" || group.myRole === "MANAGER") && (
                <Link
                  to={`/meetings/create/${group.id}`}
                  className="flex items-center gap-1 px-4 py-2 bg-primary-500 text-white rounded-xl text-sm font-semibold hover:bg-primary-600 transition-colors shadow-sm"
                >
                  + 정모 만들기
                </Link>
              )}
            </div>

            {/* 정모 탭 (예정/지난) */}
            <div className="flex gap-2 mb-4">
              <button
                onClick={() => setMeetingTab("upcoming")}
                className={`px-4 py-2 rounded-full text-sm font-semibold transition-colors ${
                  meetingTab === "upcoming"
                    ? "bg-primary-500 text-white"
                    : "bg-gray-100 text-gray-600 hover:bg-gray-200"
                }`}
              >
                예정된 정모
                {upcomingMeetings && upcomingMeetings.length > 0 && (
                  <span className="ml-1.5 px-1.5 py-0.5 text-xs rounded-full bg-white/20">
                    {upcomingMeetings.length}
                  </span>
                )}
              </button>
              <button
                onClick={() => setMeetingTab("past")}
                className={`px-4 py-2 rounded-full text-sm font-semibold transition-colors ${
                  meetingTab === "past"
                    ? "bg-gray-700 text-white"
                    : "bg-gray-100 text-gray-600 hover:bg-gray-200"
                }`}
              >
                지난 정모
                {pastMeetings && pastMeetings.length > 0 && (
                  <span className="ml-1.5 px-1.5 py-0.5 text-xs rounded-full bg-white/20">
                    {pastMeetings.length}
                  </span>
                )}
              </button>
            </div>

            {/* 예정된 정모 목록 */}
            {meetingTab === "upcoming" && (
              <>
                {upcomingMeetings && upcomingMeetings.length > 0 ? (
                  <div className="space-y-3">
                    {upcomingMeetings.map((meeting) => (
                      <Link
                        key={meeting.id}
                        to={`/meetings/${meeting.id}`}
                        className="block p-4 border border-gray-100 rounded-2xl hover:border-primary-200 hover:bg-primary-50/30 transition-all"
                      >
                        <div className="flex gap-4">
                          {/* Date badge */}
                          <div className="flex-shrink-0 w-14 h-14 bg-primary-50 rounded-xl flex flex-col items-center justify-center">
                            <Calendar size={16} className="text-primary-500 mb-0.5" />
                            <span className="text-xs font-bold text-primary-700">
                              {getDayOfWeek(meeting.meetingDate)}
                            </span>
                          </div>
                          <div className="flex-1 min-w-0">
                            <div className="flex items-center gap-2">
                              <h3 className="font-bold text-gray-900 truncate">{meeting.title}</h3>
                              {meeting.status === "ONGOING" && (
                                <span className="px-2 py-0.5 text-xs font-bold rounded-full bg-green-100 text-green-700">
                                  진행중
                                </span>
                              )}
                            </div>
                            <div className="mt-1.5 flex items-center gap-1 text-sm text-gray-500">
                              <Clock size={13} />
                              <span>{formatDateTime(meeting.meetingDate)}</span>
                            </div>
                            {meeting.address && (
                              <div className="mt-1 flex items-center gap-1 text-sm text-gray-500">
                                <MapPin size={13} />
                                <span className="truncate">{meeting.address}</span>
                              </div>
                            )}
                            {/* Attendee count */}
                            <div className="mt-2.5 flex items-center gap-2">
                              <div className="flex items-center gap-1 text-sm">
                                <Users size={14} className="text-primary-500" />
                                <span className="font-semibold text-primary-600">
                                  {meeting.currentAttendees}
                                </span>
                                <span className="text-gray-400">/{meeting.maxAttendees}</span>
                              </div>
                            </div>
                          </div>
                        </div>
                      </Link>
                    ))}
                  </div>
                ) : (
                  <div className="py-10 text-center">
                    <div className="text-4xl mb-3">📅</div>
                    <p className="text-gray-500 font-medium">예정된 정모가 없습니다</p>
                    <p className="text-gray-400 text-sm mt-1">첫 정모를 만들어보세요!</p>
                  </div>
                )}
              </>
            )}

            {/* 지난 정모 목록 */}
            {meetingTab === "past" && (
              <>
                {pastMeetings && pastMeetings.length > 0 ? (
                  <div className="space-y-3">
                    {pastMeetings.map((meeting) => (
                      <Link
                        key={meeting.id}
                        to={`/meetings/${meeting.id}`}
                        className="block p-4 border border-gray-100 rounded-2xl hover:bg-gray-50 transition-all opacity-75"
                      >
                        <div className="flex gap-4">
                          {/* Date badge */}
                          <div className="flex-shrink-0 w-14 h-14 bg-gray-100 rounded-xl flex flex-col items-center justify-center">
                            <Calendar size={16} className="text-gray-400 mb-0.5" />
                            <span className="text-xs font-bold text-gray-500">
                              {getDayOfWeek(meeting.meetingDate)}
                            </span>
                          </div>
                          <div className="flex-1 min-w-0">
                            <div className="flex items-center gap-2">
                              <h3 className="font-bold text-gray-700 truncate">{meeting.title}</h3>
                              <span className="px-2 py-0.5 text-xs font-bold rounded-full bg-gray-200 text-gray-600">
                                완료
                              </span>
                            </div>
                            <div className="mt-1.5 flex items-center gap-1 text-sm text-gray-400">
                              <Clock size={13} />
                              <span>{formatDateTime(meeting.meetingDate)}</span>
                            </div>
                            {meeting.address && (
                              <div className="mt-1 flex items-center gap-1 text-sm text-gray-400">
                                <MapPin size={13} />
                                <span className="truncate">{meeting.address}</span>
                              </div>
                            )}
                            {/* Attendee count */}
                            <div className="mt-2.5 flex items-center gap-2">
                              <div className="flex items-center gap-1 text-sm">
                                <Users size={14} className="text-gray-400" />
                                <span className="font-semibold text-gray-500">
                                  {meeting.currentAttendees}
                                </span>
                                <span className="text-gray-400">/{meeting.maxAttendees}</span>
                              </div>
                            </div>
                          </div>
                        </div>
                      </Link>
                    ))}
                  </div>
                ) : (
                  <div className="py-10 text-center">
                    <div className="text-4xl mb-3">📋</div>
                    <p className="text-gray-500 font-medium">지난 정모가 없습니다</p>
                  </div>
                )}
              </>
            )}
          </div>
        </div>

        {/* ── Members Section ── */}
        <div ref={membersRef} className="scroll-mt-44 min-h-[60vh]">
          <div className="bg-white px-5 py-5">
            <h2 className="text-lg font-bold text-gray-900 mb-4">
              모임 멤버 {members && <span className="text-primary-500">{members.length}</span>}
            </h2>

            {members && members.length > 0 ? (
              <div className="space-y-1">
                {/* Staff first */}
                {staffMembers.map((member) => (
                  <MemberRow
                    key={member.id}
                    member={member}
                    isNew={isNewMember(member.joinedAt)}
                    onClickProfile={() => setSelectedMemberId(member.member.id)}
                  />
                ))}

                {/* Divider */}
                {staffMembers.length > 0 && regularMembers.length > 0 && (
                  <div className="py-2">
                    <div className="h-px bg-gray-100" />
                  </div>
                )}

                {/* Regular members */}
                {displayedMembers.map((member) => (
                  <MemberRow
                    key={member.id}
                    member={member}
                    isNew={isNewMember(member.joinedAt)}
                    onClickProfile={() => setSelectedMemberId(member.member.id)}
                  />
                ))}

                {/* Show more button */}
                {!showAllMembers && regularMembers.length > 10 && (
                  <button
                    onClick={() => setShowAllMembers(true)}
                    className="w-full mt-3 py-3 text-center text-sm font-semibold text-gray-500 border border-gray-200 rounded-xl hover:bg-gray-50 transition-colors"
                  >
                    모임 멤버 더보기 ({regularMembers.length - 10}명 더)
                  </button>
                )}
              </div>
            ) : (
              <div className="py-10 text-center">
                <div className="text-4xl mb-3">👥</div>
                <p className="text-gray-500 font-medium">멤버가 없습니다</p>
              </div>
            )}
          </div>
        </div>
      </div>

      {/* Bottom Action Button */}
      <div className="max-w-300 mx-auto px-5 pt-6 pb-4">
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
              {(group.myRole === "OWNER" || group.myRole === "MANAGER") && (
                <Link
                  to={`/groups/${group.id}/manage`}
                  className="w-full py-3.5 bg-primary-500 text-center text-white rounded-xl font-semibold hover:bg-primary-600 transition-colors shadow-lg shadow-primary-500/25"
                >
                  모임관리
                </Link>
              )}
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

      {showModal && (
        <ResultModal
          title={modalContent.title}
          content={modalContent.content}
          callbackFn={() => setShowModal(false)}
        />
      )}

      {/* Member Profile Modal */}
      {selectedMemberId && (
        <MemberProfileModal
          memberId={selectedMemberId}
          onClose={() => setSelectedMemberId(null)}
        />
      )}
    </div>
  );
}

/** Staff card - grid layout like somoim */
function StaffCard({
  member,
  onClickProfile,
}: {
  member: GroupMemberDTO;
  onClickProfile: () => void;
}) {
  return (
    <button
      type="button"
      onClick={onClickProfile}
      className="flex flex-col items-center text-center hover:opacity-80 transition-opacity"
    >
      <div className="relative">
        <Avatar
          src={member.member.profileImage}
          alt={member.member.nickname}
          size="lg"
        />
        <div
          className={`absolute -bottom-0.5 -right-0.5 w-5 h-5 rounded-full flex items-center justify-center shadow-sm ${
            member.role === "OWNER"
              ? "bg-amber-400"
              : "bg-blue-500"
          }`}
        >
          {member.role === "OWNER" ? (
            <Crown size={11} className="text-white" />
          ) : (
            <Shield size={11} className="text-white" />
          )}
        </div>
      </div>
      <p className="mt-2 text-xs font-semibold text-gray-900 truncate w-full">
        {member.member.nickname}
      </p>
      <p className="text-[10px] text-gray-400">
        {member.role === "OWNER" ? "모임장" : "운영진"}
      </p>
    </button>
  );
}

/** Member row - list layout like somoim */
function MemberRow({
  member,
  isNew,
  onClickProfile,
}: {
  member: GroupMemberDTO;
  isNew: boolean;
  onClickProfile: () => void;
}) {
  const roleLabel =
    member.role === "OWNER"
      ? "모임장"
      : member.role === "MANAGER"
        ? "운영진"
        : null;

  return (
    <button
      type="button"
      onClick={onClickProfile}
      className="w-full flex items-center gap-3 py-3 px-1 hover:bg-gray-50 rounded-lg transition-colors text-left"
    >
      <Avatar
        src={member.member.profileImage}
        alt={member.member.nickname}
        size="md"
      />
      <div className="flex-1 min-w-0">
        <div className="flex items-center gap-2">
          <span className="font-semibold text-gray-900 text-sm">
            {member.member.nickname}
          </span>
          {roleLabel && (
            <span
              className={`px-2 py-0.5 text-[10px] font-bold rounded-full ${
                member.role === "OWNER"
                  ? "bg-amber-100 text-amber-700"
                  : "bg-blue-100 text-blue-700"
              }`}
            >
              {roleLabel}
            </span>
          )}
          {isNew && (
            <span className="px-1.5 py-0.5 text-[10px] font-bold rounded bg-green-100 text-green-600">
              NEW
            </span>
          )}
        </div>
        <p className="text-xs text-gray-400 mt-0.5">
          {getRelativeTime(member.joinedAt)} 가입
        </p>
      </div>
    </button>
  );
}

export default GroupDetailPage;
