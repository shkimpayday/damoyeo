import { useState, useEffect, useRef, useCallback } from "react";
import { Plus, Images } from "lucide-react";
import { Spinner, EmptyState } from "@/components/ui";
import { getImageUrl } from "@/utils";
import { useGalleryPostsInfinite, useDeleteGalleryPost } from "../hooks/use-gallery";
import { PostLightbox } from "./post-lightbox";
import { GalleryUploadModal } from "./gallery-upload-modal";
import type { GalleryPostDTO } from "../types";

/**
 * ============================================================================
 * 갤러리 그리드 컴포넌트
 * ============================================================================
 *
 * [역할]
 * 갤러리 게시물을 그리드 형태로 표시하고 무한 스크롤을 지원합니다.
 *
 * [구조 변경]
 * - 기존: 개별 이미지 단위 그리드
 * - 변경: 게시물 단위 그리드 (여러 이미지 묶음)
 *
 * [기능]
 * - 게시물 그리드 레이아웃 (3열)
 * - 무한 스크롤
 * - 게시물 클릭 시 라이트박스 열기 (캐러셀)
 * - 다중 이미지 표시 (아이콘)
 * - 게시물 업로드 버튼
 * - 게시물 삭제
 */
interface GalleryGridProps {
  /** 모임 ID */
  groupId: number;
  /** 업로드 가능 여부 (모임 멤버인 경우) */
  canUpload?: boolean;
}

export function GalleryGrid({ groupId, canUpload = false }: GalleryGridProps) {
  const [showLightbox, setShowLightbox] = useState(false);
  const [lightboxIndex, setLightboxIndex] = useState(0);
  const [showUploadModal, setShowUploadModal] = useState(false);

  // 무한 스크롤 옵저버 ref
  const loadMoreRef = useRef<HTMLDivElement>(null);

  // 갤러리 게시물 조회 (무한 스크롤)
  const {
    data,
    isLoading,
    isFetchingNextPage,
    hasNextPage,
    fetchNextPage,
  } = useGalleryPostsInfinite(groupId);

  // 삭제 mutation
  const deleteMutation = useDeleteGalleryPost();

  // 모든 게시물 평탄화 (dtoList가 null일 수 있으므로 ?? [] 처리)
  const allPosts: GalleryPostDTO[] = data?.pages.flatMap((page) => page.dtoList ?? []) || [];

  // Intersection Observer로 무한 스크롤 처리
  useEffect(() => {
    const observer = new IntersectionObserver(
      (entries) => {
        if (entries[0].isIntersecting && hasNextPage && !isFetchingNextPage) {
          fetchNextPage();
        }
      },
      { threshold: 0.1 }
    );

    if (loadMoreRef.current) {
      observer.observe(loadMoreRef.current);
    }

    return () => observer.disconnect();
  }, [hasNextPage, isFetchingNextPage, fetchNextPage]);

  // 게시물 클릭 핸들러
  const handlePostClick = useCallback((index: number) => {
    setLightboxIndex(index);
    setShowLightbox(true);
  }, []);

  // 게시물 삭제 핸들러
  const handleDelete = useCallback(
    async (postId: number) => {
      await deleteMutation.mutateAsync({ postId, groupId });
    },
    [deleteMutation, groupId]
  );

  // 로딩 상태
  if (isLoading) {
    return (
      <div className="flex items-center justify-center py-12">
        <Spinner size="lg" />
      </div>
    );
  }

  // 빈 상태
  if (allPosts.length === 0) {
    return (
      <div className="py-8">
        <EmptyState
          icon="🖼️"
          title="아직 사진이 없습니다"
          description={canUpload ? "첫 번째 사진을 업로드해보세요!" : undefined}
          action={
            canUpload
              ? {
                  label: "사진 올리기",
                  onClick: () => setShowUploadModal(true),
                }
              : undefined
          }
        />

        {showUploadModal && (
          <GalleryUploadModal
            groupId={groupId}
            onClose={() => setShowUploadModal(false)}
          />
        )}
      </div>
    );
  }

  return (
    <div className="space-y-4">
      {/* 업로드 버튼 */}
      {canUpload && (
        <div className="flex justify-end">
          <button
            onClick={() => setShowUploadModal(true)}
            className="flex items-center gap-2 px-4 py-2 bg-primary-500 text-white rounded-lg hover:bg-primary-600 transition-colors"
          >
            <Plus size={18} />
            <span>사진 올리기</span>
          </button>
        </div>
      )}

      {/* 게시물 그리드 */}
      <div className="grid grid-cols-3 gap-1">
        {allPosts.map((post, index) => (
          <button
            key={post.id}
            type="button"
            onClick={() => handlePostClick(index)}
            className="relative aspect-square overflow-hidden bg-gray-100 hover:opacity-90 transition-opacity"
          >
            {/* 썸네일 이미지 */}
            <img
              src={getImageUrl(post.thumbnailUrl) || ""}
              alt={post.caption || "갤러리 게시물"}
              className="w-full h-full object-cover"
              loading="lazy"
            />

            {/* 다중 이미지 표시 아이콘 */}
            {post.imageCount > 1 && (
              <div className="absolute top-2 right-2 bg-black/60 text-white px-1.5 py-0.5 rounded flex items-center gap-1">
                <Images size={14} />
                <span className="text-xs font-medium">{post.imageCount}</span>
              </div>
            )}
          </button>
        ))}
      </div>

      {/* 더 불러오기 트리거 */}
      <div ref={loadMoreRef} className="h-1" />

      {/* 로딩 인디케이터 */}
      {isFetchingNextPage && (
        <div className="flex justify-center py-4">
          <Spinner size="md" />
        </div>
      )}

      {/* 라이트박스 (캐러셀 지원) */}
      {showLightbox && (
        <PostLightbox
          posts={allPosts}
          currentIndex={lightboxIndex}
          onClose={() => setShowLightbox(false)}
          onDelete={handleDelete}
          onIndexChange={setLightboxIndex}
        />
      )}

      {/* 업로드 모달 */}
      {showUploadModal && (
        <GalleryUploadModal
          groupId={groupId}
          onClose={() => setShowUploadModal(false)}
        />
      )}
    </div>
  );
}
