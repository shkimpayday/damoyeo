import { useState } from "react";
import { useSearchParams } from "react-router";
import { useGroupsList, GroupCard, DEFAULT_CATEGORIES } from "@/features/groups";
import { CategoryChip, EmptyState, Spinner } from "@/components/ui";

function SearchPage() {
  const [searchParams, setSearchParams] = useSearchParams();
  const [keyword, setKeyword] = useState(searchParams.get("keyword") || "");
  const selectedCategoryId = searchParams.get("categoryId")
    ? Number(searchParams.get("categoryId"))
    : undefined;

  const { data, isLoading } = useGroupsList({
    keyword: searchParams.get("keyword") || undefined,
    categoryId: selectedCategoryId,
    page: 1,
    size: 20,
  });

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault();
    const params = new URLSearchParams(searchParams);
    if (keyword) {
      params.set("keyword", keyword);
    } else {
      params.delete("keyword");
    }
    setSearchParams(params);
  };

  const handleCategoryClick = (categoryId: number) => {
    const params = new URLSearchParams(searchParams);
    if (selectedCategoryId === categoryId) {
      params.delete("categoryId");
    } else {
      params.set("categoryId", String(categoryId));
    }
    setSearchParams(params);
  };

  return (
    <div className="p-4">
      {/* Search Input */}
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
        </div>
      </form>

      {/* Category Filter */}
      <div className="mb-6">
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

      {/* Results */}
      <div>
        <h3 className="text-sm font-medium text-gray-700 mb-3">
          검색 결과 {data?.totalCount ? `(${data.totalCount}개)` : ""}
        </h3>

        {isLoading ? (
          <div className="flex items-center justify-center h-40">
            <Spinner />
          </div>
        ) : data?.dtoList && data.dtoList.length > 0 ? (
          <div className="grid-groups">
            {data.dtoList.map((group) => (
              <GroupCard key={group.id} group={group} />
            ))}
          </div>
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
