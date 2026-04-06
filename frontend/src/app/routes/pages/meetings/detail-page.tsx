import { useState } from "react";
import { useParams, useNavigate, Link } from "react-router";
import { ChevronLeft, Calendar, MapPin, Users, Wallet, MessageCircle } from "lucide-react";
import {
  useMeetingDetail,
  useMeetingAttendees,
  useAttendMeeting,
  useCancelAttend,
} from "@/features/meetings";
import { useAuth, MemberProfileModal } from "@/features/auth";
import { Avatar, EmptyState, Spinner, ResultModal } from "@/components/ui";
import { formatDateTime, getDayOfWeek } from "@/utils/date";

function MeetingDetailPage() {
  const { meetingId } = useParams<{ meetingId: string }>();
  const navigate = useNavigate();
  const { isLoggedIn } = useAuth();
  const [showModal, setShowModal] = useState(false);
  const [modalContent, setModalContent] = useState({ title: "", content: "" });
  const [profileMemberId, setProfileMemberId] = useState<number | null>(null);

  const { data: meeting, isLoading } = useMeetingDetail(Number(meetingId));
  const { data: attendees } = useMeetingAttendees(Number(meetingId));
  const attendMutation = useAttendMeeting();
  const cancelMutation = useCancelAttend();

  if (isLoading) {
    return (
      <div className="flex items-center justify-center h-64">
        <Spinner size="lg" />
      </div>
    );
  }

  if (!meeting) {
    return (
      <div className="p-4">
        <EmptyState
          icon="❌"
          title="정모를 찾을 수 없습니다"
          action={{ label: "돌아가기", onClick: () => navigate(-1) }}
        />
      </div>
    );
  }

  const handleAttend = async () => {
    if (!isLoggedIn) {
      navigate("/member/login");
      return;
    }

    try {
      await attendMutation.mutateAsync({
        meetingId: meeting.id,
        request: { status: "ATTENDING" },
      });
      setModalContent({ title: "참석 등록 완료", content: "정모에 참석 등록되었습니다!" });
      setShowModal(true);
    } catch {
      setModalContent({ title: "등록 실패", content: "참석 등록에 실패했습니다." });
      setShowModal(true);
    }
  };

  const handleCancelAttend = async () => {
    try {
      await cancelMutation.mutateAsync(meeting.id);
      setModalContent({ title: "참석 취소", content: "정모 참석이 취소되었습니다." });
      setShowModal(true);
    } catch {
      setModalContent({ title: "취소 실패", content: "참석 취소에 실패했습니다." });
      setShowModal(true);
    }
  };

  const statusColors = {
    SCHEDULED: "bg-blue-100 text-blue-700",
    ONGOING: "bg-green-100 text-green-700",
    COMPLETED: "bg-gray-100 text-gray-600",
    CANCELLED: "bg-red-100 text-red-600",
  };

  const statusLabels = {
    SCHEDULED: "예정",
    ONGOING: "진행중",
    COMPLETED: "완료",
    CANCELLED: "취소",
  };

  return (
    <div className="pb-24 bg-gray-50 min-h-screen">
      {/* Header */}
      <div className="bg-white border-b border-gray-100">
        <div className="px-4 py-3 flex items-center gap-3">
          <button
            onClick={() => navigate(-1)}
            className="w-9 h-9 flex items-center justify-center rounded-full hover:bg-gray-100 transition-colors"
          >
            <ChevronLeft size={24} className="text-gray-600" />
          </button>
          <Link
            to={`/groups/${meeting.groupId}`}
            className="text-sm text-gray-500 hover:text-primary-600 transition-colors"
          >
            {meeting.groupName}
          </Link>
        </div>

        <div className="px-5 pb-5">
          <div className="flex items-start justify-between gap-3">
            <h1 className="text-2xl font-bold text-gray-900 leading-tight">
              {meeting.title}
            </h1>
            <span
              className={`shrink-0 px-3 py-1 rounded-full text-sm font-semibold ${
                statusColors[meeting.status]
              }`}
            >
              {statusLabels[meeting.status]}
            </span>
          </div>
        </div>
      </div>

      {/* 비멤버 안내 배너 */}
      {isLoggedIn && meeting.isGroupMember === false && (
        <div className="mx-4 mt-4 p-4 bg-amber-50 border border-amber-200 rounded-2xl flex items-center gap-3">
          <span className="text-2xl">👋</span>
          <div className="flex-1">
            <p className="text-sm font-semibold text-amber-800">모임 미가입 상태입니다</p>
            <p className="text-xs text-amber-600 mt-0.5">모임에 가입하면 이 정모에 참석할 수 있어요</p>
          </div>
        </div>
      )}

      {/* Info Cards */}
      <div className="p-4 space-y-3">
        {/* 일시 카드 */}
        <div className="bg-white rounded-2xl p-4 shadow-sm">
          <div className="flex items-center gap-4">
            <div className="w-12 h-12 bg-primary-50 rounded-xl flex items-center justify-center">
              <Calendar size={22} className="text-primary-500" />
            </div>
            <div className="flex-1">
              <p className="text-xs text-gray-400 font-medium mb-0.5">일시</p>
              <p className="font-semibold text-gray-900">
                {formatDateTime(meeting.meetingDate)} ({getDayOfWeek(meeting.meetingDate)})
              </p>
              {meeting.endDate && (
                <p className="text-sm text-gray-500 mt-0.5">
                  ~ {formatDateTime(meeting.endDate)}
                </p>
              )}
            </div>
          </div>
        </div>

        {/* 장소 카드 */}
        <div className="bg-white rounded-2xl p-4 shadow-sm">
          <div className="flex items-center gap-4">
            <div className="w-12 h-12 bg-orange-50 rounded-xl flex items-center justify-center">
              <MapPin size={22} className="text-orange-500" />
            </div>
            <div className="flex-1">
              <p className="text-xs text-gray-400 font-medium mb-0.5">장소</p>
              <p className="font-semibold text-gray-900">
                {meeting.address || "위치 미정"}
              </p>
            </div>
          </div>
        </div>

        {/* 참석 현황 + 참가비 카드 */}
        <div className="bg-white rounded-2xl p-4 shadow-sm">
          <div className="flex items-center gap-4">
            <div className="w-12 h-12 bg-green-50 rounded-xl flex items-center justify-center">
              <Users size={22} className="text-green-500" />
            </div>
            <div className="flex-1">
              <p className="text-xs text-gray-400 font-medium mb-0.5">참석 현황</p>
              <div className="flex items-baseline gap-1">
                <span className="text-xl font-bold text-primary-600">
                  {meeting.currentAttendees}
                </span>
                <span className="text-gray-400">/ {meeting.maxAttendees}명</span>
              </div>
            </div>
            {meeting.fee > 0 && (
              <div className="border-l border-gray-100 pl-4">
                <p className="text-xs text-gray-400 font-medium mb-0.5">참가비</p>
                <div className="flex items-center gap-1">
                  <Wallet size={16} className="text-amber-500" />
                  <span className="font-bold text-gray-900">
                    {meeting.fee.toLocaleString()}원
                  </span>
                </div>
              </div>
            )}
          </div>
        </div>
      </div>

      {/* Description */}
      <div className="px-4 pb-3">
        <div className="bg-white rounded-2xl p-5 shadow-sm">
          <h3 className="text-base font-bold text-gray-900 mb-3">상세 내용</h3>
          <p className="text-gray-600 whitespace-pre-wrap leading-relaxed">
            {meeting.description || "등록된 설명이 없습니다."}
          </p>
        </div>
      </div>

      {/* Attendees */}
      <div className="px-4 pb-4">
        <div className="bg-white rounded-2xl p-5 shadow-sm">
          <h3 className="text-base font-bold text-gray-900 mb-4">
            참석자
            <span className="ml-2 text-primary-500">{attendees?.length || 0}</span>
          </h3>
          {attendees && attendees.length > 0 ? (
            <div className="grid grid-cols-2 gap-3">
              {attendees.map((attendee) => (
                <button
                  key={attendee.id}
                  type="button"
                  onClick={() => setProfileMemberId(attendee.member.id)}
                  className="flex items-center gap-3 p-2 rounded-xl bg-gray-50 hover:bg-gray-100 transition-colors text-left"
                >
                  <Avatar
                    src={attendee.member.profileImage}
                    alt={attendee.member.nickname}
                    size="md"
                  />
                  <span className="text-sm font-medium text-gray-800 truncate">
                    {attendee.member.nickname}
                  </span>
                </button>
              ))}
            </div>
          ) : (
            <div className="py-8 text-center">
              <div className="text-3xl mb-2">👋</div>
              <p className="text-gray-500">아직 참석자가 없습니다</p>
              <p className="text-gray-400 text-sm mt-1">첫 번째 참석자가 되어보세요!</p>
            </div>
          )}
        </div>
      </div>

      {/* Bottom Action Button - 완료/취소된 정모는 버튼 숨김 */}
      {meeting.status !== "COMPLETED" && meeting.status !== "CANCELLED" && (
        <div className="fixed bottom-16 left-0 right-0 p-4 bg-white border-t border-gray-100 shadow-lg">
          <div className="flex gap-3 max-w-lg mx-auto">
            {/* 비멤버: 모임 가입 유도 */}
            {meeting.isGroupMember === false ? (
              <Link
                to={`/groups/${meeting.groupId}`}
                className="flex-1 py-3.5 bg-primary-500 text-white rounded-xl font-semibold text-center hover:bg-primary-600 transition-colors shadow-lg shadow-primary-500/25"
              >
                모임 가입하고 참석하기
              </Link>
            ) : (
              <>
                {/* 수정 버튼은 권한이 있는 경우에만 표시 (생성자 또는 OWNER/MANAGER) */}
                {meeting.canEdit && (
                  <Link
                    to={`/meetings/${meeting.id}/edit`}
                    className="px-5 py-3.5 border border-gray-200 rounded-xl font-semibold text-gray-700 hover:bg-gray-50 transition-colors"
                  >
                    수정
                  </Link>
                )}

                {/* 참석자 전용 채팅 버튼 */}
                {meeting.myStatus === "ATTENDING" && (
                  <Link
                    to={`/meetings/${meeting.id}/chat`}
                    className="px-4 py-3.5 bg-primary-50 rounded-xl flex items-center justify-center hover:bg-primary-100 transition-colors"
                    title="참석자 채팅"
                  >
                    <MessageCircle size={22} className="text-primary-500" />
                  </Link>
                )}

                {meeting.myStatus === "ATTENDING" ? (
                  <button
                    onClick={handleCancelAttend}
                    className="flex-1 py-3.5 border-2 border-red-200 text-red-600 rounded-xl font-semibold hover:bg-red-50 transition-colors"
                  >
                    참석 취소
                  </button>
                ) : (
                  <button
                    onClick={handleAttend}
                    disabled={meeting.currentAttendees >= meeting.maxAttendees}
                    className="flex-1 py-3.5 bg-primary-500 text-white rounded-xl font-semibold disabled:bg-gray-300 hover:bg-primary-600 transition-colors shadow-lg shadow-primary-500/25"
                  >
                    {meeting.currentAttendees >= meeting.maxAttendees
                      ? "정원 마감"
                      : "참석하기"}
                  </button>
                )}
              </>
            )}
          </div>
        </div>
      )}

      {showModal && (
        <ResultModal
          title={modalContent.title}
          content={modalContent.content}
          callbackFn={() => setShowModal(false)}
        />
      )}

      {/* 회원 프로필 모달 */}
      {profileMemberId && (
        <MemberProfileModal
          memberId={profileMemberId}
          onClose={() => setProfileMemberId(null)}
        />
      )}
    </div>
  );
}

export default MeetingDetailPage;
