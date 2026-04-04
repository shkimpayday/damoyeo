import { useState, useRef, useEffect } from "react";
import { useProfile, useUpdateProfile, useUploadProfileImage } from "../hooks";
import { useAuthStore } from "../stores";
import { Avatar, Spinner, ResultModal } from "@/components/ui";
import { getImageUrl } from "@/utils";

interface ProfileEditModalProps {
  isOpen: boolean;
  onClose: () => void;
}

export function ProfileEditModal({ isOpen, onClose }: ProfileEditModalProps) {
  const { member, updateProfile: updateStoreProfile } = useAuthStore();
  const fileInputRef = useRef<HTMLInputElement>(null);

  const [nickname, setNickname] = useState(member.nickname || "");
  const [introduction, setIntroduction] = useState("");
  const [previewImage, setPreviewImage] = useState<string | null>(null);
  const [selectedFile, setSelectedFile] = useState<File | null>(null);
  const [showJoinedGroups, setShowJoinedGroups] = useState(true);
  const [showResultModal, setShowResultModal] = useState(false);
  const [resultContent, setResultContent] = useState({ title: "", content: "" });

  // 모달이 열릴 때마다 최신 프로필 데이터를 강제 요청
  // ⚠️ staleTime 내 캐시 데이터에는 만료된 PREMIUM 역할이 남아 있을 수 있으므로
  // refetch()로 항상 서버에서 최신 roleNames를 가져옵니다.
  const { data: profile, refetch: refetchProfile, isFetching: isProfileFetching } = useProfile();
  const updateProfileMutation = useUpdateProfile();
  const uploadImageMutation = useUploadProfileImage();

  // 프리미엄 회원 여부 확인 (서버에서 가져온 최신 roleNames 기준)
  const isPremium = profile?.roleNames?.includes("PREMIUM") ?? false;

  useEffect(() => {
    if (isOpen) {
      // 모달 열릴 때마다 최신 데이터 강제 요청 (캐시 무시)
      refetchProfile();

      setNickname(member.nickname || "");
      setPreviewImage(null);
      setSelectedFile(null);
      // 프로필 데이터에서 showJoinedGroups 초기화
      if (profile) {
        setIntroduction(profile.introduction || "");
        setShowJoinedGroups(profile.showJoinedGroups ?? true);
      }
    }
  }, [isOpen, member.nickname, profile, refetchProfile]);

  if (!isOpen) return null;

  const handleImageClick = () => {
    fileInputRef.current?.click();
  };

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (file) {
      if (file.size > 5 * 1024 * 1024) {
        setResultContent({
          title: "파일 크기 초과",
          content: "이미지는 5MB 이하여야 합니다.",
        });
        setShowResultModal(true);
        return;
      }

      setSelectedFile(file);
      const reader = new FileReader();
      reader.onloadend = () => {
        setPreviewImage(reader.result as string);
      };
      reader.readAsDataURL(file);
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    try {
      // 이미지 업로드가 있으면 먼저 처리
      let newImageUrl = member.profileImage;
      if (selectedFile) {
        const result = await uploadImageMutation.mutateAsync(selectedFile);
        // 상대 경로를 전체 URL로 변환
        newImageUrl = getImageUrl(result.imageUrl);
      }

      // 프로필 정보 업데이트
      await updateProfileMutation.mutateAsync({
        nickname,
        introduction,
        ...(isPremium && { showJoinedGroups }),  // 프리미엄만 설정 전송
      });

      // Zustand 상태 업데이트
      updateStoreProfile(nickname, newImageUrl);

      setResultContent({
        title: "수정 완료",
        content: "프로필이 수정되었습니다.",
      });
      setShowResultModal(true);
    } catch (error) {
      const status = (error as { response?: { status?: number } }).response?.status;
      if (status === 413) {
        setResultContent({
          title: "파일 크기 초과",
          content: "이미지 파일이 너무 큽니다. 10MB 이하의 이미지를 사용해주세요.",
        });
      } else {
        setResultContent({
          title: "수정 실패",
          content: "프로필 수정에 실패했습니다.",
        });
      }
      setShowResultModal(true);
    }
  };

  const handleResultClose = () => {
    setShowResultModal(false);
    if (resultContent.title === "수정 완료") {
      onClose();
    }
  };

  const isLoading = updateProfileMutation.isPending || uploadImageMutation.isPending;

  return (
    <>
      <div className="fixed inset-0 z-50 flex items-center justify-center">
        {/* Backdrop */}
        <div
          className="absolute inset-0 bg-black/50"
          onClick={onClose}
        />

        {/* Modal */}
        <div className="relative bg-white rounded-xl w-full max-w-md mx-4 p-6 max-h-[90vh] overflow-y-auto">
          <h2 className="text-xl font-bold text-gray-900 mb-6">프로필 수정</h2>

          <form onSubmit={handleSubmit}>
            {/* Profile Image */}
            <div className="flex flex-col items-center mb-6">
              <div
                className="relative cursor-pointer group"
                onClick={handleImageClick}
              >
                <Avatar
                  src={previewImage || member.profileImage}
                  alt={nickname}
                  size="xl"
                />
                <div className="absolute inset-0 flex items-center justify-center bg-black/40 rounded-full opacity-0 group-hover:opacity-100 transition-opacity">
                  <span className="text-white text-2xl">📷</span>
                </div>
              </div>
              <input
                ref={fileInputRef}
                type="file"
                accept="image/*"
                onChange={handleFileChange}
                className="hidden"
              />
              <p className="mt-2 text-sm text-gray-500">
                클릭하여 이미지 변경
              </p>
            </div>

            {/* Nickname */}
            <div className="mb-4">
              <label className="block text-sm font-medium text-gray-700 mb-1">
                닉네임
              </label>
              <input
                type="text"
                value={nickname}
                onChange={(e) => setNickname(e.target.value)}
                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-transparent outline-none"
                placeholder="닉네임을 입력하세요"
                maxLength={20}
                required
              />
            </div>

            {/* Introduction */}
            <div className="mb-6">
              <label className="block text-sm font-medium text-gray-700 mb-1">
                자기소개
              </label>
              <textarea
                value={introduction}
                onChange={(e) => setIntroduction(e.target.value)}
                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-transparent outline-none resize-none"
                placeholder="자기소개를 입력하세요"
                rows={3}
                maxLength={200}
              />
              <p className="mt-1 text-xs text-gray-400 text-right">
                {introduction.length}/200
              </p>
            </div>

            {/* Privacy Settings */}
            <div className={`mb-6 p-4 rounded-lg border ${
              isPremium
                ? "bg-amber-50 border-amber-200"
                : "bg-gray-50 border-gray-200"
            }`}>
              <div className="flex items-center gap-2 mb-3">
                {isPremium ? (
                  <span className="text-amber-600 font-semibold text-sm">
                    Premium 회원 설정
                  </span>
                ) : (
                  <span className="text-gray-600 font-semibold text-sm">
                    프라이버시 설정
                  </span>
                )}
              </div>
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm font-medium text-gray-700">
                    활동 중인 모임 공개
                  </p>
                  <p className="text-xs text-gray-500 mt-0.5">
                    다른 회원이 내 프로필에서 모임을 볼 수 있습니다
                  </p>
                </div>
                <button
                  type="button"
                  disabled={isProfileFetching}
                  onClick={() => {
                    if (isPremium) {
                      setShowJoinedGroups(!showJoinedGroups);
                    } else {
                      setResultContent({
                        title: "Premium 전용 기능",
                        content: "활동 모임 공개 설정은 Premium 회원만 변경할 수 있습니다. Premium에 가입하여 더 많은 기능을 이용해보세요!",
                      });
                      setShowResultModal(true);
                    }
                  }}
                  className={`relative inline-flex h-6 w-11 items-center rounded-full transition-colors ${
                    isProfileFetching
                      ? "bg-gray-200 cursor-wait"
                      : showJoinedGroups
                        ? isPremium ? "bg-primary-500" : "bg-primary-300"
                        : "bg-gray-300"
                  } ${(!isPremium || isProfileFetching) && "cursor-not-allowed opacity-70"}`}
                >
                  <span
                    className={`inline-block h-4 w-4 transform rounded-full bg-white transition-transform ${
                      showJoinedGroups ? "translate-x-6" : "translate-x-1"
                    }`}
                  />
                </button>
              </div>
              {!isPremium && !isProfileFetching && (
                <p className="text-xs text-amber-600 mt-2 flex items-center gap-1">
                  <span>🔒</span>
                  Premium 회원만 설정 변경 가능
                </p>
              )}
            </div>

            {/* Buttons */}
            <div className="flex gap-3">
              <button
                type="button"
                onClick={onClose}
                className="flex-1 py-3 border border-gray-300 rounded-lg font-medium text-gray-700 hover:bg-gray-50 transition-colors"
                disabled={isLoading}
              >
                취소
              </button>
              <button
                type="submit"
                className="flex-1 py-3 bg-primary-500 text-white rounded-lg font-medium hover:bg-primary-600 transition-colors disabled:bg-gray-300 flex items-center justify-center"
                disabled={isLoading}
              >
                {isLoading ? <Spinner size="sm" /> : "저장"}
              </button>
            </div>
          </form>
        </div>
      </div>

      {showResultModal && (
        <ResultModal
          title={resultContent.title}
          content={resultContent.content}
          callbackFn={handleResultClose}
        />
      )}
    </>
  );
}
