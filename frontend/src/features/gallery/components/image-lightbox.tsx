import { useState, useEffect, useCallback, useRef } from "react";
import {
  X,
  ChevronLeft,
  ChevronRight,
  Trash2,
  Download,
  Heart,
  MessageCircle,
  Send,
} from "lucide-react";
import { Avatar, Spinner } from "@/components/ui";
import { formatDateTime, getRelativeTime } from "@/utils/date";
import { getImageUrl } from "@/utils";
import {
  useToggleImageLike,
  useImageComments,
  useAddImageComment,
  useDeleteImageComment,
} from "../hooks/use-gallery";
import type { GalleryImageDTO, GalleryCommentDTO } from "../types";

/**
 * ============================================================================
 * 이미지 라이트박스 컴포넌트
 * ============================================================================
 *
 * [역할]
 * 갤러리 이미지를 전체 화면으로 보여주는 라이트박스입니다.
 *
 * [기능]
 * - 이미지 확대 보기
 * - 좌우 네비게이션 (화살표 키 지원)
 * - 업로더 정보 표시
 * - 이미지 삭제 (권한 있는 경우)
 * - 이미지 다운로드
 * - 좋아요 (하트 토글)
 * - 댓글 보기/작성/삭제
 */
interface ImageLightboxProps {
  /** 이미지 목록 */
  images: GalleryImageDTO[];
  /** 현재 이미지 인덱스 */
  currentIndex: number;
  /** 닫기 콜백 */
  onClose: () => void;
  /** 삭제 콜백 */
  onDelete?: (imageId: number) => void;
  /** 인덱스 변경 콜백 */
  onIndexChange?: (index: number) => void;
  /** 좋아요 상태 변경 콜백 (낙관적 업데이트 반영) */
  onLikeChange?: (imageId: number, liked: boolean, likeCount: number) => void;
}

export function ImageLightbox({
  images,
  currentIndex,
  onClose,
  onDelete,
  onIndexChange,
  onLikeChange,
}: ImageLightboxProps) {
  const [index, setIndex] = useState(currentIndex);
  const [isDeleting, setIsDeleting] = useState(false);
  const [showComments, setShowComments] = useState(false);
  const [commentInput, setCommentInput] = useState("");
  const commentInputRef = useRef<HTMLInputElement>(null);

  const currentImage = images[index];

  // Hooks
  const toggleLikeMutation = useToggleImageLike();
  const addCommentMutation = useAddImageComment();
  const deleteCommentMutation = useDeleteImageComment();

  // 댓글 목록 조회 (댓글 패널 열릴 때만)
  const {
    data: comments,
    isLoading: isLoadingComments,
  } = useImageComments(showComments ? currentImage?.id : 0);

  // 이전 이미지로 이동
  const goToPrev = useCallback(() => {
    const newIndex = index > 0 ? index - 1 : images.length - 1;
    setIndex(newIndex);
    onIndexChange?.(newIndex);
    setShowComments(false); // 이미지 변경 시 댓글 패널 닫기
  }, [index, images.length, onIndexChange]);

  // 다음 이미지로 이동
  const goToNext = useCallback(() => {
    const newIndex = index < images.length - 1 ? index + 1 : 0;
    setIndex(newIndex);
    onIndexChange?.(newIndex);
    setShowComments(false);
  }, [index, images.length, onIndexChange]);

  // 키보드 네비게이션
  useEffect(() => {
    const handleKeyDown = (e: KeyboardEvent) => {
      // 댓글 입력 중이면 화살표 키 무시
      if (document.activeElement === commentInputRef.current) {
        if (e.key === "Escape") {
          commentInputRef.current?.blur();
        }
        return;
      }

      switch (e.key) {
        case "Escape":
          if (showComments) {
            setShowComments(false);
          } else {
            onClose();
          }
          break;
        case "ArrowLeft":
          goToPrev();
          break;
        case "ArrowRight":
          goToNext();
          break;
      }
    };

    window.addEventListener("keydown", handleKeyDown);
    return () => window.removeEventListener("keydown", handleKeyDown);
  }, [onClose, goToPrev, goToNext, showComments]);

  // 스크롤 방지
  useEffect(() => {
    document.body.style.overflow = "hidden";
    return () => {
      document.body.style.overflow = "";
    };
  }, []);

  // 삭제 처리
  const handleDelete = async () => {
    if (!currentImage.canDelete || isDeleting) return;

    const confirmed = window.confirm("이 이미지를 삭제하시겠습니까?");
    if (!confirmed) return;

    setIsDeleting(true);
    try {
      await onDelete?.(currentImage.id);
      if (images.length <= 1) {
        onClose();
      } else if (index >= images.length - 1) {
        setIndex(index - 1);
      }
    } finally {
      setIsDeleting(false);
    }
  };

  // 다운로드 처리 (cross-origin 대응: Blob으로 다운로드)
  const handleDownload = async () => {
    try {
      const primaryUrl = currentImage.images?.[0]?.imageUrl || currentImage.thumbnailUrl;
      const imageUrl = getImageUrl(primaryUrl);
      if (!imageUrl) return;

      const response = await fetch(imageUrl);
      const blob = await response.blob();
      const blobUrl = URL.createObjectURL(blob);
      const link = document.createElement("a");
      link.href = blobUrl;
      link.download = currentImage.images?.[0]?.originalFileName || `image-${currentImage.id}.jpg`;
      link.click();
      URL.revokeObjectURL(blobUrl);
    } catch (error) {
      console.error("Download failed:", error);
      const primaryUrl = currentImage.images?.[0]?.imageUrl || currentImage.thumbnailUrl;
      window.open(getImageUrl(primaryUrl), "_blank");
    }
  };

  // 좋아요 토글
  const handleLikeToggle = () => {
    if (toggleLikeMutation.isPending) return;

    toggleLikeMutation.mutate(currentImage.id, {
      onSuccess: (result) => {
        onLikeChange?.(currentImage.id, result.liked, result.likeCount);
      },
    });
  };

  // 댓글 작성
  const handleCommentSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!commentInput.trim() || addCommentMutation.isPending) return;

    addCommentMutation.mutate(
      { postId: currentImage.id, content: commentInput.trim() },
      {
        onSuccess: () => {
          setCommentInput("");
        },
      }
    );
  };

  // 댓글 삭제
  const handleCommentDelete = (comment: GalleryCommentDTO) => {
    if (!comment.canDelete || deleteCommentMutation.isPending) return;

    const confirmed = window.confirm("이 댓글을 삭제하시겠습니까?");
    if (!confirmed) return;

    deleteCommentMutation.mutate({
      commentId: comment.id,
      postId: currentImage.id,
    });
  };

  if (!currentImage) return null;

  return (
    <div
      className="fixed inset-0 z-[70] bg-black/95 flex flex-col"
      onClick={onClose}
    >
      {/* 헤더 */}
      <div
        className="flex items-center justify-between p-4 text-white"
        onClick={(e) => e.stopPropagation()}
      >
        {/* 업로더 정보 */}
        <div className="flex items-center gap-3">
          <Avatar
            src={currentImage.uploader?.profileImage}
            alt={currentImage.uploader?.nickname || ""}
            size="sm"
          />
          <div>
            <p className="text-sm font-medium">
              {currentImage.uploader?.nickname || "알 수 없음"}
            </p>
            <p className="text-xs text-gray-400">
              {formatDateTime(currentImage.createdAt)}
            </p>
          </div>
        </div>

        {/* 액션 버튼 */}
        <div className="flex items-center gap-2">
          <button
            onClick={handleDownload}
            className="p-2 rounded-full hover:bg-white/10 transition-colors"
            title="다운로드"
          >
            <Download size={20} />
          </button>
          {currentImage.canDelete && (
            <button
              onClick={handleDelete}
              disabled={isDeleting}
              className="p-2 rounded-full hover:bg-white/10 transition-colors text-red-400 disabled:opacity-50"
              title="삭제"
            >
              <Trash2 size={20} />
            </button>
          )}
          <button
            onClick={onClose}
            className="p-2 rounded-full hover:bg-white/10 transition-colors"
            title="닫기"
          >
            <X size={24} />
          </button>
        </div>
      </div>

      {/* 메인 영역 */}
      <div className="flex-1 flex overflow-hidden">
        {/* 이미지 영역 */}
        <div
          className={`flex-1 flex items-center justify-center px-4 relative transition-all duration-300 ${
            showComments ? "md:mr-80" : ""
          }`}
          onClick={(e) => e.stopPropagation()}
        >
          {/* 이전 버튼 */}
          {images.length > 1 && (
            <button
              onClick={goToPrev}
              className="absolute left-4 p-3 rounded-full bg-black/50 text-white hover:bg-black/70 transition-colors z-10"
            >
              <ChevronLeft size={28} />
            </button>
          )}

          {/* 이미지 */}
          <img
            src={getImageUrl(currentImage.images?.[0]?.imageUrl || currentImage.thumbnailUrl) || ""}
            alt={currentImage.caption || "갤러리 이미지"}
            className="max-w-full max-h-[calc(100vh-200px)] object-contain"
          />

          {/* 다음 버튼 */}
          {images.length > 1 && (
            <button
              onClick={goToNext}
              className="absolute right-4 p-3 rounded-full bg-black/50 text-white hover:bg-black/70 transition-colors z-10"
            >
              <ChevronRight size={28} />
            </button>
          )}
        </div>

        {/* 댓글 패널 (사이드바) */}
        {showComments && (
          <div
            className="w-80 bg-white flex flex-col absolute right-0 top-0 bottom-0 md:relative animate-slide-in-right"
            onClick={(e) => e.stopPropagation()}
          >
            {/* 댓글 헤더 */}
            <div className="flex items-center justify-between p-4 border-b">
              <h3 className="font-semibold text-gray-900">
                댓글 {currentImage.commentCount > 0 && `(${currentImage.commentCount})`}
              </h3>
              <button
                onClick={() => setShowComments(false)}
                className="p-1 rounded-full hover:bg-gray-100"
              >
                <X size={20} className="text-gray-500" />
              </button>
            </div>

            {/* 댓글 목록 */}
            <div className="flex-1 overflow-y-auto p-4 space-y-4">
              {isLoadingComments ? (
                <div className="flex justify-center py-8">
                  <Spinner size="md" />
                </div>
              ) : comments && comments.length > 0 ? (
                comments.map((comment) => (
                  <div key={comment.id} className="flex gap-3">
                    <Avatar
                      src={comment.writer?.profileImage}
                      alt={comment.writer?.nickname || ""}
                      size="sm"
                    />
                    <div className="flex-1 min-w-0">
                      <div className="flex items-center gap-2">
                        <span className="font-medium text-sm text-gray-900">
                          {comment.writer?.nickname || "알 수 없음"}
                        </span>
                        <span className="text-xs text-gray-400">
                          {getRelativeTime(comment.createdAt)}
                        </span>
                      </div>
                      <p className="text-sm text-gray-700 mt-1 break-words">
                        {comment.content}
                      </p>
                      {comment.canDelete && (
                        <button
                          onClick={() => handleCommentDelete(comment)}
                          className="text-xs text-gray-400 hover:text-red-500 mt-1"
                          disabled={deleteCommentMutation.isPending}
                        >
                          삭제
                        </button>
                      )}
                    </div>
                  </div>
                ))
              ) : (
                <p className="text-center text-gray-400 text-sm py-8">
                  아직 댓글이 없습니다.
                </p>
              )}
            </div>

            {/* 댓글 입력 */}
            <form
              onSubmit={handleCommentSubmit}
              className="p-4 border-t flex gap-2"
            >
              <input
                ref={commentInputRef}
                type="text"
                value={commentInput}
                onChange={(e) => setCommentInput(e.target.value)}
                placeholder="댓글을 입력하세요..."
                className="flex-1 px-3 py-2 border rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-primary"
              />
              <button
                type="submit"
                disabled={!commentInput.trim() || addCommentMutation.isPending}
                className="p-2 bg-primary text-white rounded-lg disabled:opacity-50 disabled:cursor-not-allowed hover:bg-primary/90 transition-colors"
              >
                <Send size={18} />
              </button>
            </form>
          </div>
        )}
      </div>

      {/* 하단 정보 + 좋아요/댓글 버튼 */}
      <div
        className="p-4 text-white"
        onClick={(e) => e.stopPropagation()}
      >
        {/* 좋아요/댓글 버튼 */}
        <div className="flex items-center justify-center gap-6 mb-3">
          {/* 좋아요 버튼 */}
          <button
            onClick={handleLikeToggle}
            disabled={toggleLikeMutation.isPending}
            className="flex items-center gap-2 transition-colors"
          >
            <Heart
              size={24}
              className={`transition-colors ${
                currentImage.liked
                  ? "fill-red-500 text-red-500"
                  : "text-white hover:text-red-400"
              }`}
            />
            <span className="text-sm">{currentImage.likeCount || 0}</span>
          </button>

          {/* 댓글 버튼 */}
          <button
            onClick={() => setShowComments(!showComments)}
            className="flex items-center gap-2 transition-colors hover:text-primary"
          >
            <MessageCircle
              size={24}
              className={showComments ? "text-primary" : ""}
            />
            <span className="text-sm">{currentImage.commentCount || 0}</span>
          </button>
        </div>

        {/* 캡션 */}
        {currentImage.caption && (
          <p className="text-sm text-center mb-2">{currentImage.caption}</p>
        )}

        {/* 페이지 인디케이터 */}
        <p className="text-xs text-gray-400 text-center">
          {index + 1} / {images.length}
        </p>
      </div>

      {/* 스타일 */}
      <style>{`
        @keyframes slide-in-right {
          from {
            transform: translateX(100%);
          }
          to {
            transform: translateX(0);
          }
        }
        .animate-slide-in-right {
          animation: slide-in-right 0.2s ease-out;
        }
      `}</style>
    </div>
  );
}
