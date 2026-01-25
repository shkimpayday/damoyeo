import { useState, useEffect } from "react";
import { useParams, useNavigate } from "react-router";
import {
  useGroupDetail,
  useUpdateGroup,
  DEFAULT_CATEGORIES,
} from "@/features/groups";
import { ResultModal, Spinner, EmptyState } from "@/components/ui";

function GroupEditPage() {
  const { groupId } = useParams<{ groupId: string }>();
  const navigate = useNavigate();
  const { data: group, isLoading } = useGroupDetail(Number(groupId));
  const updateMutation = useUpdateGroup();

  const [name, setName] = useState("");
  const [description, setDescription] = useState("");
  const [categoryId, setCategoryId] = useState<number | null>(null);
  const [address, setAddress] = useState("");
  const [maxMembers, setMaxMembers] = useState(20);
  const [isPublic, setIsPublic] = useState(true);
  const [showModal, setShowModal] = useState(false);
  const [modalContent, setModalContent] = useState({ title: "", content: "" });

  useEffect(() => {
    if (group) {
      setName(group.name);
      setDescription(group.description || "");
      setCategoryId(group.category.id);
      setAddress(group.address || "");
      setMaxMembers(group.maxMembers);
      setIsPublic(group.isPublic);
    }
  }, [group]);

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

  // 권한 체크
  if (group.myRole !== "OWNER" && group.myRole !== "MANAGER") {
    return (
      <div className="p-4">
        <EmptyState
          icon="🔒"
          title="수정 권한이 없습니다"
          description="모임장 또는 운영진만 수정할 수 있습니다."
          action={{ label: "돌아가기", onClick: () => navigate(-1) }}
        />
      </div>
    );
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!categoryId) {
      setModalContent({ title: "입력 오류", content: "카테고리를 선택해주세요." });
      setShowModal(true);
      return;
    }

    try {
      await updateMutation.mutateAsync({
        groupId: group.id,
        request: {
          name,
          description,
          categoryId,
          address,
          maxMembers,
          isPublic,
        },
      });
      setModalContent({ title: "수정 완료", content: "모임 정보가 수정되었습니다!" });
      setShowModal(true);
    } catch {
      setModalContent({ title: "수정 실패", content: "모임 수정에 실패했습니다." });
      setShowModal(true);
    }
  };

  const handleModalClose = () => {
    setShowModal(false);
    if (modalContent.title === "수정 완료") {
      navigate(`/groups/${group.id}`);
    }
  };

  return (
    <div className="p-4 pb-20">
      <h1 className="text-xl font-bold text-gray-900 mb-6">모임 정보 수정</h1>

      <form onSubmit={handleSubmit} className="space-y-6">
        {/* Name */}
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">
            모임 이름 *
          </label>
          <input
            type="text"
            value={name}
            onChange={(e) => setName(e.target.value)}
            placeholder="모임 이름을 입력하세요"
            className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-primary-500 outline-none"
            required
          />
        </div>

        {/* Category */}
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-2">
            카테고리 *
          </label>
          <div className="flex flex-wrap gap-2">
            {DEFAULT_CATEGORIES.map((category) => (
              <button
                key={category.id}
                type="button"
                onClick={() => setCategoryId(category.id)}
                className={`px-3 py-2 rounded-lg text-sm transition-colors ${
                  categoryId === category.id
                    ? "bg-primary-500 text-white"
                    : "bg-gray-100 text-gray-700 hover:bg-gray-200"
                }`}
              >
                {category.icon} {category.name}
              </button>
            ))}
          </div>
        </div>

        {/* Description */}
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">
            모임 소개
          </label>
          <textarea
            value={description}
            onChange={(e) => setDescription(e.target.value)}
            placeholder="모임에 대해 소개해주세요"
            rows={4}
            className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-primary-500 outline-none resize-none"
          />
        </div>

        {/* Address */}
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">
            활동 지역
          </label>
          <input
            type="text"
            value={address}
            onChange={(e) => setAddress(e.target.value)}
            placeholder="예: 서울시 강남구"
            className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-primary-500 outline-none"
          />
        </div>

        {/* Max Members */}
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">
            최대 인원
          </label>
          <input
            type="number"
            value={maxMembers}
            onChange={(e) => setMaxMembers(Number(e.target.value))}
            min={group.memberCount}
            max={100}
            className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-primary-500 outline-none"
          />
          <p className="mt-1 text-xs text-gray-500">
            현재 멤버 수({group.memberCount}명) 이상으로 설정해야 합니다.
          </p>
        </div>

        {/* Public */}
        <div className="flex items-center gap-3">
          <input
            type="checkbox"
            id="isPublic"
            checked={isPublic}
            onChange={(e) => setIsPublic(e.target.checked)}
            className="w-5 h-5 rounded border-gray-300 text-primary-600 focus:ring-primary-500"
          />
          <label htmlFor="isPublic" className="text-sm text-gray-700">
            공개 모임 (검색 결과에 노출)
          </label>
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

export default GroupEditPage;
