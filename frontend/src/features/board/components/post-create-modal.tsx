import { useState, useRef } from "react";
import { X, Image as ImageIcon, Loader2, AlertCircle } from "lucide-react";
import { useCreateBoardPost } from "../hooks/use-board";
import {
  BOARD_CATEGORY_LABELS,
  type BoardCategory,
  type GroupRole,
} from "../types";

interface PostCreateModalProps {
  groupId: number;
  /** 현재 사용자의 모임 역할 (공지는 OWNER/MANAGER만) */
  userRole?: GroupRole | null;
  onClose: () => void;
  onSuccess?: () => void;
}

const MAX_IMAGES = 5;
const MAX_FILE_SIZE_MB = 10;

/**
 * 게시글 작성 모달
 *
 * @description
 * 새 게시글을 작성하는 폼 모달입니다.
 * - 카테고리 선택 (가입인사, 모임후기, 자유게시판, 공지사항)
 * - 제목 + 본문 입력
 * - 이미지 첨부 (최대 5개, 미리보기 포함)
 * - 공지사항 카테고리는 운영진(OWNER/MANAGER)만 선택 가능
 */
export function PostCreateModal({
  groupId,
  userRole,
  onClose,
  onSuccess,
}: PostCreateModalProps) {
  const [category, setCategory] = useState<BoardCategory>("FREE");
  const [title, setTitle] = useState("");
  const [content, setContent] = useState("");
  const [imageFiles, setImageFiles] = useState<File[]>([]);
  const [imagePreviews, setImagePreviews] = useState<string[]>([]);
  const [error, setError] = useState("");

  const fileInputRef = useRef<HTMLInputElement>(null);
  const createMutation = useCreateBoardPost();

  const isStaff = userRole === "OWNER" || userRole === "MANAGER";

  // 카테고리 선택 옵션 (운영진만 공지사항 선택 가능)
  const categoryOptions: BoardCategory[] = isStaff
    ? ["GREETING", "REVIEW", "FREE", "NOTICE"]
    : ["GREETING", "REVIEW", "FREE"];

  const handleImageSelect = (e: React.ChangeEvent<HTMLInputElement>) => {
    const files = Array.from(e.target.files || []);
    if (!files.length) return;

    // 최대 개수 체크
    const remaining = MAX_IMAGES - imageFiles.length;
    if (remaining <= 0) {
      setError(`이미지는 최대 ${MAX_IMAGES}개까지 첨부할 수 있습니다.`);
      return;
    }

    const validFiles: File[] = [];
    const previews: string[] = [];

    for (const file of files.slice(0, remaining)) {
      if (file.size > MAX_FILE_SIZE_MB * 1024 * 1024) {
        setError(`이미지 크기는 ${MAX_FILE_SIZE_MB}MB 이하여야 합니다.`);
        continue;
      }
      validFiles.push(file);
      previews.push(URL.createObjectURL(file));
    }

    setImageFiles((prev) => [...prev, ...validFiles]);
    setImagePreviews((prev) => [...prev, ...previews]);
    setError("");

    // 파일 인풋 초기화 (같은 파일 재선택 가능)
    e.target.value = "";
  };

  const removeImage = (index: number) => {
    URL.revokeObjectURL(imagePreviews[index]);
    setImageFiles((prev) => prev.filter((_, i) => i !== index));
    setImagePreviews((prev) => prev.filter((_, i) => i !== index));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError("");

    if (!title.trim()) {
      setError("제목을 입력해주세요.");
      return;
    }
    if (!content.trim()) {
      setError("본문을 입력해주세요.");
      return;
    }

    try {
      await createMutation.mutateAsync({
        groupId,
        request: {
          category,
          title: title.trim(),
          content: content.trim(),
          files: imageFiles.length > 0 ? imageFiles : undefined,
        },
      });
      // URL 해제
      imagePreviews.forEach((url) => URL.revokeObjectURL(url));
      onSuccess?.();
      onClose();
    } catch {
      setError("게시글 작성에 실패했습니다. 다시 시도해주세요.");
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-end sm:items-center justify-center">
      {/* 배경 */}
      <div
        className="absolute inset-0 bg-black/60 backdrop-blur-sm"
        onClick={onClose}
      />

      {/* 모달 본체 */}
      <div className="relative w-full sm:max-w-lg bg-white sm:rounded-2xl rounded-t-2xl max-h-[92vh] flex flex-col overflow-hidden shadow-2xl">
        {/* 헤더 */}
        <div className="flex items-center justify-between px-4 py-3 border-b border-gray-100 shrink-0">
          <h2 className="text-base font-bold text-gray-900">게시글 작성</h2>
          <button
            onClick={onClose}
            className="w-8 h-8 flex items-center justify-center rounded-full hover:bg-gray-100 text-gray-500"
          >
            <X size={20} />
          </button>
        </div>

        {/* 폼 */}
        <form onSubmit={handleSubmit} className="flex flex-col flex-1 overflow-hidden">
          <div className="flex-1 overflow-y-auto px-4 py-4 space-y-4">
            {/* 카테고리 선택 */}
            <div>
              <label className="block text-xs font-semibold text-gray-500 mb-2 uppercase tracking-wide">
                카테고리
              </label>
              <div className="flex flex-wrap gap-2">
                {categoryOptions.map((cat) => (
                  <button
                    key={cat}
                    type="button"
                    onClick={() => setCategory(cat)}
                    className={`px-3.5 py-1.5 rounded-full text-sm font-medium border transition-all ${
                      category === cat
                        ? cat === "NOTICE"
                          ? "bg-red-500 text-white border-red-500"
                          : cat === "GREETING"
                          ? "bg-green-500 text-white border-green-500"
                          : cat === "REVIEW"
                          ? "bg-blue-500 text-white border-blue-500"
                          : "bg-gray-700 text-white border-gray-700"
                        : "bg-white text-gray-600 border-gray-200 hover:border-gray-400"
                    }`}
                  >
                    {BOARD_CATEGORY_LABELS[cat]}
                  </button>
                ))}
              </div>
            </div>

            {/* 제목 */}
            <div>
              <label className="block text-xs font-semibold text-gray-500 mb-2 uppercase tracking-wide">
                제목
              </label>
              <input
                type="text"
                value={title}
                onChange={(e) => setTitle(e.target.value)}
                placeholder="제목을 입력하세요"
                maxLength={100}
                className="w-full bg-gray-50 rounded-xl px-4 py-2.5 text-sm text-gray-900 placeholder-gray-400 border border-transparent focus:border-blue-300 focus:bg-white focus:outline-none transition-all"
              />
              <p className="text-right text-xs text-gray-400 mt-1">
                {title.length}/100
              </p>
            </div>

            {/* 본문 */}
            <div>
              <label className="block text-xs font-semibold text-gray-500 mb-2 uppercase tracking-wide">
                본문
              </label>
              <textarea
                value={content}
                onChange={(e) => setContent(e.target.value)}
                placeholder="내용을 입력하세요"
                rows={5}
                maxLength={2000}
                className="w-full bg-gray-50 rounded-xl px-4 py-2.5 text-sm text-gray-900 placeholder-gray-400 border border-transparent focus:border-blue-300 focus:bg-white focus:outline-none transition-all resize-none"
              />
              <p className="text-right text-xs text-gray-400 mt-1">
                {content.length}/2000
              </p>
            </div>

            {/* 이미지 첨부 */}
            <div>
              <div className="flex items-center justify-between mb-2">
                <label className="text-xs font-semibold text-gray-500 uppercase tracking-wide">
                  이미지 첨부
                </label>
                <span className="text-xs text-gray-400">
                  {imageFiles.length}/{MAX_IMAGES}
                </span>
              </div>

              <div className="flex flex-wrap gap-2">
                {/* 미리보기 */}
                {imagePreviews.map((url, idx) => (
                  <div
                    key={idx}
                    className="relative w-20 h-20 rounded-xl overflow-hidden bg-gray-100"
                  >
                    <img
                      src={url}
                      alt={`미리보기 ${idx + 1}`}
                      className="w-full h-full object-cover"
                    />
                    <button
                      type="button"
                      onClick={() => removeImage(idx)}
                      className="absolute top-0.5 right-0.5 w-5 h-5 bg-black/60 rounded-full flex items-center justify-center text-white hover:bg-black/80"
                    >
                      <X size={11} />
                    </button>
                  </div>
                ))}

                {/* 추가 버튼 */}
                {imageFiles.length < MAX_IMAGES && (
                  <button
                    type="button"
                    onClick={() => fileInputRef.current?.click()}
                    className="w-20 h-20 rounded-xl border-2 border-dashed border-gray-300 flex flex-col items-center justify-center gap-1 text-gray-400 hover:border-blue-400 hover:text-blue-400 transition-colors"
                  >
                    <ImageIcon size={20} />
                    <span className="text-xs">추가</span>
                  </button>
                )}
              </div>

              <input
                ref={fileInputRef}
                type="file"
                accept="image/*"
                multiple
                onChange={handleImageSelect}
                className="hidden"
              />
            </div>

            {/* 에러 메시지 */}
            {error && (
              <div className="flex items-center gap-2 text-sm text-red-600 bg-red-50 px-3 py-2 rounded-xl">
                <AlertCircle size={16} className="shrink-0" />
                {error}
              </div>
            )}
          </div>

          {/* 하단 버튼 */}
          <div className="shrink-0 px-4 py-3 border-t border-gray-100 bg-white">
            <button
              type="submit"
              disabled={createMutation.isPending || !title.trim() || !content.trim()}
              className="w-full py-3 rounded-xl bg-blue-500 text-white font-semibold text-sm hover:bg-blue-600 disabled:opacity-50 disabled:cursor-not-allowed transition-colors flex items-center justify-center gap-2"
            >
              {createMutation.isPending ? (
                <>
                  <Loader2 size={16} className="animate-spin" />
                  등록 중...
                </>
              ) : (
                "게시글 등록"
              )}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
