import { useState, useRef, useEffect } from "react";
import {
  X,
  Heart,
  MessageCircle,
  Send,
  ChevronLeft,
  ChevronRight,
  Trash2,
  Pin,
} from "lucide-react";
import { Avatar } from "@/components/ui";
import { getRelativeTime, formatDateTime } from "@/utils/date";
import {
  useToggleBoardPostLike,
  useDeleteBoardPost,
  useBoardComments,
  useAddBoardComment,
  useDeleteBoardComment,
} from "../hooks/use-board";
import {
  BOARD_CATEGORY_LABELS,
  BOARD_CATEGORY_COLORS,
  type BoardPostDTO,
} from "../types";

interface PostDetailModalProps {
  post: BoardPostDTO;
  groupId: number;
  onClose: () => void;
}

/**
 * 게시글 상세 모달
 *
 * @description
 * 게시글의 전체 내용을 모달로 표시합니다.
 * - 이미지 캐러셀 (여러 이미지 좌우 스와이프)
 * - 전체 본문 내용
 * - 좋아요 버튼 (낙관적 업데이트)
 * - 댓글 목록 + 댓글 작성
 */
export function PostDetailModal({ post, groupId, onClose }: PostDetailModalProps) {
  const [imageIndex, setImageIndex] = useState(0);
  const [commentText, setCommentText] = useState("");
  const commentInputRef = useRef<HTMLInputElement>(null);
  const contentRef = useRef<HTMLDivElement>(null);

  const toggleLikeMutation = useToggleBoardPostLike();
  const deleteMutation = useDeleteBoardPost();
  const addCommentMutation = useAddBoardComment();
  const deleteCommentMutation = useDeleteBoardComment();

  const { data: comments = [], isLoading: commentsLoading } = useBoardComments(post.id);

  const categoryColor = BOARD_CATEGORY_COLORS[post.category];
  const categoryLabel = BOARD_CATEGORY_LABELS[post.category];

  // 모달 열릴 때 스크롤 잠금
  useEffect(() => {
    document.body.style.overflow = "hidden";
    return () => {
      document.body.style.overflow = "";
    };
  }, []);

  const handleLike = () => {
    toggleLikeMutation.mutate(post.id);
  };

  const handleDelete = async () => {
    if (!confirm("게시글을 삭제하시겠습니까?")) return;
    try {
      await deleteMutation.mutateAsync({ postId: post.id, groupId });
      onClose();
    } catch {
      alert("삭제에 실패했습니다.");
    }
  };

  const handleAddComment = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!commentText.trim()) return;
    try {
      await addCommentMutation.mutateAsync({
        postId: post.id,
        content: commentText.trim(),
      });
      setCommentText("");
    } catch {
      alert("댓글 작성에 실패했습니다.");
    }
  };

  const handleDeleteComment = async (commentId: number) => {
    if (!confirm("댓글을 삭제하시겠습니까?")) return;
    try {
      await deleteCommentMutation.mutateAsync({ commentId, postId: post.id });
    } catch {
      alert("댓글 삭제에 실패했습니다.");
    }
  };

  const prevImage = () =>
    setImageIndex((i) => (i > 0 ? i - 1 : post.images.length - 1));
  const nextImage = () =>
    setImageIndex((i) => (i < post.images.length - 1 ? i + 1 : 0));

  return (
    <div className="fixed inset-0 z-50 flex items-end sm:items-center justify-center">
      {/* 배경 오버레이 */}
      <div
        className="absolute inset-0 bg-black/60 backdrop-blur-sm"
        onClick={onClose}
      />

      {/* 모달 본체 */}
      <div className="relative w-full sm:max-w-lg bg-white sm:rounded-2xl rounded-t-2xl max-h-[92vh] flex flex-col overflow-hidden shadow-2xl">
        {/* 모달 헤더 */}
        <div className="flex items-center justify-between px-4 py-3 border-b border-gray-100 shrink-0">
          <div className="flex items-center gap-2">
            {post.isPinned && (
              <Pin size={14} className="text-orange-500 fill-orange-500" />
            )}
            <span
              className={`text-xs font-semibold px-2.5 py-1 rounded-full border ${categoryColor.bg} ${categoryColor.text} ${categoryColor.border}`}
            >
              {categoryLabel}
            </span>
          </div>
          <div className="flex items-center gap-1">
            {post.canDelete && (
              <button
                onClick={handleDelete}
                className="w-8 h-8 flex items-center justify-center rounded-full hover:bg-red-50 text-red-400"
              >
                <Trash2 size={16} />
              </button>
            )}
            <button
              onClick={onClose}
              className="w-8 h-8 flex items-center justify-center rounded-full hover:bg-gray-100 text-gray-500"
            >
              <X size={20} />
            </button>
          </div>
        </div>

        {/* 스크롤 영역 */}
        <div ref={contentRef} className="overflow-y-auto flex-1">
          {/* 이미지 캐러셀 */}
          {post.images.length > 0 && (
            <div className="relative bg-black">
              <div className="aspect-[4/3] overflow-hidden">
                <img
                  src={post.images[imageIndex].imageUrl}
                  alt={`이미지 ${imageIndex + 1}`}
                  className="w-full h-full object-contain"
                />
              </div>

              {/* 이전/다음 버튼 */}
              {post.images.length > 1 && (
                <>
                  <button
                    onClick={prevImage}
                    className="absolute left-2 top-1/2 -translate-y-1/2 w-8 h-8 bg-black/40 rounded-full flex items-center justify-center text-white hover:bg-black/60"
                  >
                    <ChevronLeft size={18} />
                  </button>
                  <button
                    onClick={nextImage}
                    className="absolute right-2 top-1/2 -translate-y-1/2 w-8 h-8 bg-black/40 rounded-full flex items-center justify-center text-white hover:bg-black/60"
                  >
                    <ChevronRight size={18} />
                  </button>
                  {/* 인디케이터 */}
                  <div className="absolute bottom-2 left-1/2 -translate-x-1/2 flex gap-1">
                    {post.images.map((_, i) => (
                      <button
                        key={i}
                        onClick={() => setImageIndex(i)}
                        className={`w-1.5 h-1.5 rounded-full transition-all ${
                          i === imageIndex ? "bg-white w-4" : "bg-white/50"
                        }`}
                      />
                    ))}
                  </div>
                </>
              )}
            </div>
          )}

          {/* 게시글 본문 */}
          <div className="px-4 pt-4 pb-2">
            {/* 작성자 정보 */}
            <div className="flex items-center gap-2.5 mb-3">
              <Avatar
                src={post.author.profileImage}
                alt={post.author.nickname}
                size="sm"
              />
              <div>
                <p className="text-sm font-semibold text-gray-900">
                  {post.author.nickname}
                </p>
                <p className="text-xs text-gray-400">
                  {formatDateTime(post.createdAt)}
                </p>
              </div>
            </div>

            {/* 제목 */}
            <h2 className="text-base font-bold text-gray-900 mb-2">
              {post.title}
            </h2>

            {/* 본문 */}
            <p className="text-sm text-gray-700 leading-relaxed whitespace-pre-wrap">
              {post.content}
            </p>

            {/* 좋아요 / 댓글 수 */}
            <div className="flex items-center gap-4 mt-4 pt-3 border-t border-gray-100">
              <button
                onClick={handleLike}
                className={`flex items-center gap-1.5 text-sm font-medium transition-colors ${
                  post.liked ? "text-red-500" : "text-gray-400 hover:text-red-400"
                }`}
              >
                <Heart
                  size={18}
                  className={post.liked ? "fill-red-500" : ""}
                />
                좋아요 {post.likeCount}
              </button>
              <button
                onClick={() => commentInputRef.current?.focus()}
                className="flex items-center gap-1.5 text-sm font-medium text-gray-400 hover:text-blue-400"
              >
                <MessageCircle size={18} />
                댓글 {comments.length}
              </button>
            </div>
          </div>

          {/* 댓글 섹션 */}
          <div className="px-4 pb-4">
            <h3 className="text-sm font-bold text-gray-700 mb-3">
              댓글 {comments.length}개
            </h3>

            {commentsLoading ? (
              <div className="text-center py-4 text-sm text-gray-400">
                불러오는 중...
              </div>
            ) : comments.length === 0 ? (
              <div className="text-center py-6 text-sm text-gray-400">
                첫 번째 댓글을 남겨보세요!
              </div>
            ) : (
              <div className="space-y-3">
                {comments.map((comment) => (
                  <div key={comment.id} className="flex gap-2.5 group">
                    <Avatar
                      src={comment.author.profileImage}
                      alt={comment.author.nickname}
                      size="sm"
                    />
                    <div className="flex-1 min-w-0">
                      <div className="bg-gray-50 rounded-2xl px-3 py-2">
                        <p className="text-xs font-semibold text-gray-800 mb-0.5">
                          {comment.author.nickname}
                        </p>
                        <p className="text-sm text-gray-700 leading-relaxed">
                          {comment.content}
                        </p>
                      </div>
                      <p className="text-xs text-gray-400 mt-1 px-1">
                        {getRelativeTime(comment.createdAt)}
                      </p>
                    </div>
                    {comment.canDelete && (
                      <button
                        onClick={() => handleDeleteComment(comment.id)}
                        className="opacity-0 group-hover:opacity-100 transition-opacity mt-1 w-6 h-6 flex items-center justify-center text-gray-300 hover:text-red-400"
                      >
                        <Trash2 size={13} />
                      </button>
                    )}
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>

        {/* 댓글 입력창 (하단 고정) */}
        <form
          onSubmit={handleAddComment}
          className="shrink-0 flex items-center gap-2 px-4 py-3 border-t border-gray-100 bg-white"
        >
          <input
            ref={commentInputRef}
            type="text"
            value={commentText}
            onChange={(e) => setCommentText(e.target.value)}
            placeholder="댓글을 입력하세요..."
            className="flex-1 bg-gray-100 rounded-full px-4 py-2 text-sm outline-none focus:bg-gray-50 focus:ring-2 focus:ring-blue-200 transition-all"
          />
          <button
            type="submit"
            disabled={!commentText.trim() || addCommentMutation.isPending}
            className="w-9 h-9 flex items-center justify-center rounded-full bg-blue-500 text-white hover:bg-blue-600 disabled:opacity-40 disabled:cursor-not-allowed transition-colors"
          >
            <Send size={15} />
          </button>
        </form>
      </div>
    </div>
  );
}
