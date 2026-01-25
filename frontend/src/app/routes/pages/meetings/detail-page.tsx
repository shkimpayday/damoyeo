import { useState } from "react";
import { useParams, useNavigate, Link } from "react-router";
import {
  useMeetingDetail,
  useMeetingAttendees,
  useAttendMeeting,
  useCancelAttend,
} from "@/features/meetings";
import { useAuth } from "@/features/auth";
import { Avatar, EmptyState, Spinner, ResultModal } from "@/components/ui";
import { formatDateTime, getDayOfWeek } from "@/utils/date";

function MeetingDetailPage() {
  const { meetingId } = useParams<{ meetingId: string }>();
  const navigate = useNavigate();
  const { isLoggedIn } = useAuth();
  const [showModal, setShowModal] = useState(false);
  const [modalContent, setModalContent] = useState({ title: "", content: "" });

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
    <div className="pb-20">
      {/* Header */}
      <div className="p-4 bg-white">
        <Link
          to={`/groups/${meeting.groupId}`}
          className="text-sm text-primary-600 mb-2 block"
        >
          ← {meeting.groupName}
        </Link>
        <div className="flex items-start justify-between">
          <h1 className="text-2xl font-bold text-gray-900">{meeting.title}</h1>
          <span
            className={`px-3 py-1 rounded-full text-sm font-medium ${
              statusColors[meeting.status]
            }`}
          >
            {statusLabels[meeting.status]}
          </span>
        </div>
      </div>

      {/* Info */}
      <div className="p-4 bg-white mt-2 space-y-4">
        <div className="flex items-start gap-3">
          <span className="text-xl">📅</span>
          <div>
            <p className="font-medium">
              {formatDateTime(meeting.meetingDate)} (
              {getDayOfWeek(meeting.meetingDate)})
            </p>
            {meeting.endDate && (
              <p className="text-sm text-gray-500">
                ~ {formatDateTime(meeting.endDate)}
              </p>
            )}
          </div>
        </div>

        <div className="flex items-start gap-3">
          <span className="text-xl">📍</span>
          <p className="font-medium">{meeting.address || "위치 미정"}</p>
        </div>

        <div className="flex items-start gap-3">
          <span className="text-xl">👥</span>
          <p className="font-medium">
            {meeting.currentAttendees}/{meeting.maxAttendees}명 참석
          </p>
        </div>

        {meeting.fee > 0 && (
          <div className="flex items-start gap-3">
            <span className="text-xl">💰</span>
            <p className="font-medium">
              {meeting.fee.toLocaleString()}원
            </p>
          </div>
        )}
      </div>

      {/* Description */}
      <div className="p-4 bg-white mt-2">
        <h3 className="font-bold text-gray-900 mb-2">상세 내용</h3>
        <p className="text-gray-600 whitespace-pre-wrap">
          {meeting.description || "설명이 없습니다."}
        </p>
      </div>

      {/* Attendees */}
      <div className="p-4 bg-white mt-2">
        <h3 className="font-bold text-gray-900 mb-3">
          참석자 ({attendees?.length || 0})
        </h3>
        {attendees && attendees.length > 0 ? (
          <div className="flex flex-wrap gap-2">
            {attendees.map((attendee) => (
              <div key={attendee.id} className="flex items-center gap-2">
                <Avatar
                  src={attendee.member.profileImage}
                  alt={attendee.member.nickname}
                  size="sm"
                />
                <span className="text-sm">{attendee.member.nickname}</span>
              </div>
            ))}
          </div>
        ) : (
          <p className="text-gray-500 text-center py-4">아직 참석자가 없습니다.</p>
        )}
      </div>

      {/* Bottom Action Button */}
      {meeting.status === "SCHEDULED" && (
        <div className="fixed bottom-16 left-0 right-0 p-4 bg-white border-t border-gray-200">
          <div className="flex gap-3">
            {/* Edit Button - 항상 표시 (권한은 edit 페이지에서 체크) */}
            <Link
              to={`/meetings/${meeting.id}/edit`}
              className="px-4 py-3 border border-gray-300 rounded-lg font-medium text-gray-700 hover:bg-gray-50 transition-colors"
            >
              수정
            </Link>

            {meeting.myStatus === "ATTENDING" ? (
              <button
                onClick={handleCancelAttend}
                className="flex-1 py-3 border border-red-300 text-red-600 rounded-lg font-medium"
              >
                참석 취소
              </button>
            ) : (
              <button
                onClick={handleAttend}
                disabled={meeting.currentAttendees >= meeting.maxAttendees}
                className="flex-1 py-3 bg-primary-500 text-white rounded-lg font-medium disabled:bg-gray-300"
              >
                {meeting.currentAttendees >= meeting.maxAttendees
                  ? "정원 마감"
                  : "참석하기"}
              </button>
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
    </div>
  );
}

export default MeetingDetailPage;
