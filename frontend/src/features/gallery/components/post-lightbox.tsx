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
  useTogglePostLike,
  usePostComments,
  useAddPostComment,
  useDeletePostComment,
} from "../hooks/use-gallery";
import type { GalleryPostDTO, GalleryCommentDTO } from "../types";

/**
 * ============================================================================
 * 게시물 라이트박스 컴포넌트
 * ============================================================================
 *
 * [역할]
 * 갤러리 게시물을 전체 화면으로 보여주는 라이트박스입니다.
 * 게시물 내 여러 이미지를 캐러셀로 표시합니다.
 *
 * [기능]
 * - 게시물 내 이미지 캐러셀 (스와이프/화살표)
 * - 게시물 간 네비게이션
 * - 캐러셀 인디케이터 (페이지 점)
 * - 좋아요/댓글 (게시물 단위)
 * - 게시물 삭제/다운로드
 */
interface PostLightboxProps {
  /** 게시물 목록 */
  posts: GalleryPostDTO[];
  /** 현재 게시물 인덱스 */
  currentIndex: number;
  /** 닫기 콜백 */
  onClose: () => void;
  /** 삭제 콜백 */
  onDelete?: (postId: number) => void;
  /** 인덱스 변경 콜백 */
  onIndexChange?: (index: number) => void;
}

export function PostLightbox({
  posts,
  currentIndex,
  onClose,
  onDelete,
  onIndexChange,
}: PostLightboxProps) {
  const [postIndex, setPostIndex] = useState(currentIndex);
  const [imageIndex, setImageIndex] = useState(0); // 현재 게시물 내 이미지 인덱스
  const [isDeleting, setIsDeleting] = useState(false);
  const [showComments, setShowComments] = useState(false);
  const [commentInput, setCommentInput] = useState("");
  const commentInputRef = useRef<HTMLInputElement>(null);

  const currentPost = posts[postIndex];
  const currentImage = currentPost?.images?.[imageIndex];
  const totalImages = currentPost?.images?.length || 0;

  // Hooks
  const toggleLikeMutation = useTogglePostLike();
  const addCommentMutation = useAddPostComment();
  const deleteCommentMutation = useDeletePostComment();

  // 댓글 목록 조회 (댓글 패널 열릴 때만)
  const {
    data: comments,
    isLoading: isLoadingComments,
  } = usePostComments(showComments ? currentPost?.id : 0);

  // 게시물 변경 시 이미지 인덱스 초기화
  useEffect(() => {
    setImageIndex(0);
  }, [postIndex]);

  // 이전 게시물로 이동
  const goToPrevPost = useCallback(() => {
    const newIndex = postIndex > 0 ? postIndex - 1 : posts.length - 1;
    setPostIndex(newIndex);
    onIndexChange?.(newIndex);
    setShowComments(false);
  }, [postIndex, posts.length, onIndexChange]);

  // 다음 게시물로 이동
  const goToNextPost = useCallback(() => {
    const newIndex = postIndex < posts.length - 1 ? postIndex + 1 : 0;
    setPostIndex(newIndex);
    onIndexChange?.(newIndex);
    setShowComments(false);
  }, [postIndex, posts.length, onIndexChange]);

  // 게시물 내 이전 이미지로 이동
  const goToPrevImage = useCallback(() => {
    if (imageIndex > 0) {
      setImageIndex(imageIndex - 1);
    }
  }, [imageIndex]);

  // 게시물 내 다음 이미지로 이동
  const goToNextImage = useCallback(() => {
    if (imageIndex < totalImages - 1) {
      setImageIndex(imageIndex + 1);
    }
  }, [imageIndex, totalImages]);

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
          // 이미지 캐러셀 먼저, 없으면 게시물 이동
          if (totalImages > 1 && imageIndex > 0) {
            goToPrevImage();
          } else {
            goToPrevPost();
          }
          break;
        case "ArrowRight":
          // 이미지 캐러셀 먼저, 없으면 게시물 이동
          if (totalImages > 1 && imageIndex < totalImages - 1) {
            goToNextImage();
          } else {
            goToNextPost();
          }
          break;
        case "ArrowUp":
          // 이전 게시물
          goToPrevPost();
          break;
        case "ArrowDown":
          // 다음 게시물
          goToNextPost();
          break;
      }
    };

    window.addEventListener("keydown", handleKeyDown);
    return () => window.removeEventListener("keydown", handleKeyDown);
  }, [onClose, goToPrevPost, goToNextPost, goToPrevImage, goToNextImage, showComments, totalImages, imageIndex]);

  // 스크롤 방지
  useEffect(() => {
    document.body.style.overflow = "hidden";
    return () => {
      document.body.style.overflow = "";
    };
  }, []);

  // 삭제 처리
  const handleDelete = async () => {
    if (!currentPost.canDelete || isDeleting) return;

    const confirmed = window.confirm("이 게시물을 삭제하시겠습니까? 포함된 모든 이미지가 삭제됩니다.");
    if (!confirmed) return;

    setIsDeleting(true);
    try {
      await onDelete?.(currentPost.id);
      if (posts.length <= 1) {
        onClose();
      } else if (postIndex >= posts.length - 1) {
        setPostIndex(postIndex - 1);
      }
    } finally {
      setIsDeleting(false);
    }
  };

  // 다운로드 처리 (현재 이미지)
  const handleDownload = async () => {
    if (!currentImage) return;

    try {
      const imageUrl = getImageUrl(currentImage.imageUrl);
      if (!imageUrl) return;

      const response = await fetch(imageUrl);
      const blob = await response.blob();
      const blobUrl = URL.createObjectURL(blob);
      const link = document.createElement("a");
      link.href = blobUrl;
      link.download = currentImage.originalFileName || `image-${currentImage.id}.jpg`;
      link.click();
      URL.revokeObjectURL(blobUrl);
    } catch (error) {
      console.error("Download failed:", error);
      if (currentImage) {
        window.open(getImageUrl(currentImage.imageUrl), "_blank");
      }
    }
  };

  // 좋아요 토글
  const handleLikeToggle = () => {
    if (toggleLikeMutation.isPending) return;
    toggleLikeMutation.mutate(currentPost.id);
  };

  // 댓글 작성
  const handleCommentSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!commentInput.trim() || addCommentMutation.isPending) return;

    addCommentMutation.mutate(
      { postId: currentPost.id, content: commentInput.trim() },
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
      postId: currentPost.id,
    });
  };

  if (!currentPost) return null;

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
            src={currentPost.uploader?.profileImage}
            alt={currentPost.uploader?.nickname || ""}
            size="sm"
          />
          <div>
            <p className="text-sm font-medium">
              {currentPost.uploader?.nickname || "알 수 없음"}
            </p>
            <p className="text-xs text-gray-400">
              {formatDateTime(currentPost.createdAt)}
            </p>
          </div>
        </div>

        {/* 액션 버튼 */}
        <div className="flex items-center gap-2">
          <button
            onClick={handleDownload}
            className="p-2 rounded-full hover:bg-white/10 transition-colors"
            title="현재 이미지 다운로드"
          >
            <Download size={20} />
          </button>
          {currentPost.canDelete && (
            <button
              onClick={handleDelete}
              disabled={isDeleting}
              className="p-2 rounded-full hover:bg-white/10 transition-colors text-red-400 disabled:opacity-50"
              title="게시물 삭제"
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
          {/* 게시물 간 이전 버튼 */}
          {posts.length > 1 && (
            <button
              onClick={goToPrevPost}
              className="absolute left-2 top-1/2 -translate-y-1/2 p-2 rounded-full bg-black/50 text-white hover:bg-black/70 transition-colors z-10"
              title="이전 게시물"
            >
              <ChevronLeft size={24} />
            </button>
          )}

          {/* 이미지 캐러셀 */}
          <div className="relative max-w-full max-h-[calc(100vh-200px)]">
            {/* 이미지 */}
            {currentImage && (
              <img
                src={getImageUrl(currentImage.imageUrl) || ""}
                alt={currentPost.caption || "갤러리 이미지"}
                className="max-w-full max-h-[calc(100vh-200px)] object-contain"
              />
            )}

            {/* 캐러셀 내 네비게이션 (이미지가 2개 이상일 때) */}
            {totalImages > 1 && (
              <>
                {/* 이전 이미지 버튼 */}
                {imageIndex > 0 && (
                  <button
                    onClick={goToPrevImage}
                    className="absolute left-4 top-1/2 -translate-y-1/2 p-2 rounded-full bg-black/60 text-white hover:bg-black/80 transition-colors"
                    title="이전 이미지"
                  >
                    <ChevronLeft size={20} />
                  </button>
                )}

                {/* 다음 이미지 버튼 */}
                {imageIndex < totalImages - 1 && (
                  <button
                    onClick={goToNextImage}
                    className="absolute right-4 top-1/2 -translate-y-1/2 p-2 rounded-full bg-black/60 text-white hover:bg-black/80 transition-colors"
                    title="다음 이미지"
                  >
                    <ChevronRight size={20} />
                  </button>
                )}

                {/* 캐러셀 인디케이터 (페이지 점) */}
                <div className="absolute bottom-4 left-1/2 -translate-x-1/2 flex items-center gap-2">
                  {currentPost.images.map((_, idx) => (
                    <button
                      key={idx}
                      onClick={() => setImageIndex(idx)}
                      className={`w-2 h-2 rounded-full transition-colors ${
                        idx === imageIndex
                          ? "bg-white"
                          : "bg-white/40 hover:bg-white/60"
                      }`}
                      title={`이미지 ${idx + 1}`}
                    />
                  ))}
                </div>
              </>
            )}
          </div>

          {/* 게시물 간 다음 버튼 */}
          {posts.length > 1 && (
            <button
              onClick={goToNextPost}
              className="absolute right-2 top-1/2 -translate-y-1/2 p-2 rounded-full bg-black/50 text-white hover:bg-black/70 transition-colors z-10"
              title="다음 게시물"
            >
              <ChevronRight size={24} />
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
                댓글 {currentPost.commentCount > 0 && `(${currentPost.commentCount})`}
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
                currentPost.liked
                  ? "fill-red-500 text-red-500"
                  : "text-white hover:text-red-400"
              }`}
            />
            <span className="text-sm">{currentPost.likeCount || 0}</span>
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
            <span className="text-sm">{currentPost.commentCount || 0}</span>
          </button>
        </div>

        {/* 캡션 */}
        {currentPost.caption && (
          <p className="text-sm text-center mb-2">{currentPost.caption}</p>
        )}

        {/* 페이지 인디케이터 */}
        <p className="text-xs text-gray-400 text-center">
          게시물 {postIndex + 1} / {posts.length}
          {totalImages > 1 && ` • 이미지 ${imageIndex + 1} / ${totalImages}`}
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
