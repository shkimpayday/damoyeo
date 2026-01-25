import { useState } from "react";
import { useNavigate } from "react-router";
import { useCreateGroup, DEFAULT_CATEGORIES } from "@/features/groups";
import { ResultModal, RegionSelect } from "@/components/ui";

function GroupCreatePage() {
  const navigate = useNavigate();
  const createMutation = useCreateGroup();

  const [name, setName] = useState("");
  const [description, setDescription] = useState("");
  const [categoryId, setCategoryId] = useState<number | null>(null);
  const [address, setAddress] = useState("");
  const [maxMembers, setMaxMembers] = useState(20);
  const [isPublic, setIsPublic] = useState(true);
  const [showModal, setShowModal] = useState(false);
  const [modalContent, setModalContent] = useState({ title: "", content: "" });

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!categoryId) {
      setModalContent({ title: "입력 오류", content: "카테고리를 선택해주세요." });
      setShowModal(true);
      return;
    }

    try {
      const result = await createMutation.mutateAsync({
        name,
        description,
        categoryId,
        address,
        maxMembers,
        isPublic,
      });
      setModalContent({ title: "모임 생성 완료", content: "모임이 생성되었습니다!" });
      setShowModal(true);
      setTimeout(() => navigate(`/groups/${result.id}`), 1500);
    } catch {
      setModalContent({ title: "생성 실패", content: "모임 생성에 실패했습니다." });
      setShowModal(true);
    }
  };

  return (
    <div className="min-h-screen py-8 px-4 mb-50">
      <div className="max-w-2xl mx-auto">
        <h1 className="text-xl font-bold text-gray-900 mb-6">새 모임 만들기</h1>

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
        <RegionSelect
          value={address}
          onChange={setAddress}
          label="활동 지역"
          placeholder="지역을 선택하세요"
        />

        {/* Max Members */}
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">
            최대 인원
          </label>
          <input
            type="number"
            value={maxMembers}
            onChange={(e) => setMaxMembers(Number(e.target.value))}
            min={2}
            max={100}
            className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-primary-500 outline-none"
          />
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

        {/* Submit */}
        <button
          type="submit"
          disabled={createMutation.isPending}
          className="w-full py-3 bg-primary-500 text-white rounded-lg font-medium hover:bg-primary-600 disabled:bg-gray-300 transition-colors"
        >
          {createMutation.isPending ? "생성 중..." : "모임 만들기"}
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
    </div>
  );
}

export default GroupCreatePage;
