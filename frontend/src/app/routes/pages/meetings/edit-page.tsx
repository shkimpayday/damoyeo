import { useState, useEffect } from "react";
import { useParams, useNavigate } from "react-router";
import { useMeetingDetail, useUpdateMeeting } from "@/features/meetings";
import { ResultModal, Spinner, EmptyState } from "@/components/ui";
import { toInputDateTimeFormat } from "@/utils/date";

function MeetingEditPage() {
  const { meetingId } = useParams<{ meetingId: string }>();
  const navigate = useNavigate();
  const { data: meeting, isLoading } = useMeetingDetail(Number(meetingId));
  const updateMutation = useUpdateMeeting();

  const [title, setTitle] = useState("");
  const [description, setDescription] = useState("");
  const [address, setAddress] = useState("");
  const [meetingDate, setMeetingDate] = useState("");
  const [maxAttendees, setMaxAttendees] = useState(10);
  const [fee, setFee] = useState(0);
  const [showModal, setShowModal] = useState(false);
  const [modalContent, setModalContent] = useState({ title: "", content: "" });

  useEffect(() => {
    if (meeting) {
      setTitle(meeting.title);
      setDescription(meeting.description || "");
      setAddress(meeting.address || "");
      setMeetingDate(toInputDateTimeFormat(new Date(meeting.meetingDate)));
      setMaxAttendees(meeting.maxAttendees);
      setFee(meeting.fee);
    }
  }, [meeting]);

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

  // 수정 가능한 상태인지 확인
  if (meeting.status !== "SCHEDULED") {
    return (
      <div className="p-4">
        <EmptyState
          icon="🔒"
          title="수정할 수 없는 정모입니다"
          description="예정된 정모만 수정할 수 있습니다."
          action={{ label: "돌아가기", onClick: () => navigate(-1) }}
        />
      </div>
    );
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    try {
      await updateMutation.mutateAsync({
        meetingId: meeting.id,
        request: {
          title,
          description,
          address,
          meetingDate: new Date(meetingDate).toISOString(),
          maxAttendees,
          fee,
        },
      });
      setModalContent({ title: "수정 완료", content: "정모가 수정되었습니다!" });
      setShowModal(true);
    } catch {
      setModalContent({ title: "수정 실패", content: "정모 수정에 실패했습니다." });
      setShowModal(true);
    }
  };

  const handleModalClose = () => {
    setShowModal(false);
    if (modalContent.title === "수정 완료") {
      navigate(`/meetings/${meeting.id}`);
    }
  };

  return (
    <div className="p-4 pb-20">
      <h1 className="text-xl font-bold text-gray-900 mb-2">정모 수정</h1>
      <p className="text-sm text-gray-500 mb-6">{meeting.groupName}</p>

      <form onSubmit={handleSubmit} className="space-y-6">
        {/* Title */}
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">
            정모 제목 *
          </label>
          <input
            type="text"
            value={title}
            onChange={(e) => setTitle(e.target.value)}
            placeholder="정모 제목을 입력하세요"
            className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-primary-500 outline-none"
            required
          />
        </div>

        {/* Date */}
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">
            일시 *
          </label>
          <input
            type="datetime-local"
            value={meetingDate}
            onChange={(e) => setMeetingDate(e.target.value)}
            className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-primary-500 outline-none"
            required
          />
        </div>

        {/* Address */}
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">
            장소
          </label>
          <input
            type="text"
            value={address}
            onChange={(e) => setAddress(e.target.value)}
            placeholder="정모 장소를 입력하세요"
            className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-primary-500 outline-none"
          />
        </div>

        {/* Description */}
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">
            상세 내용
          </label>
          <textarea
            value={description}
            onChange={(e) => setDescription(e.target.value)}
            placeholder="정모에 대해 설명해주세요"
            rows={4}
            className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-primary-500 outline-none resize-none"
          />
        </div>

        {/* Max Attendees */}
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">
            최대 인원
          </label>
          <input
            type="number"
            value={maxAttendees}
            onChange={(e) => setMaxAttendees(Number(e.target.value))}
            min={meeting.currentAttendees}
            max={100}
            className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-primary-500 outline-none"
          />
          <p className="mt-1 text-xs text-gray-500">
            현재 참석자 수({meeting.currentAttendees}명) 이상으로 설정해야 합니다.
          </p>
        </div>

        {/* Fee */}
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">
            참가비 (원)
          </label>
          <input
            type="number"
            value={fee}
            onChange={(e) => setFee(Number(e.target.value))}
            min={0}
            step={1000}
            className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-primary-500 outline-none"
          />
        </div>

        {/* Buttons */}
        <div className="flex gap-3">
          <button
            type="button"
            onClick={() => navigate(-1)}
            className="flex-1 py-3 border border-gray-300 rounded-lg font-medium text-gray-700 hover:bg-gray-50 transition-colors"
          >
            취소
          </button>
          <button
            type="submit"
            disabled={updateMutation.isPending}
            className="flex-1 py-3 bg-primary-500 text-white rounded-lg font-medium hover:bg-primary-600 disabled:bg-gray-300 transition-colors"
          >
            {updateMutation.isPending ? "저장 중..." : "저장"}
          </button>
        </div>
      </form>

      {showModal && (
        <ResultModal
          title={modalContent.title}
          content={modalContent.content}
          callbackFn={handleModalClose}
        />
      )}
    </div>
  );
}

export default MeetingEditPage;
