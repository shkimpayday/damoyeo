import { useState, useEffect, useRef } from "react";
import { useParams, useNavigate } from "react-router";
import { Camera, X, Crown } from "lucide-react";
import { AxiosError } from "axios";
import { ENV } from "@/config";
import {
  useGroupDetail,
  useUpdateGroup,
  DEFAULT_CATEGORIES,
} from "@/features/groups";
import { ResultModal, Spinner, EmptyState, RegionSelect } from "@/components/ui";
import { PremiumLimitModal, usePremiumStatus } from "@/features/payment";

/** 일반 회원 최대 인원 제한 */
const NORMAL_MEMBER_LIMIT = 30;

function GroupEditPage() {
  const { groupId } = useParams<{ groupId: string }>();
  const navigate = useNavigate();
  const { data: group, isLoading } = useGroupDetail(Number(groupId));
  const updateMutation = useUpdateGroup();
  const { data: premiumStatus } = usePremiumStatus();
  const fileInputRef = useRef<HTMLInputElement>(null);

  const [name, setName] = useState("");
  const [description, setDescription] = useState("");
  const [categoryId, setCategoryId] = useState<number | null>(null);
  const [address, setAddress] = useState("");
  const [coords, setCoords] = useState<{ lat: number; lng: number } | null>(null);
  const [maxMembers, setMaxMembers] = useState(20);
  const [isPublic, setIsPublic] = useState(true);
  const [coverImage, setCoverImage] = useState<File | null>(null);
  const [previewUrl, setPreviewUrl] = useState<string | null>(null);
  const [showModal, setShowModal] = useState(false);
  const [modalContent, setModalContent] = useState({ title: "", content: "" });
  const [showLimitModal, setShowLimitModal] = useState(false);

  const isPremium = premiumStatus?.isPremium ?? false;

  useEffect(() => {
    if (group) {
      setName(group.name || "");
      setDescription(group.description || "");
      setCategoryId(group.category?.id || null);
      setAddress(group.address || "");
      setMaxMembers(group.maxMembers || 20);
      setIsPublic(group.isPublic ?? true);
      // 기존 좌표 복원
      if (group.location?.lat != null && group.location?.lng != null) {
        setCoords({ lat: group.location.lat, lng: group.location.lng });
      }
      // 기존 커버 이미지가 있으면 미리보기로 설정
      if (group.coverImage) {
        setPreviewUrl(group.coverImage);
      }
    }
  }, [group]);

  const handleImageChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (file) {
      if (file.size > 20 * 1024 * 1024) {
        setModalContent({ title: "파일 용량 초과", content: "이미지 파일은 20MB 이하만 업로드 가능합니다." });
        setShowModal(true);
        e.target.value = "";
        return;
      }
      setCoverImage(file);
      const url = URL.createObjectURL(file);
      setPreviewUrl(url);
    }
  };

  const handleRemoveImage = () => {
    setCoverImage(null);
    setPreviewUrl(null);
    if (fileInputRef.current) {
      fileInputRef.current.value = "";
    }
  };

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

    // 일반 회원의 인원 제한 체크 (프론트엔드 사전 검증)
    if (!isPremium && maxMembers > NORMAL_MEMBER_LIMIT) {
      setShowLimitModal(true);
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
          lat: coords?.lat,
          lng: coords?.lng,
          ...(coverImage && { coverImage }),
        },
      });
      setModalContent({ title: "수정 완료", content: "모임 정보가 수정되었습니다!" });
      setShowModal(true);
    } catch (error) {
      if (error instanceof AxiosError) {
        if (error.response?.status === 403) {
          setShowLimitModal(true);
          return;
        }
        if (error.response?.status === 413) {
          setModalContent({ title: "파일 용량 초과", content: "이미지 파일이 너무 큽니다. 20MB 이하의 이미지를 사용해주세요." });
          setShowModal(true);
          return;
        }
      }
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
        {/* Cover Image */}
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-2">
            커버 이미지
          </label>
          <div className="relative">
            {previewUrl ? (
              <div className="relative aspect-[4/1] rounded-xl overflow-hidden bg-gray-100">
                <img
                  src={previewUrl.startsWith("/uploads") ? `${ENV.API_URL}${previewUrl}` : previewUrl}
                  alt="커버 미리보기"
                  className="w-full h-full object-cover"
                />
                <button
                  type="button"
                  onClick={handleRemoveImage}
                  className="absolute top-2 right-2 w-8 h-8 bg-black/50 rounded-full flex items-center justify-center text-white hover:bg-black/70 transition-colors"
                >
                  <X size={18} />
                </button>
                <button
                  type="button"
                  onClick={() => fileInputRef.current?.click()}
                  className="absolute bottom-2 right-2 px-3 py-1.5 bg-white/90 rounded-lg text-sm font-medium text-gray-700 hover:bg-white transition-colors"
                >
                  변경
                </button>
              </div>
            ) : (
              <button
                type="button"
                onClick={() => fileInputRef.current?.click()}
                className="w-full aspect-[3/1] border-2 border-dashed border-gray-300 rounded-xl flex flex-col items-center justify-center gap-2 hover:border-primary-400 hover:bg-primary-50 transition-colors"
              >
                <Camera size={32} className="text-gray-400" />
                <span className="text-sm text-gray-500">커버 이미지 추가</span>
              </button>
            )}
            <input
              ref={fileInputRef}
              type="file"
              accept="image/*"
              onChange={handleImageChange}
              className="hidden"
            />
          </div>
          <p className="mt-1 text-xs text-gray-500">
            모임 상단에 표시되는 대표 이미지입니다.
          </p>
        </div>

        {/* Name */}
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">
            모임 이름 *
          </label>
          <input
            type="text"
            value={name ?? ""}
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
            value={description ?? ""}
            onChange={(e) => setDescription(e.target.value)}
            placeholder="모임에 대해 소개해주세요"
            rows={20}
            className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-primary-500 outline-none resize-none"
          />
        </div>

        <div>
          {/* Address */}
          <RegionSelect
            value={address}
            onChange={(region, regionCoords) => {
              setAddress(region);
              setCoords(regionCoords ?? null);
            }}
            label="활동 지역"
            placeholder="지역을 선택하세요"
          />
        </div>

        {/* Max Members */}
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">
            최대 인원
          </label>
          <input
            type="number"
            value={maxMembers ?? 20}
            onChange={(e) => setMaxMembers(Number(e.target.value))}
            min={group.memberCount}
            max={isPremium ? 1000 : NORMAL_MEMBER_LIMIT}
            className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-primary-500 outline-none"
          />
          <p className="mt-1 text-xs text-gray-500">
            현재 멤버 수({group.memberCount}명) 이상으로 설정해야 합니다.
            {!isPremium && (
              <span className="ml-1">
                (일반 회원: 최대 {NORMAL_MEMBER_LIMIT}명,{" "}
                <button
                  type="button"
                  onClick={() => setShowLimitModal(true)}
                  className="text-amber-600 hover:text-amber-700 font-medium inline-flex items-center gap-0.5"
                >
                  <Crown size={12} />
                  프리미엄
                </button>
                은 무제한)
              </span>
            )}
          </p>
        </div>

        {/* Public */}
        <div className="flex items-center gap-3">
          <input
            type="checkbox"
            id="isPublic"
            checked={isPublic ?? true}
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

      {/* 프리미엄 제한 모달 */}
      <PremiumLimitModal
        isOpen={showLimitModal}
        onClose={() => setShowLimitModal(false)}
        limitType="member"
      />
    </div>
  );
}

export default GroupEditPage;
