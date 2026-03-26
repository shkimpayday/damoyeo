import { useState } from "react";
import { ChevronRight, Image as ImageIcon, Images } from "lucide-react";
import { Spinner } from "@/components/ui";
import { getImageUrl } from "@/utils";
import { useRecentGalleryPosts, useGalleryPostCount } from "../hooks/use-gallery";
import { PostLightbox } from "./post-lightbox";

/**
 * ============================================================================
 * 갤러리 미리보기 컴포넌트
 * ============================================================================
 *
 * [역할]
 * 모임 상세 페이지에서 갤러리 탭 클릭 전 미리보기를 표시합니다.
 *
 * [구조 변경]
 * - 기존: 개별 이미지 단위
 * - 변경: 게시물 단위 (여러 이미지 묶음)
 *
 * [표시 내용]
 * - 최근 4개 게시물 썸네일
 * - 전체 게시물 개수
 * - 다중 이미지 표시 (아이콘)
 * - 더보기 버튼
 */
interface GalleryPreviewProps {
  /** 모임 ID */
  groupId: number;
  /** 갤러리 전체보기 클릭 콜백 */
  onViewAll: () => void;
}

export function GalleryPreview({ groupId, onViewAll }: GalleryPreviewProps) {
  const [showLightbox, setShowLightbox] = useState(false);
  const [lightboxIndex, setLightboxIndex] = useState(0);

  const { data: posts, isLoading } = useRecentGalleryPosts(groupId, 4);
  const { data: countData } = useGalleryPostCount(groupId);

  const totalCount = countData?.count || 0;

  // 로딩 상태
  if (isLoading) {
    return (
      <div className="bg-white rounded-2xl p-5 shadow-sm">
        <div className="flex items-center justify-between mb-4">
          <h3 className="text-base font-bold text-gray-900">갤러리</h3>
        </div>
        <div className="flex justify-center py-8">
          <Spinner size="md" />
        </div>
      </div>
    );
  }

  // 빈 상태
  if (!posts || posts.length === 0) {
    return (
      <div className="bg-white rounded-2xl p-5 shadow-sm">
        <div className="flex items-center justify-between mb-4">
          <h3 className="text-base font-bold text-gray-900">갤러리</h3>
          <button
            onClick={onViewAll}
            className="flex items-center gap-1 text-sm text-primary-500 hover:text-primary-600"
          >
            <span>전체보기</span>
            <ChevronRight size={16} />
          </button>
        </div>
        <div className="py-8 text-center">
          <ImageIcon size={40} className="mx-auto mb-2 text-gray-300" />
          <p className="text-sm text-gray-500">아직 사진이 없습니다</p>
        </div>
      </div>
    );
  }

  // 게시물 클릭 핸들러
  const handlePostClick = (index: number) => {
    setLightboxIndex(index);
    setShowLightbox(true);
  };

  return (
    <>
      <div className="bg-white rounded-2xl p-5 shadow-sm">
        <div className="flex items-center justify-between mb-4">
          <h3 className="text-base font-bold text-gray-900">
            갤러리
            {totalCount > 0 && (
              <span className="ml-2 text-primary-500">{totalCount}</span>
            )}
          </h3>
          <button
            onClick={onViewAll}
            className="flex items-center gap-1 text-sm text-primary-500 hover:text-primary-600"
          >
            <span>전체보기</span>
            <ChevronRight size={16} />
          </button>
        </div>

        {/* 게시물 그리드 (1x4) */}
        <div className="grid grid-cols-4 gap-1 rounded-xl overflow-hidden">
          {posts.map((post, index) => (
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
                <div className="absolute top-1 right-1 bg-black/60 text-white px-1 py-0.5 rounded flex items-center gap-0.5">
                  <Images size={10} />
                  <span className="text-[10px] font-medium">{post.imageCount}</span>
                </div>
              )}

              {/* 마지막 게시물에 더보기 오버레이 */}
              {index === 3 && totalCount > 4 && (
                <div className="absolute inset-0 bg-black/50 flex items-center justify-center">
                  <span className="text-white font-bold text-lg">
                    +{totalCount - 4}
                  </span>
                </div>
              )}
            </button>
          ))}
        </div>
      </div>

      {/* 라이트박스 */}
      {showLightbox && posts && (
        <PostLightbox
          posts={posts}
          currentIndex={lightboxIndex}
          onClose={() => setShowLightbox(false)}
          onIndexChange={setLightboxIndex}
        />
      )}
    </>
  );
}
