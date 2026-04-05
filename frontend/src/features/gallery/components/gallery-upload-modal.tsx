import { useState, useRef, useCallback } from "react";
import { X, Upload } from "lucide-react";
import { AxiosError } from "axios";
import { Spinner } from "@/components/ui";
import { useUploadGalleryPost } from "../hooks/use-gallery";

/**
 * ============================================================================
 * 갤러리 업로드 모달 컴포넌트
 * ============================================================================
 *
 * [역할]
 * 갤러리에 이미지를 업로드하는 모달입니다.
 *
 * [기능]
 * - 드래그 앤 드롭 업로드
 * - 다중 파일 선택 (최대 10개)
 * - 미리보기
 * - 캡션 입력 (선택)
 */
interface GalleryUploadModalProps {
  /** 모임 ID */
  groupId: number;
  /** 닫기 콜백 */
  onClose: () => void;
  /** 업로드 완료 콜백 */
  onSuccess?: () => void;
}

const MAX_FILES = 10;
const MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
const ALLOWED_TYPES = ["image/jpeg", "image/png", "image/gif", "image/webp"];

export function GalleryUploadModal({
  groupId,
  onClose,
  onSuccess,
}: GalleryUploadModalProps) {
  const [selectedFiles, setSelectedFiles] = useState<File[]>([]);
  const [previews, setPreviews] = useState<string[]>([]);
  const [caption, setCaption] = useState("");
  const [isDragOver, setIsDragOver] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const fileInputRef = useRef<HTMLInputElement>(null);
  const uploadMutation = useUploadGalleryPost();

  // 파일 유효성 검사
  const validateFiles = useCallback((files: File[]): File[] => {
    const validFiles: File[] = [];

    for (const file of files) {
      // 타입 확인
      if (!ALLOWED_TYPES.includes(file.type)) {
        setError("JPG, PNG, GIF, WebP 파일만 업로드할 수 있습니다.");
        continue;
      }

      // 크기 확인
      if (file.size > MAX_FILE_SIZE) {
        setError("파일 크기는 10MB 이하여야 합니다.");
        continue;
      }

      validFiles.push(file);
    }

    return validFiles;
  }, []);

  // 파일 추가
  const addFiles = useCallback(
    (newFiles: File[]) => {
      setError(null);

      const validFiles = validateFiles(newFiles);
      const totalFiles = [...selectedFiles, ...validFiles];

      if (totalFiles.length > MAX_FILES) {
        setError(`최대 ${MAX_FILES}개의 이미지만 업로드할 수 있습니다.`);
        return;
      }

      // 미리보기 생성
      const newPreviews = validFiles.map((file) => URL.createObjectURL(file));

      setSelectedFiles(totalFiles.slice(0, MAX_FILES));
      setPreviews([...previews, ...newPreviews].slice(0, MAX_FILES));
    },
    [selectedFiles, previews, validateFiles]
  );

  // 파일 제거
  const removeFile = useCallback(
    (index: number) => {
      const newFiles = [...selectedFiles];
      const newPreviews = [...previews];

      // 미리보기 URL 해제
      URL.revokeObjectURL(newPreviews[index]);

      newFiles.splice(index, 1);
      newPreviews.splice(index, 1);

      setSelectedFiles(newFiles);
      setPreviews(newPreviews);
    },
    [selectedFiles, previews]
  );

  // 파일 선택 핸들러
  const handleFileSelect = useCallback(
    (e: React.ChangeEvent<HTMLInputElement>) => {
      if (e.target.files) {
        addFiles(Array.from(e.target.files));
      }
    },
    [addFiles]
  );

  // 드래그 앤 드롭 핸들러
  const handleDragOver = useCallback((e: React.DragEvent) => {
    e.preventDefault();
    setIsDragOver(true);
  }, []);

  const handleDragLeave = useCallback((e: React.DragEvent) => {
    e.preventDefault();
    setIsDragOver(false);
  }, []);

  const handleDrop = useCallback(
    (e: React.DragEvent) => {
      e.preventDefault();
      setIsDragOver(false);

      if (e.dataTransfer.files) {
        addFiles(Array.from(e.dataTransfer.files));
      }
    },
    [addFiles]
  );

  // 업로드 핸들러
  const handleUpload = async () => {
    if (selectedFiles.length === 0) return;

    try {
      await uploadMutation.mutateAsync({
        groupId,
        files: selectedFiles,
        caption: caption.trim() || undefined,
      });

      // 미리보기 URL 정리
      previews.forEach((url) => URL.revokeObjectURL(url));

      onSuccess?.();
      onClose();
    } catch (err) {
      if (err instanceof AxiosError && err.response?.status === 413) {
        setError("파일 크기가 너무 큽니다. 10MB 이하의 이미지를 사용해주세요.");
      } else {
        setError("업로드에 실패했습니다. 다시 시도해주세요.");
      }
    }
  };

  // 컴포넌트 언마운트 시 미리보기 URL 정리
  // (useEffect cleanup에서 처리하면 onClose 전에 실행될 수 있음)

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/50">
      <div className="w-full max-w-lg bg-white rounded-2xl overflow-hidden">
        {/* 헤더 */}
        <div className="flex items-center justify-between p-4 border-b border-gray-100">
          <h2 className="text-lg font-bold text-gray-900">사진 올리기</h2>
          <button
            onClick={onClose}
            className="p-2 rounded-full hover:bg-gray-100 transition-colors"
          >
            <X size={20} className="text-gray-500" />
          </button>
        </div>

        {/* 본문 */}
        <div className="p-4 space-y-4">
          {/* 드래그 앤 드롭 영역 */}
          <div
            onClick={() => fileInputRef.current?.click()}
            onDragOver={handleDragOver}
            onDragLeave={handleDragLeave}
            onDrop={handleDrop}
            className={`
              relative border-2 border-dashed rounded-xl p-8 text-center cursor-pointer
              transition-colors
              ${isDragOver
                ? "border-primary-400 bg-primary-50"
                : "border-gray-200 hover:border-gray-300 hover:bg-gray-50"
              }
            `}
          >
            <input
              ref={fileInputRef}
              type="file"
              accept="image/jpeg,image/png,image/gif,image/webp"
              multiple
              onChange={handleFileSelect}
              className="hidden"
            />
            <Upload size={32} className="mx-auto mb-2 text-gray-400" />
            <p className="text-sm text-gray-600">
              클릭하거나 드래그하여 업로드
            </p>
            <p className="text-xs text-gray-400 mt-1">
              JPG, PNG, GIF, WebP (최대 10MB, {MAX_FILES}개)
            </p>
          </div>

          {/* 에러 메시지 */}
          {error && (
            <p className="text-sm text-red-500 text-center">{error}</p>
          )}

          {/* 미리보기 */}
          {previews.length > 0 && (
            <div className="grid grid-cols-5 gap-2">
              {previews.map((preview, index) => (
                <div key={index} className="relative aspect-square">
                  <img
                    src={preview}
                    alt={`미리보기 ${index + 1}`}
                    className="w-full h-full object-cover rounded-lg"
                  />
                  <button
                    onClick={() => removeFile(index)}
                    className="absolute -top-1 -right-1 p-1 bg-red-500 text-white rounded-full hover:bg-red-600 transition-colors"
                  >
                    <X size={12} />
                  </button>
                </div>
              ))}
            </div>
          )}

          {/* 캡션 입력 */}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              설명 (선택)
            </label>
            <input
              type="text"
              value={caption}
              onChange={(e) => setCaption(e.target.value)}
              placeholder="사진에 대한 설명을 입력하세요"
              className="w-full px-3 py-2 border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-transparent"
              maxLength={100}
            />
          </div>
        </div>

        {/* 푸터 */}
        <div className="flex gap-3 p-4 border-t border-gray-100">
          <button
            onClick={onClose}
            className="flex-1 py-3 border border-gray-200 text-gray-700 rounded-xl font-medium hover:bg-gray-50 transition-colors"
          >
            취소
          </button>
          <button
            onClick={handleUpload}
            disabled={selectedFiles.length === 0 || uploadMutation.isPending}
            className="flex-1 py-3 bg-primary-500 text-white rounded-xl font-medium hover:bg-primary-600 transition-colors disabled:bg-gray-300 disabled:cursor-not-allowed"
          >
            {uploadMutation.isPending ? (
              <Spinner size="sm" />
            ) : (
              `업로드 (${selectedFiles.length})`
            )}
          </button>
        </div>
      </div>
    </div>
  );
}
