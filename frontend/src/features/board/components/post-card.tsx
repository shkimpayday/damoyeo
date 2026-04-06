import { useState } from "react";
import { Heart, MessageCircle, MoreVertical, Trash2, Pin } from "lucide-react";
import { Avatar } from "@/components/ui";
import { getRelativeTime } from "@/utils/date";
import { useToggleBoardPostLike, useDeleteBoardPost } from "../hooks/use-board";
import {
  BOARD_CATEGORY_LABELS,
  BOARD_CATEGORY_COLORS,
  type BoardPostDTO,
} from "../types";

interface PostCardProps {
  post: BoardPostDTO;
  groupId: number;
  onClick: (post: BoardPostDTO) => void;
}

/**
 * 게시글 카드 컴포넌트
 *
 * @description
 * 게시판 목록에서 각 게시글을 카드 형태로 표시합니다.
 * - 카테고리 뱃지
 * - 제목 + 본문 미리보기
 * - 이미지 썸네일 (있는 경우)
 * - 좋아요 / 댓글 수
 * - 삭제 메뉴 (본인 or 운영진)
 */
export function PostCard({ post, groupId, onClick }: PostCardProps) {
  const [showMenu, setShowMenu] = useState(false);

  const toggleLikeMutation = useToggleBoardPostLike();
  const deleteMutation = useDeleteBoardPost();

  const categoryColor = BOARD_CATEGORY_COLORS[post.category];
  const categoryLabel = BOARD_CATEGORY_LABELS[post.category];

  const handleLike = (e: React.MouseEvent) => {
    e.stopPropagation();
    toggleLikeMutation.mutate(post.id);
  };

  const handleDelete = async (e: React.MouseEvent) => {
    e.stopPropagation();
    setShowMenu(false);
    if (!confirm("게시글을 삭제하시겠습니까?")) return;
    try {
      await deleteMutation.mutateAsync({ postId: post.id, groupId });
    } catch {
      alert("삭제에 실패했습니다.");
    }
  };

  return (
    <div
      onClick={() => onClick(post)}
      className="bg-white rounded-2xl shadow-sm border border-gray-100 p-4 cursor-pointer hover:shadow-md transition-all duration-200 active:scale-[0.99]"
    >
      {/* 상단: 작성자 정보 + 메뉴 */}
      <div className="flex items-start justify-between mb-3">
        <div className="flex items-center gap-2.5 min-w-0">
          <Avatar
            src={post.author.profileImage}
            alt={post.author.nickname}
            size="sm"
          />
          <div className="min-w-0">
            <p className="text-sm font-semibold text-gray-900 truncate">
              {post.author.nickname}
            </p>
            <p className="text-xs text-gray-400">{getRelativeTime(post.createdAt)}</p>
          </div>
        </div>

        <div className="flex items-center gap-1.5 shrink-0">
          {/* 카테고리 뱃지 */}
          <span
            className={`text-xs font-medium px-2 py-0.5 rounded-full border ${categoryColor.bg} ${categoryColor.text} ${categoryColor.border}`}
          >
            {categoryLabel}
          </span>

          {/* 고정 핀 */}
          {post.isPinned && (
            <Pin size={14} className="text-orange-500 fill-orange-500" />
          )}

          {/* 삭제 메뉴 */}
          {post.canDelete && (
            <div className="relative">
              <button
                onClick={(e) => {
                  e.stopPropagation();
                  setShowMenu((v) => !v);
                }}
                className="w-7 h-7 flex items-center justify-center rounded-full hover:bg-gray-100 text-gray-400"
              >
                <MoreVertical size={16} />
              </button>
              {showMenu && (
                <>
                  {/* 바깥 클릭 닫기 */}
                  <div
                    className="fixed inset-0 z-10"
                    onClick={(e) => {
                      e.stopPropagation();
                      setShowMenu(false);
                    }}
                  />
                  <div className="absolute right-0 top-8 z-20 bg-white border border-gray-200 rounded-xl shadow-lg py-1 min-w-[110px]">
                    <button
                      onClick={handleDelete}
                      className="flex items-center gap-2 w-full px-3 py-2 text-sm text-red-600 hover:bg-red-50"
                    >
                      <Trash2 size={14} />
                      삭제
                    </button>
                  </div>
                </>
              )}
            </div>
          )}
        </div>
      </div>

      {/* 제목 */}
      <h3 className="text-sm font-bold text-gray-900 mb-1.5 line-clamp-1">
        {post.title}
      </h3>

      {/* 본문 미리보기 + 이미지 썸네일 */}
      <div className="flex gap-3">
        <p className="flex-1 text-sm text-gray-500 line-clamp-2 leading-relaxed">
          {post.content}
        </p>
        {post.thumbnailUrl && (
          <div className="w-16 h-16 shrink-0 rounded-lg overflow-hidden bg-gray-100">
            <img
              src={post.thumbnailUrl}
              alt="게시글 이미지"
              className="w-full h-full object-cover"
            />
          </div>
        )}
      </div>

      {/* 이미지가 여러 개일 때 미리보기 그리드 */}
      {post.imageCount > 0 && !post.thumbnailUrl && post.images.length > 0 && (
        <div className="mt-2.5 grid grid-cols-3 gap-1 rounded-lg overflow-hidden">
          {post.images.slice(0, 3).map((img, idx) => (
            <div
              key={img.id}
              className="relative aspect-square bg-gray-100 overflow-hidden"
            >
              <img
                src={img.imageUrl}
                alt=""
                className="w-full h-full object-cover"
              />
              {idx === 2 && post.imageCount > 3 && (
                <div className="absolute inset-0 bg-black/50 flex items-center justify-center">
                  <span className="text-white text-sm font-bold">
                    +{post.imageCount - 3}
                  </span>
                </div>
              )}
            </div>
          ))}
        </div>
      )}

      {/* 하단: 좋아요 / 댓글 */}
      <div className="flex items-center gap-4 mt-3 pt-3 border-t border-gray-50">
        <button
          onClick={handleLike}
          className={`flex items-center gap-1.5 text-sm transition-colors ${
            post.liked ? "text-red-500" : "text-gray-400 hover:text-red-400"
          }`}
        >
          <Heart
            size={16}
            className={post.liked ? "fill-red-500" : ""}
          />
          <span className="font-medium">{post.likeCount}</span>
        </button>
        <div className="flex items-center gap-1.5 text-sm text-gray-400">
          <MessageCircle size={16} />
          <span className="font-medium">{post.commentCount}</span>
        </div>
      </div>
    </div>
  );
}
