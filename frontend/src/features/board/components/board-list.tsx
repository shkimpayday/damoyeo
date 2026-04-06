import { useState, useRef, useCallback, useEffect } from "react";
import { PenSquare, Loader2, StickyNote } from "lucide-react";
import { useBoardPostsInfinite } from "../hooks/use-board";
import { PostCard } from "./post-card";
import { PostDetailModal } from "./post-detail-modal";
import { PostCreateModal } from "./post-create-modal";
import {
  BOARD_CATEGORY_LABELS,
  type BoardCategory,
  type BoardPostDTO,
  type GroupRole,
} from "../types";

interface BoardListProps {
  groupId: number;
  /** 현재 사용자의 모임 역할 (없으면 비회원) */
  userRole?: GroupRole | null;
}

/** 카테고리 필터 탭 (전체 포함) */
const FILTER_TABS: { key: BoardCategory | "ALL"; label: string }[] = [
  { key: "ALL", label: "전체" },
  { key: "NOTICE", label: BOARD_CATEGORY_LABELS.NOTICE },
  { key: "GREETING", label: BOARD_CATEGORY_LABELS.GREETING },
  { key: "REVIEW", label: BOARD_CATEGORY_LABELS.REVIEW },
  { key: "FREE", label: BOARD_CATEGORY_LABELS.FREE },
];

/**
 * 모임 게시판 목록 컴포넌트
 *
 * @description
 * 모임 게시판의 메인 컴포넌트입니다.
 * - 카테고리 필터 탭 (전체, 공지, 가입인사, 모임후기, 자유)
 * - 무한 스크롤 게시글 목록
 * - 게시글 상세 모달
 * - 새 게시글 작성 버튼 (멤버만)
 */
export function BoardList({ groupId, userRole }: BoardListProps) {
  const [activeCategory, setActiveCategory] = useState<BoardCategory | "ALL">("ALL");
  const [selectedPost, setSelectedPost] = useState<BoardPostDTO | null>(null);
  const [showCreateModal, setShowCreateModal] = useState(false);

  const observerRef = useRef<IntersectionObserver | null>(null);
  const loadMoreRef = useRef<HTMLDivElement>(null);

  const category = activeCategory === "ALL" ? undefined : activeCategory;

  const {
    data,
    fetchNextPage,
    hasNextPage,
    isFetchingNextPage,
    isLoading,
  } = useBoardPostsInfinite(groupId, category);

  // 모든 페이지의 게시글 평탄화
  const posts = data?.pages.flatMap((page) => page.dtoList) ?? [];

  // 무한 스크롤: 하단 sentinel 관찰
  const handleObserver = useCallback(
    (entries: IntersectionObserverEntry[]) => {
      const [target] = entries;
      if (target.isIntersecting && hasNextPage && !isFetchingNextPage) {
        fetchNextPage();
      }
    },
    [fetchNextPage, hasNextPage, isFetchingNextPage]
  );

  useEffect(() => {
    const element = loadMoreRef.current;
    if (!element) return;

    observerRef.current = new IntersectionObserver(handleObserver, {
      threshold: 0.1,
    });
    observerRef.current.observe(element);

    return () => observerRef.current?.disconnect();
  }, [handleObserver]);

  return (
    <div className="relative min-h-full">
      {/* 카테고리 필터 탭 */}
      <div className="sticky top-0 z-10 bg-white border-b border-gray-100">
        <div className="flex overflow-x-auto scrollbar-hide px-4 py-2 gap-2">
          {FILTER_TABS.map((tab) => (
            <button
              key={tab.key}
              onClick={() => setActiveCategory(tab.key)}
              className={`shrink-0 px-3.5 py-1.5 rounded-full text-xs font-semibold border transition-all ${
                activeCategory === tab.key
                  ? tab.key === "NOTICE"
                    ? "bg-red-500 text-white border-red-500"
                    : tab.key === "GREETING"
                    ? "bg-green-500 text-white border-green-500"
                    : tab.key === "REVIEW"
                    ? "bg-blue-500 text-white border-blue-500"
                    : tab.key === "FREE"
                    ? "bg-gray-700 text-white border-gray-700"
                    : "bg-gray-900 text-white border-gray-900"
                  : "bg-white text-gray-500 border-gray-200 hover:border-gray-400"
              }`}
            >
              {tab.label}
            </button>
          ))}
        </div>
      </div>

      {/* 게시글 목록 */}
      <div className="px-4 py-4 space-y-3">
        {isLoading ? (
          // 스켈레톤 로딩
          Array.from({ length: 4 }).map((_, i) => (
            <div
              key={i}
              className="bg-white rounded-2xl border border-gray-100 p-4 animate-pulse"
            >
              <div className="flex items-center gap-2 mb-3">
                <div className="w-8 h-8 bg-gray-200 rounded-full" />
                <div className="flex-1 space-y-1.5">
                  <div className="h-3 bg-gray-200 rounded w-24" />
                  <div className="h-2.5 bg-gray-100 rounded w-16" />
                </div>
              </div>
              <div className="h-4 bg-gray-200 rounded w-3/4 mb-2" />
              <div className="h-3 bg-gray-100 rounded w-full mb-1" />
              <div className="h-3 bg-gray-100 rounded w-2/3" />
            </div>
          ))
        ) : posts.length === 0 ? (
          <div className="flex flex-col items-center justify-center py-16 text-center">
            <div className="w-16 h-16 bg-gray-100 rounded-full flex items-center justify-center mb-4">
              <StickyNote size={28} className="text-gray-400" />
            </div>
            <p className="text-base font-semibold text-gray-600 mb-1">
              아직 게시글이 없습니다
            </p>
            <p className="text-sm text-gray-400">
              {activeCategory === "GREETING"
                ? "모임에 가입하셨나요? 가입인사를 남겨보세요!"
                : activeCategory === "REVIEW"
                ? "정모 후기를 공유해보세요!"
                : "첫 번째 게시글을 작성해보세요"}
            </p>
            {userRole && (
              <button
                onClick={() => setShowCreateModal(true)}
                className="mt-4 px-5 py-2 bg-blue-500 text-white text-sm font-semibold rounded-xl hover:bg-blue-600 transition-colors"
              >
                게시글 작성하기
              </button>
            )}
          </div>
        ) : (
          <>
            {posts.map((post) => (
              <PostCard
                key={post.id}
                post={post}
                groupId={groupId}
                onClick={setSelectedPost}
              />
            ))}

            {/* 무한 스크롤 트리거 */}
            <div ref={loadMoreRef} className="h-4" />

            {isFetchingNextPage && (
              <div className="flex justify-center py-4">
                <Loader2 size={20} className="animate-spin text-gray-400" />
              </div>
            )}

            {!hasNextPage && posts.length > 0 && (
              <p className="text-center text-xs text-gray-400 py-4">
                모든 게시글을 불러왔습니다
              </p>
            )}
          </>
        )}
      </div>

      {/* 글쓰기 FAB (멤버만) */}
      {userRole && (
        <button
          onClick={() => setShowCreateModal(true)}
          className="fixed bottom-20 right-4 z-20 w-14 h-14 bg-blue-500 hover:bg-blue-600 active:scale-95 text-white rounded-full shadow-lg flex items-center justify-center transition-all"
          aria-label="게시글 작성"
        >
          <PenSquare size={22} />
        </button>
      )}

      {/* 게시글 상세 모달 */}
      {selectedPost && (
        <PostDetailModal
          post={selectedPost}
          groupId={groupId}
          onClose={() => setSelectedPost(null)}
        />
      )}

      {/* 게시글 작성 모달 */}
      {showCreateModal && (
        <PostCreateModal
          groupId={groupId}
          userRole={userRole}
          onClose={() => setShowCreateModal(false)}
        />
      )}
    </div>
  );
}
