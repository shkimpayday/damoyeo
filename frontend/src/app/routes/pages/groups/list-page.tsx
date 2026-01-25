import { useState, useEffect, useRef, useCallback } from "react";
import { useSearchParams, Link } from "react-router";
import { X, SlidersHorizontal, ChevronDown } from "lucide-react";
import { useGroupsInfinite, GroupCard, CategoryCard, DEFAULT_CATEGORIES } from "@/features/groups";
import { EmptyState, Spinner } from "@/components/ui";

type SortType = "latest" | "popular" | "distance";

function GroupListPage() {
  const [searchParams, setSearchParams] = useSearchParams();
  const [sortBy, setSortBy] = useState<SortType>("latest");
  const [showSortMenu, setShowSortMenu] = useState(false);
  const selectedCategoryId = searchParams.get("categoryId")
    ? Number(searchParams.get("categoryId"))
    : undefined;

  const loadMoreRef = useRef<HTMLDivElement>(null);

  const {
    data,
    isLoading,
    isFetchingNextPage,
    hasNextPage,
    fetchNextPage,
  } = useGroupsInfinite({
    categoryId: selectedCategoryId,
    sort: sortBy,
    size: 12,
  });

  // Intersection Observer로 무한 스크롤 구현
  const handleObserver = useCallback(
    (entries: IntersectionObserverEntry[]) => {
      const [target] = entries;
      if (target.isIntersecting && hasNextPage && !isFetchingNextPage) {
        fetchNextPage();
      }
    },
    [hasNextPage, isFetchingNextPage, fetchNextPage]
  );

  useEffect(() => {
    const element = loadMoreRef.current;
    if (!element) return;

    const observer = new IntersectionObserver(handleObserver, {
      threshold: 0.1,
      rootMargin: "100px",
    });

    observer.observe(element);
    return () => observer.disconnect();
  }, [handleObserver]);

  const handleCategoryClick = (categoryId: number) => {
    const params = new URLSearchParams(searchParams);
    if (selectedCategoryId === categoryId) {
      params.delete("categoryId");
    } else {
      params.set("categoryId", String(categoryId));
    }
    setSearchParams(params);
  };

  const clearCategory = () => {
    const params = new URLSearchParams(searchParams);
    params.delete("categoryId");
    setSearchParams(params);
  };

  // 모든 페이지의 그룹을 하나의 배열로 합치기 (null 필터링)
  const allGroups = data?.pages.flatMap((page) => page.dtoList).filter(Boolean) ?? [];
  const selectedCategory = DEFAULT_CATEGORIES.find(c => c.id === selectedCategoryId);

  const sortOptions: { value: SortType; label: string }[] = [
    { value: "latest", label: "최신순" },
    { value: "popular", label: "인기순" },
    { value: "distance", label: "거리순" },
  ];

  const currentSortLabel = sortOptions.find(o => o.value === sortBy)?.label || "최신순";

  return (
    <div className="pb-20">
      {/* 상단 필터 영역 */}
      <div className="sticky top-[104px] md:top-16 z-40 bg-white border-b border-gray-100">
        {/* 카테고리 필터 - 가로 스크롤 */}
        <div className="app-content py-4 overflow-x-auto scrollbar-hide">
          <div className="flex gap-2">
            {/* 전체 버튼 */}
            <button
              onClick={clearCategory}
              className={`
                inline-flex items-center px-4 py-2.5 rounded-full text-sm font-semibold
                transition-all duration-200 whitespace-nowrap shrink-0
                ${!selectedCategoryId
                  ? "bg-primary-500 text-white shadow-md"
                  : "bg-gray-100 text-gray-700 hover:bg-gray-200"
                }
              `}
            >
              전체
            </button>
            {DEFAULT_CATEGORIES.map((category) => (
              <CategoryCard
                key={category.id}
                category={category}
                isSelected={selectedCategoryId === category.id}
                onClick={() => handleCategoryClick(category.id)}
              />
            ))}
          </div>
        </div>

        {/* 정렬 및 결과 수 */}
        <div className="app-content py-3 flex items-center justify-between border-t border-gray-50">
          {/* 정렬 드롭다운 */}
          <div className="relative">
            <button
              onClick={() => setShowSortMenu(!showSortMenu)}
              className="flex items-center gap-1 text-sm font-medium text-gray-700 hover:text-gray-900 transition-colors"
            >
              <SlidersHorizontal size={16} className="text-gray-400" />
              <span>{currentSortLabel}</span>
              <ChevronDown size={16} className={`text-gray-400 transition-transform ${showSortMenu ? 'rotate-180' : ''}`} />
            </button>

            {showSortMenu && (
              <>
                <div className="fixed inset-0 z-10" onClick={() => setShowSortMenu(false)} />
                <div className="absolute top-full left-0 mt-2 py-2 bg-white rounded-xl shadow-lg border border-gray-100 z-20 min-w-[120px]">
                  {sortOptions.map((option) => (
                    <button
                      key={option.value}
                      onClick={() => {
                        setSortBy(option.value);
                        setShowSortMenu(false);
                      }}
                      className={`w-full px-4 py-2 text-left text-sm transition-colors ${
                        sortBy === option.value
                          ? "text-primary-600 bg-primary-50 font-medium"
                          : "text-gray-600 hover:bg-gray-50"
                      }`}
                    >
                      {option.label}
                    </button>
                  ))}
                </div>
              </>
            )}
          </div>

          <span className="text-sm text-gray-400">
            {allGroups.length}개의 모임
          </span>
        </div>
      </div>

      {/* 선택된 카테고리 태그 */}
      {selectedCategory && (
        <div className="app-content py-4">
          <div className="inline-flex items-center gap-2 px-4 py-2 bg-primary-50 rounded-full">
            <span className="text-sm font-medium text-primary-700">
              {selectedCategory.icon} {selectedCategory.name}
            </span>
            <button
              onClick={clearCategory}
              className="p-0.5 hover:bg-primary-100 rounded-full transition-colors"
            >
              <X size={14} className="text-primary-600" />
            </button>
          </div>
        </div>
      )}

      {/* 모임 그리드 */}
      <div className="app-content section-spacing">
        {isLoading ? (
          <div className="flex items-center justify-center h-60">
            <Spinner size="lg" />
          </div>
        ) : allGroups.length > 0 ? (
          <>
            <div className="grid-groups">
              {allGroups.map((group) => (
                <GroupCard key={group.id} group={group} />
              ))}
            </div>

            {/* Load More Trigger */}
            <div ref={loadMoreRef} className="py-12 flex items-center justify-center">
              {isFetchingNextPage && <Spinner />}
              {!hasNextPage && allGroups.length > 0 && (
                <p className="text-sm text-gray-400">모든 모임을 불러왔습니다</p>
              )}
            </div>
          </>
        ) : (
          <div className="py-20">
            <EmptyState
              icon="🔍"
              title="모임이 없습니다"
              description={
                selectedCategory
                  ? `${selectedCategory.name} 카테고리에 모임이 없습니다`
                  : "새로운 모임을 만들어보세요!"
              }
              action={
                <Link
                  to="/groups/create"
                  className="inline-flex items-center px-8 py-3 bg-primary-500 text-white rounded-full font-semibold hover:bg-primary-600 transition-colors shadow-lg shadow-primary-500/25"
                >
                  모임 만들기
                </Link>
              }
            />
          </div>
        )}
      </div>
    </div>
  );
}

export default GroupListPage;
