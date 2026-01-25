import { useState, useRef, useEffect } from "react";
import { useUpdateProfile, useUploadProfileImage } from "../hooks";
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
  const [showResultModal, setShowResultModal] = useState(false);
  const [resultContent, setResultContent] = useState({ title: "", content: "" });

  const updateProfileMutation = useUpdateProfile();
  const uploadImageMutation = useUploadProfileImage();

  useEffect(() => {
    if (isOpen) {
      setNickname(member.nickname || "");
      setPreviewImage(null);
      setSelectedFile(null);
    }
  }, [isOpen, member.nickname]);

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
      });

      // Zustand 상태 업데이트
      updateStoreProfile(nickname, newImageUrl);

      setResultContent({
        title: "수정 완료",
        content: "프로필이 수정되었습니다.",
      });
      setShowResultModal(true);
    } catch {
      setResultContent({
        title: "수정 실패",
        content: "프로필 수정에 실패했습니다.",
      });
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
