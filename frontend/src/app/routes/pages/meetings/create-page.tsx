import { useState } from "react";
import { useParams, useNavigate } from "react-router";
import { useGroupDetail } from "@/features/groups";
import { useCreateMeeting } from "@/features/meetings";
import { ResultModal, Spinner } from "@/components/ui";
import { toInputDateTimeFormat } from "@/utils/date";

function MeetingCreatePage() {
  const { groupId } = useParams<{ groupId: string }>();
  const navigate = useNavigate();
  const { data: group, isLoading: groupLoading } = useGroupDetail(
    Number(groupId)
  );
  const createMutation = useCreateMeeting();

  const [title, setTitle] = useState("");
  const [description, setDescription] = useState("");
  const [address, setAddress] = useState("");
  const [meetingDate, setMeetingDate] = useState(
    toInputDateTimeFormat(new Date(Date.now() + 7 * 24 * 60 * 60 * 1000))
  );
  const [maxAttendees, setMaxAttendees] = useState(10);
  const [fee, setFee] = useState(0);
  const [showModal, setShowModal] = useState(false);
  const [modalContent, setModalContent] = useState({ title: "", content: "" });

  if (groupLoading) {
    return (
      <div className="flex items-center justify-center h-64">
        <Spinner size="lg" />
      </div>
    );
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    try {
      await createMutation.mutateAsync({
        groupId: Number(groupId),
        request: {
          title,
          description,
          address,
          meetingDate: new Date(meetingDate).toISOString(),
          maxAttendees,
          fee,
        },
      });
      setModalContent({ title: "정모 생성 완료", content: "정모가 생성되었습니다!" });
      setShowModal(true);
      setTimeout(() => navigate(`/groups/${groupId}`), 1500);
    } catch {
      setModalContent({ title: "생성 실패", content: "정모 생성에 실패했습니다." });
      setShowModal(true);
    }
  };

  return (
    <div className="p-4">
      <h1 className="text-xl font-bold text-gray-900 mb-2">새 정모 만들기</h1>
      {group && (
        <p className="text-sm text-gray-500 mb-6">{group.name}</p>
      )}

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
            min={2}
            max={100}
            className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-primary-500 outline-none"
          />
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

        {/* Submit */}
        <button
          type="submit"
          disabled={createMutation.isPending}
          className="w-full py-3 bg-primary-500 text-white rounded-lg font-medium hover:bg-primary-600 disabled:bg-gray-300 transition-colors"
        >
          {createMutation.isPending ? "생성 중..." : "정모 만들기"}
        </button>
      </form>

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

export default MeetingCreatePage;
