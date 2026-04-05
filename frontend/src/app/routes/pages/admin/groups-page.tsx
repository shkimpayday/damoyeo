import { useState } from "react";
import { Link } from "react-router";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { jwtAxios } from "@/lib/axios";
import { ENV } from "@/config";
import { Spinner } from "@/components/ui";
import { ExternalLink } from "lucide-react";

/**
 * 모임 관리 페이지
 *
 * [기능]
 * - 모임 목록 조회 (페이지네이션)
 * - 모임 검색 (이름)
 * - 모임 상태 변경 (ACTIVE ↔ INACTIVE)
 * - 모임 삭제 (소프트 삭제)
 */

interface AdminGroupDTO {
  id: number;
  name: string;
  categoryName: string;
  ownerNickname: string;
  ownerEmail: string;
  memberCount: number;
  maxMembers: number;
  status: string;
  createdAt: string;
}

interface PageResponseDTO<T> {
  dtoList: T[];
  totalCount: number;
  totalPage: number;
  current: number;
  prev: boolean;
  next: boolean;
}

// 모임 목록 조회
const fetchGroups = async (page: number, keyword: string, status: string): Promise<PageResponseDTO<AdminGroupDTO>> => {
  const params = {
    page,
    size: 10,
    keyword: keyword || undefined,
    status: status || undefined,
  };
  const res = await jwtAxios.get(`${ENV.API_URL}/api/admin/groups`, { params });
  return res.data;
};

// 모임 상태 변경
const updateGroupStatus = async ({ groupId, status }: { groupId: number; status: string }) => {
  const res = await jwtAxios.patch(`${ENV.API_URL}/api/admin/groups/${groupId}/status`, { status });
  return res.data;
};

// 상태 뱃지 컴포넌트
function StatusBadge({ status }: { status: string }) {
  const config: Record<string, { bg: string; text: string; label: string }> = {
    ACTIVE: { bg: "bg-green-100", text: "text-green-700", label: "활성" },
    INACTIVE: { bg: "bg-gray-100", text: "text-gray-600", label: "비활성" },
    DELETED: { bg: "bg-red-100", text: "text-red-700", label: "삭제됨" },
  };
  const c = config[status] || config.ACTIVE;
  return (
    <span className={`px-2 py-0.5 text-xs rounded-full ${c.bg} ${c.text}`}>
      {c.label}
    </span>
  );
}

export function GroupsPage() {
  const queryClient = useQueryClient();
  const [page, setPage] = useState(1);
  const [keyword, setKeyword] = useState("");
  const [searchInput, setSearchInput] = useState("");
  const [statusFilter, setStatusFilter] = useState("");

  // 모임 목록 조회
  const { data, isLoading, isError } = useQuery({
    queryKey: ["admin", "groups", page, keyword, statusFilter],
    queryFn: () => fetchGroups(page, keyword, statusFilter),
    staleTime: 30 * 1000,
  });

  // 상태 변경 Mutation
  const statusMutation = useMutation({
    mutationFn: updateGroupStatus,
    onSuccess: () => {
      // 관리자 모임 쿼리 무효화
      queryClient.invalidateQueries({ queryKey: ["admin", "groups"] });
      // 사용자 페이지 모임 쿼리도 무효화 (메인 사이트에 즉시 반영)
      queryClient.invalidateQueries({ queryKey: ["groups"] });
    },
  });

  // 검색 핸들러
  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault();
    setKeyword(searchInput);
    setPage(1);
  };

  // 상태 변경 핸들러
  const handleStatusChange = (groupId: number, currentStatus: string) => {
    const newStatus = currentStatus === "ACTIVE" ? "INACTIVE" : "ACTIVE";
    const action = newStatus === "ACTIVE" ? "활성화" : "비활성화";

    if (confirm(`이 모임을 ${action}하시겠습니까?`)) {
      statusMutation.mutate({ groupId, status: newStatus });
    }
  };

  // 삭제 핸들러
  const handleDelete = (groupId: number) => {
    if (confirm("정말로 이 모임을 삭제하시겠습니까?\n삭제된 모임은 복구할 수 없습니다.")) {
      statusMutation.mutate({ groupId, status: "DELETED" });
    }
  };

  return (
    <div>
      {/* 페이지 헤더 */}
      <div className="mb-6">
        <h1 className="text-2xl font-bold text-gray-800">모임 관리</h1>
        <p className="text-gray-500 mt-1">모임 목록 조회 및 상태를 관리합니다</p>
      </div>

      {/* 검색 및 필터 */}
      <div className="bg-white rounded-xl shadow-sm p-4 mb-6 border border-gray-100">
        <form onSubmit={handleSearch} className="flex flex-col gap-2 sm:flex-row sm:gap-4">
          <input
            type="text"
            value={searchInput}
            onChange={(e) => setSearchInput(e.target.value)}
            placeholder="모임 이름으로 검색"
            className="flex-1 px-4 py-2 border border-gray-200 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-transparent"
          />
          <div className="flex gap-2">
            <select
              value={statusFilter}
              onChange={(e) => {
                setStatusFilter(e.target.value);
                setPage(1);
              }}
              className="flex-1 sm:flex-none px-4 py-2 border border-gray-200 rounded-lg focus:ring-2 focus:ring-primary-500"
            >
              <option value="">전체 상태</option>
              <option value="ACTIVE">활성</option>
              <option value="INACTIVE">비활성</option>
              <option value="DELETED">삭제됨</option>
            </select>
            <button
              type="submit"
              className="px-6 py-2 bg-primary-500 text-white rounded-lg hover:bg-primary-600 whitespace-nowrap"
            >
              검색
            </button>
          </div>
        </form>
      </div>

      {/* API 에러 표시 */}
      {isError && (
        <div className="bg-yellow-50 border border-yellow-200 rounded-lg p-4 mb-6">
          <p className="text-yellow-700">
            ⚠️ 데이터를 불러오는 데 실패했습니다. 잠시 후 다시 시도해주세요.
          </p>
        </div>
      )}

      {/* 모임 목록 */}
      <div className="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden">
        {isLoading ? (
          <div className="flex items-center justify-center h-64">
            <Spinner />
          </div>
        ) : (
          <>
            <div className="overflow-x-auto">
            <table className="w-full min-w-200">
              <thead className="bg-gray-50 border-b border-gray-100">
                <tr>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">
                    모임명
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">
                    카테고리
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">
                    모임장
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">
                    인원
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">
                    상태
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">
                    생성일
                  </th>
                  <th className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase">
                    액션
                  </th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-100">
                {(data?.dtoList ?? []).map((group) => (
                  <tr key={group.id} className="hover:bg-gray-50">
                    <td className="px-6 py-4 whitespace-nowrap">
                      <Link
                        to={`/groups/${group.id}`}
                        className="inline-flex items-center gap-1 font-medium text-primary-600 hover:text-primary-700 hover:underline"
                      >
                        {group.name}
                        <ExternalLink size={14} className="text-gray-400" />
                      </Link>
                    </td>
                    <td className="px-6 py-4 text-sm text-gray-600 whitespace-nowrap">
                      {group.categoryName}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <p className="text-sm text-gray-800">{group.ownerNickname}</p>
                      <p className="text-xs text-gray-400">{group.ownerEmail}</p>
                    </td>
                    <td className="px-6 py-4 text-sm text-gray-600 whitespace-nowrap">
                      {group.memberCount} / {group.maxMembers}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <StatusBadge status={group.status} />
                    </td>
                    <td className="px-6 py-4 text-sm text-gray-600 whitespace-nowrap">
                      {new Date(group.createdAt).toLocaleDateString()}
                    </td>
                    <td className="px-6 py-4 text-right whitespace-nowrap">
                      <div className="flex justify-end gap-2">
                        {group.status !== "DELETED" && (
                          <>
                            <button
                              onClick={() => handleStatusChange(group.id, group.status)}
                              className={`px-3 py-1 text-xs rounded-lg transition-colors ${
                                group.status === "ACTIVE"
                                  ? "bg-gray-100 text-gray-600 hover:bg-gray-200"
                                  : "bg-green-100 text-green-700 hover:bg-green-200"
                              }`}
                              disabled={statusMutation.isPending}
                            >
                              {group.status === "ACTIVE" ? "비활성화" : "활성화"}
                            </button>
                            <button
                              onClick={() => handleDelete(group.id)}
                              className="px-3 py-1 text-xs bg-red-100 text-red-700 rounded-lg hover:bg-red-200 transition-colors"
                              disabled={statusMutation.isPending}
                            >
                              삭제
                            </button>
                          </>
                        )}
                      </div>
                    </td>
                  </tr>
                ))}
                {(!data?.dtoList || data.dtoList.length === 0) && (
                  <tr>
                    <td colSpan={7} className="px-6 py-12 text-center text-gray-400">
                      모임이 없습니다
                    </td>
                  </tr>
                )}
              </tbody>
            </table>
            </div>

            {/* 페이지네이션 */}
            {data && data.totalPage > 1 && (
              <div className="flex items-center justify-center gap-2 p-4 border-t border-gray-100">
                <button
                  onClick={() => setPage(page - 1)}
                  disabled={page <= 1}
                  className="px-3 py-1 text-sm bg-gray-100 rounded hover:bg-gray-200 disabled:opacity-50"
                >
                  이전
                </button>
                <span className="text-sm text-gray-600">
                  {data.current} / {data.totalPage}
                </span>
                <button
                  onClick={() => setPage(page + 1)}
                  disabled={page >= data.totalPage}
                  className="px-3 py-1 text-sm bg-gray-100 rounded hover:bg-gray-200 disabled:opacity-50"
                >
                  다음
                </button>
              </div>
            )}
          </>
        )}
      </div>
    </div>
  );
}

export default GroupsPage;
