import { useState, useRef, useCallback } from "react";
import { useSearchParams } from "react-router";
import { useGroupsInfinite, GroupCard, DEFAULT_CATEGORIES } from "@/features/groups";
import { CategoryChip, EmptyState, Spinner } from "@/components/ui";

/**
 * 모임 검색 페이지
 *
 * [기능]
 * - 키워드 검색 (모임 이름)
 * - 카테고리 필터링
 * - 정렬 옵션 (최신순, 인기순)
 * - 무한 스크롤 페이지네이션
 *
 * [URL 파라미터]
 * - keyword: 검색 키워드
 * - categoryId: 카테고리 ID
 * - sort: 정렬 기준 (latest, popular)
 */
function SearchPage() {
  const [searchParams, setSearchParams] = useSearchParams();
  const [keyword, setKeyword] = useState(searchParams.get("keyword") || "");

  // URL에서 파라미터 추출
  const selectedCategoryId = searchParams.get("categoryId")
    ? Number(searchParams.get("categoryId"))
    : undefined;
  const selectedSort = searchParams.get("sort") || "latest";

  // 무한 스크롤 쿼리
  const {
    data,
    isLoading,
    isFetchingNextPage,
    hasNextPage,
    fetchNextPage,
  } = useGroupsInfinite({
    keyword: searchParams.get("keyword") || undefined,
    categoryId: selectedCategoryId,
    sort: selectedSort as "latest" | "popular",
    size: 12,
  });

  // Intersection Observer를 위한 ref
  const observerRef = useRef<IntersectionObserver | null>(null);
  const loadMoreRef = useCallback(
    (node: HTMLDivElement | null) => {
      if (isLoading || isFetchingNextPage) return;

      // 기존 observer 해제
      if (observerRef.current) {
        observerRef.current.disconnect();
      }

      // 새 observer 생성
      observerRef.current = new IntersectionObserver((entries) => {
        if (entries[0].isIntersecting && hasNextPage) {
          fetchNextPage();
        }
      });

      // 요소 관찰 시작
      if (node) {
        observerRef.current.observe(node);
      }
    },
    [isLoading, isFetchingNextPage, hasNextPage, fetchNextPage]
  );

  // 검색 폼 제출
  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault();
    const params = new URLSearchParams(searchParams);
    if (keyword.trim()) {
      params.set("keyword", keyword.trim());
    } else {
      params.delete("keyword");
    }
    setSearchParams(params);
  };

  // 카테고리 클릭 (토글)
  const handleCategoryClick = (categoryId: number) => {
    const params = new URLSearchParams(searchParams);
    if (selectedCategoryId === categoryId) {
      params.delete("categoryId");
    } else {
      params.set("categoryId", String(categoryId));
    }
    setSearchParams(params);
  };

  // 정렬 변경
  const handleSortChange = (sort: string) => {
    const params = new URLSearchParams(searchParams);
    params.set("sort", sort);
    setSearchParams(params);
  };

  // 전체 결과 개수 계산
  const totalCount = data?.pages[0]?.totalCount ?? 0;

  // 모든 페이지의 모임 목록을 평탄화
  const allGroups = data?.pages.flatMap((page) => page.dtoList ?? []) ?? [];

  return (
    <div className="p-4">
      {/* 검색 입력 */}
      <form onSubmit={handleSearch} className="mb-4">
        <div className="relative">
          <input
            type="text"
            value={keyword}
            onChange={(e) => setKeyword(e.target.value)}
            placeholder="모임 이름으로 검색"
            className="w-full px-4 py-3 pl-10 bg-gray-100 rounded-lg outline-none focus:ring-2 focus:ring-primary-500"
          />
          <span className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400">
            🔍
          </span>
          {keyword && (
            <button
              type="button"
              onClick={() => {
                setKeyword("");
                const params = new URLSearchParams(searchParams);
                params.delete("keyword");
                setSearchParams(params);
              }}
              className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-600"
            >
              ✕
            </button>
          )}
        </div>
      </form>

      {/* 카테고리 필터 */}
      <div className="mb-4">
        <h3 className="text-sm font-medium text-gray-700 mb-2">카테고리</h3>
        <div className="flex flex-wrap gap-2">
          {DEFAULT_CATEGORIES.map((category) => (
            <CategoryChip
              key={category.id}
              category={category}
              size="sm"
              isSelected={selectedCategoryId === category.id}
              onClick={() => handleCategoryClick(category.id)}
            />
          ))}
        </div>
      </div>

      {/* 정렬 옵션 */}
      <div className="mb-4 flex items-center justify-between">
        <h3 className="text-sm font-medium text-gray-700">
          검색 결과 {totalCount > 0 && `(${totalCount}개)`}
        </h3>
        <div className="flex gap-2">
          <button
            onClick={() => handleSortChange("latest")}
            className={`px-3 py-1.5 text-sm rounded-full transition-colors ${
              selectedSort === "latest"
                ? "bg-primary-500 text-white"
                : "bg-gray-100 text-gray-600 hover:bg-gray-200"
            }`}
          >
            최신순
          </button>
          <button
            onClick={() => handleSortChange("popular")}
            className={`px-3 py-1.5 text-sm rounded-full transition-colors ${
              selectedSort === "popular"
                ? "bg-primary-500 text-white"
                : "bg-gray-100 text-gray-600 hover:bg-gray-200"
            }`}
          >
            인기순
          </button>
        </div>
      </div>

      {/* 검색 결과 */}
      <div>
        {isLoading ? (
          <div className="flex items-center justify-center h-40">
            <Spinner />
          </div>
        ) : allGroups.length > 0 ? (
          <>
            <div className="grid-groups">
              {allGroups.map((group) => (
                <GroupCard key={group.id} group={group} />
              ))}
            </div>

            {/* 무한 스크롤 트리거 */}
            <div
              ref={loadMoreRef}
              className="h-10 flex items-center justify-center mt-4"
            >
              {isFetchingNextPage && <Spinner size="sm" />}
              {!hasNextPage && allGroups.length > 0 && (
                <p className="text-sm text-gray-400">
                  모든 모임을 불러왔습니다
                </p>
              )}
            </div>
          </>
        ) : (
          <EmptyState
            icon="🔍"
            title="검색 결과가 없습니다"
            description="다른 키워드나 카테고리로 검색해보세요"
          />
        )}
      </div>
    </div>
  );
}

export default SearchPage;
