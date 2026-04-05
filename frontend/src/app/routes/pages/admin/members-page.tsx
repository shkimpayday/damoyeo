import { useState } from "react";
import { Link } from "react-router";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { jwtAxios } from "@/lib/axios";
import { ENV } from "@/config";
import { Spinner } from "@/components/ui";
import { Avatar } from "@/components/ui/avatar";
import { Crown, Minus, X, Gift, ExternalLink } from "lucide-react";

/**
 * 회원 관리 페이지
 *
 * [기능]
 * - 회원 목록 조회 (페이지네이션)
 * - 회원 검색 (이메일, 닉네임)
 * - 역할 변경 (USER ↔ ADMIN, PREMIUM 부여)
 * - 프리미엄 관리 (부여, 기간 조정, 취소)
 * - 회원 상세 정보 확인
 */

interface AdminMemberDTO {
  id: number;
  email: string;
  nickname: string;
  profileImage: string | null;
  roleNames: string[];
  social: boolean;
  createdAt: string;
  groupCount: number;
  // 프리미엄 정보
  isPremium: boolean;
  premiumType: string | null;
  premiumStartDate: string | null;
  premiumEndDate: string | null;
  premiumDaysRemaining: number;
}

interface PageResponseDTO<T> {
  dtoList: T[];
  totalCount: number;
  totalPage: number;
  current: number;
  prev: boolean;
  next: boolean;
}

// ============================================================================
// API 함수
// ============================================================================

/** 회원 목록 조회 */
const fetchMembers = async (page: number, keyword: string): Promise<PageResponseDTO<AdminMemberDTO>> => {
  const params = { page, size: 10, keyword: keyword || undefined };
  const res = await jwtAxios.get(`${ENV.API_URL}/api/admin/members`, { params });
  return res.data;
};

/** 회원 역할 변경 */
const updateMemberRole = async ({ memberId, role }: { memberId: number; role: string }) => {
  const res = await jwtAxios.patch(`${ENV.API_URL}/api/admin/members/${memberId}/role`, { role });
  return res.data;
};

/** 프리미엄 부여 */
const grantPremium = async ({ memberId, days }: {
  memberId: number;
  days: number;
}) => {
  const res = await jwtAxios.post(`${ENV.API_URL}/api/admin/members/${memberId}/premium`, {
    days
  });
  return res.data;
};

/** 프리미엄 기간 조정 */
const adjustPremiumDays = async ({ memberId, days }: { memberId: number; days: number }) => {
  const res = await jwtAxios.patch(`${ENV.API_URL}/api/admin/members/${memberId}/premium/adjust`, { days });
  return res.data;
};

/** 프리미엄 취소 */
const revokePremium = async (memberId: number) => {
  const res = await jwtAxios.delete(`${ENV.API_URL}/api/admin/members/${memberId}/premium`);
  return res.data;
};

// ============================================================================
// 컴포넌트
// ============================================================================

/** 역할 뱃지 컴포넌트 */
function RoleBadge({ role }: { role: string }) {
  const config: Record<string, { bg: string; text: string; label: string }> = {
    ADMIN: { bg: "bg-red-100", text: "text-red-700", label: "관리자" },
    PREMIUM: { bg: "bg-yellow-100", text: "text-yellow-700", label: "프리미엄" },
    USER: { bg: "bg-gray-100", text: "text-gray-600", label: "일반" },
  };
  const c = config[role] || config.USER;
  return (
    <span className={`px-2 py-0.5 text-xs rounded-full ${c.bg} ${c.text}`}>
      {c.label}
    </span>
  );
}

/** 프리미엄 상태 뱃지 */
function PremiumStatusBadge({ member }: { member: AdminMemberDTO }) {
  // role에 PREMIUM이 있는지 확인 (레거시 데이터 지원)
  const hasPremiumRole = member.roleNames.includes("PREMIUM");

  // Payment 기록이 있는 실제 프리미엄인지, role만 있는 레거시인지 구분
  const hasActivePremium = member.isPremium && member.premiumDaysRemaining > 0;

  // 프리미엄 role도 없고 활성 프리미엄도 없으면 일반 회원
  if (!hasPremiumRole && !hasActivePremium) {
    return (
      <span className="text-xs text-gray-400">일반 회원</span>
    );
  }

  // role만 있고 Payment 기록이 없는 경우 (레거시)
  if (hasPremiumRole && !hasActivePremium) {
    return (
      <div className="flex flex-col gap-1">
        <div className="flex items-center gap-1 px-2 py-0.5 bg-purple-100 rounded-full w-fit">
          <Crown size={12} className="text-purple-600" />
          <span className="text-xs font-medium text-purple-700">프리미엄</span>
        </div>
        <span className="text-xs text-gray-400">
          (기간 정보 없음)
        </span>
      </div>
    );
  }

  // 남은 일수에 따른 색상 (7일 이하면 빨간색, 30일 이하면 주황색)
  const daysColor =
    member.premiumDaysRemaining <= 7
      ? "text-red-600 bg-red-50"
      : member.premiumDaysRemaining <= 30
        ? "text-orange-600 bg-orange-50"
        : "text-yellow-700 bg-yellow-50";

  return (
    <div className="flex flex-col gap-1">
      {/* 프리미엄 뱃지 + 남은 일수 */}
      <div className="flex items-center gap-2">
        <div className="flex items-center gap-1 px-2 py-0.5 bg-gradient-to-r from-yellow-100 to-orange-100 rounded-full">
          <Crown size={12} className="text-yellow-600" />
          <span className="text-xs font-medium text-yellow-700">프리미엄</span>
        </div>
        <span className={`text-xs font-bold px-2 py-0.5 rounded ${daysColor}`}>
          {member.premiumDaysRemaining}일 남음
        </span>
      </div>
      {/* 만료일 */}
      {member.premiumEndDate && (
        <span className="text-xs text-gray-400">
          만료: {new Date(member.premiumEndDate).toLocaleDateString()}
        </span>
      )}
    </div>
  );
}

/** 프리미엄 관리 모달 */
function PremiumManageModal({
  member,
  onClose,
  onGrant,
  onAdjust,
  onRevoke,
  isPending
}: {
  member: AdminMemberDTO;
  onClose: () => void;
  onGrant: (days: number) => void;
  onAdjust: (days: number) => void;
  onRevoke: () => void;
  isPending: boolean;
}) {
  const [grantDays, setGrantDays] = useState(30);
  const [adjustDays, setAdjustDays] = useState(7);

  // 빠른 선택 버튼 옵션
  const quickOptions = [7, 30, 90, 365];

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50">
      <div className="bg-white rounded-2xl shadow-xl w-full max-w-md mx-4 overflow-hidden">
        {/* 헤더 */}
        <div className="flex items-center justify-between px-6 py-4 border-b bg-gradient-to-r from-yellow-50 to-orange-50">
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 rounded-full bg-gradient-to-br from-yellow-400 to-orange-500 flex items-center justify-center">
              <Crown size={20} className="text-white" />
            </div>
            <div>
              <h3 className="font-semibold text-gray-800">프리미엄 관리</h3>
              <p className="text-sm text-gray-500">{member.nickname}</p>
            </div>
          </div>
          <button
            onClick={onClose}
            className="p-2 hover:bg-gray-100 rounded-full transition-colors"
          >
            <X size={20} className="text-gray-500" />
          </button>
        </div>

        {/* 현재 상태 */}
        <div className="px-6 py-4 border-b bg-gray-50">
          <p className="text-xs text-gray-500 mb-2">현재 상태</p>
          {member.isPremium ? (
            <div className="flex items-center justify-between">
              <div className="flex items-center gap-2">
                <Crown size={16} className="text-yellow-500" />
                <span className="font-medium text-yellow-700">프리미엄 회원</span>
              </div>
              <div className="text-right">
                <p className="text-sm font-medium text-gray-700">
                  남은 기간: <span className="text-yellow-600">{member.premiumDaysRemaining}일</span>
                </p>
                {member.premiumEndDate && (
                  <p className="text-xs text-gray-500">
                    만료일: {new Date(member.premiumEndDate).toLocaleDateString()}
                  </p>
                )}
              </div>
            </div>
          ) : (
            <p className="text-gray-500">일반 회원 (프리미엄 없음)</p>
          )}
        </div>

        {/* 액션 */}
        <div className="px-6 py-4 space-y-4">
          {/* 프리미엄 부여/연장 */}
          <div className="p-4 border rounded-xl bg-yellow-50 border-yellow-200">
            <div className="flex items-center gap-2 mb-2">
              <Gift size={16} className="text-yellow-600" />
              <span className="font-medium text-yellow-800">
                {member.isPremium ? "프리미엄 기간 연장" : "프리미엄 부여"}
              </span>
            </div>

            {/* 안내 메시지 */}
            <p className="text-xs text-yellow-700 mb-3 leading-relaxed">
              {member.isPremium
                ? `현재 남은 ${member.premiumDaysRemaining}일에 입력한 일수가 추가됩니다.`
                : "오늘부터 입력한 일수만큼 프리미엄이 적용됩니다."}
            </p>

            {/* 빠른 선택 버튼 */}
            <div className="flex gap-2 mb-3">
              {quickOptions.map((days) => (
                <button
                  key={days}
                  onClick={() => setGrantDays(days)}
                  className={`flex-1 py-1.5 text-xs rounded-lg transition-colors ${
                    grantDays === days
                      ? "bg-yellow-500 text-white"
                      : "bg-white border border-yellow-300 text-yellow-700 hover:bg-yellow-100"
                  }`}
                >
                  {days >= 365 ? "1년" : days >= 30 ? `${days / 30}개월` : `${days}일`}
                </button>
              ))}
            </div>

            {/* 직접 입력 */}
            <div className="flex gap-2">
              <div className="flex-1 flex items-center gap-2">
                <input
                  type="number"
                  value={grantDays}
                  onChange={(e) => setGrantDays(Math.max(1, Number(e.target.value)))}
                  min={1}
                  max={730}
                  className="w-full px-3 py-2 border border-yellow-300 rounded-lg text-sm text-center bg-white"
                />
                <span className="text-sm text-gray-600 whitespace-nowrap">일</span>
              </div>
              <button
                onClick={() => onGrant(grantDays)}
                disabled={isPending}
                className="px-4 py-2 bg-gradient-to-r from-yellow-400 to-orange-500 text-white font-medium rounded-lg hover:from-yellow-500 hover:to-orange-600 disabled:opacity-50 whitespace-nowrap"
              >
                {member.isPremium ? "기간 연장" : "부여하기"}
              </button>
            </div>
          </div>

          {/* 기간 감소 (프리미엄 회원인 경우만) */}
          {member.isPremium && (
            <div className="p-4 border rounded-xl">
              <div className="flex items-center gap-2 mb-2">
                <Minus size={16} className="text-gray-600" />
                <span className="font-medium text-gray-800">기간 감소</span>
              </div>
              <p className="text-xs text-gray-500 mb-3">
                현재 남은 기간에서 입력한 일수만큼 차감됩니다.
                {member.premiumDaysRemaining <= 7 && (
                  <span className="text-red-500 ml-1">
                    (남은 기간이 적습니다)
                  </span>
                )}
              </p>
              <div className="flex items-center gap-2">
                <input
                  type="number"
                  value={adjustDays}
                  onChange={(e) => setAdjustDays(Math.max(1, Number(e.target.value)))}
                  min={1}
                  max={member.premiumDaysRemaining}
                  className="flex-1 px-3 py-2 border rounded-lg text-center"
                />
                <span className="text-sm text-gray-600">일</span>
                <button
                  onClick={() => onAdjust(-adjustDays)}
                  disabled={isPending || adjustDays > member.premiumDaysRemaining}
                  className="flex items-center gap-1 px-4 py-2 bg-gray-100 text-gray-700 rounded-lg hover:bg-gray-200 disabled:opacity-50"
                >
                  <Minus size={16} />
                  감소
                </button>
              </div>
            </div>
          )}

          {/* 프리미엄 즉시 취소 (프리미엄 회원인 경우만) */}
          {member.isPremium && (
            <div className="p-4 border border-red-200 rounded-xl bg-red-50">
              <div className="flex items-center justify-between">
                <div>
                  <p className="font-medium text-red-800">프리미엄 즉시 취소</p>
                  <p className="text-xs text-red-600 mt-0.5">
                    남은 {member.premiumDaysRemaining}일이 모두 사라지고 즉시 일반 회원이 됩니다.
                  </p>
                </div>
                <button
                  onClick={onRevoke}
                  disabled={isPending}
                  className="px-4 py-2 bg-red-500 text-white rounded-lg hover:bg-red-600 disabled:opacity-50 whitespace-nowrap"
                >
                  취소하기
                </button>
              </div>
            </div>
          )}
        </div>

        {/* 푸터 */}
        <div className="px-6 py-4 border-t bg-gray-50">
          <button
            onClick={onClose}
            className="w-full py-2 text-gray-600 hover:text-gray-800"
          >
            닫기
          </button>
        </div>
      </div>
    </div>
  );
}

// ============================================================================
// 메인 페이지 컴포넌트
// ============================================================================

export function MembersPage() {
  const queryClient = useQueryClient();
  const [page, setPage] = useState(1);
  const [keyword, setKeyword] = useState("");
  const [searchInput, setSearchInput] = useState("");
  const [selectedMember, setSelectedMember] = useState<AdminMemberDTO | null>(null);

  // 회원 목록 조회
  const { data, isLoading, isError } = useQuery({
    queryKey: ["admin", "members", page, keyword],
    queryFn: () => fetchMembers(page, keyword),
    staleTime: 30 * 1000,
  });

  // 역할 변경 Mutation
  const roleMutation = useMutation({
    mutationFn: updateMemberRole,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["admin", "members"] });
    },
  });

  // 프리미엄 부여 Mutation
  const grantMutation = useMutation({
    mutationFn: grantPremium,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["admin", "members"] });
      setSelectedMember(null);
    },
  });

  // 프리미엄 기간 조정 Mutation
  const adjustMutation = useMutation({
    mutationFn: adjustPremiumDays,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["admin", "members"] });
      setSelectedMember(null);
    },
  });

  // 프리미엄 취소 Mutation
  const revokeMutation = useMutation({
    mutationFn: revokePremium,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["admin", "members"] });
      setSelectedMember(null);
    },
  });

  const isPremiumPending = grantMutation.isPending || adjustMutation.isPending || revokeMutation.isPending;

  // 검색 핸들러
  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault();
    setKeyword(searchInput);
    setPage(1);
  };

  // 역할 변경 핸들러
  const handleRoleChange = (memberId: number, currentRoles: string[], targetRole: string) => {
    const hasRole = currentRoles.includes(targetRole);
    const action = hasRole ? "제거" : "부여";

    if (confirm(`${targetRole} 권한을 ${action}하시겠습니까?`)) {
      roleMutation.mutate({ memberId, role: targetRole });
    }
  };

  // 프리미엄 부여 핸들러
  const handleGrantPremium = (days: number) => {
    if (!selectedMember) return;

    const message = selectedMember.isPremium
      ? `${selectedMember.nickname}님의 프리미엄 기간을 ${days}일 연장하시겠습니까?\n(현재 ${selectedMember.premiumDaysRemaining}일 → ${selectedMember.premiumDaysRemaining + days}일)`
      : `${selectedMember.nickname}님에게 ${days}일간 프리미엄을 부여하시겠습니까?`;

    if (confirm(message)) {
      grantMutation.mutate({ memberId: selectedMember.id, days });
    }
  };

  // 프리미엄 기간 감소 핸들러
  const handleAdjustPremium = (days: number) => {
    if (!selectedMember) return;

    const newDays = selectedMember.premiumDaysRemaining + days;
    const message = `${selectedMember.nickname}님의 프리미엄 기간을 ${Math.abs(days)}일 감소하시겠습니까?\n(현재 ${selectedMember.premiumDaysRemaining}일 → ${Math.max(0, newDays)}일)`;

    if (confirm(message)) {
      adjustMutation.mutate({ memberId: selectedMember.id, days });
    }
  };

  // 프리미엄 취소 핸들러
  const handleRevokePremium = () => {
    if (!selectedMember) return;

    const message = `${selectedMember.nickname}님의 프리미엄을 즉시 취소하시겠습니까?\n\n⚠️ 남은 ${selectedMember.premiumDaysRemaining}일이 모두 사라집니다.\n이 작업은 되돌릴 수 없습니다.`;

    if (confirm(message)) {
      revokeMutation.mutate(selectedMember.id);
    }
  };

  return (
    <div>
      {/* 페이지 헤더 */}
      <div className="mb-6">
        <h1 className="text-2xl font-bold text-gray-800">회원 관리</h1>
        <p className="text-gray-500 mt-1">회원 목록 조회 및 권한, 프리미엄을 관리합니다</p>
      </div>

      {/* 검색 */}
      <div className="bg-white rounded-xl shadow-sm p-4 mb-6 border border-gray-100">
        <form onSubmit={handleSearch} className="flex gap-4">
          <input
            type="text"
            value={searchInput}
            onChange={(e) => setSearchInput(e.target.value)}
            placeholder="이메일 또는 닉네임으로 검색"
            className="flex-1 px-4 py-2 border border-gray-200 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-transparent"
          />
          <button
            type="submit"
            className="px-6 py-2 bg-primary-500 text-white rounded-lg hover:bg-primary-600"
          >
            검색
          </button>
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

      {/* 회원 목록 */}
      <div className="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden">
        {isLoading ? (
          <div className="flex items-center justify-center h-64">
            <Spinner />
          </div>
        ) : (
          <>
            <div className="overflow-x-auto">
            <table className="w-full min-w-160">
              <thead className="bg-gray-50 border-b border-gray-100">
                <tr>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">
                    회원
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">
                    이메일
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">
                    권한
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">
                    프리미엄
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">
                    모임 수
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">
                    가입일
                  </th>
                  <th className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase">
                    액션
                  </th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-100">
                {(data?.dtoList ?? []).map((member) => (
                  <tr key={member.id} className="hover:bg-gray-50">
                    <td className="px-6 py-4">
                      <div className="flex items-center gap-3">
                        <Avatar
                          src={member.profileImage || undefined}
                          alt={member.nickname}
                          size="sm"
                        />
                        <div>
                          <Link
                            to={`/member/${member.id}`}
                            className="inline-flex items-center gap-1 font-medium text-primary-600 hover:text-primary-700 hover:underline"
                          >
                            {member.nickname}
                            <ExternalLink size={14} className="text-gray-400" />
                          </Link>
                          {member.social && (
                            <span className="text-xs text-gray-400 block">소셜 로그인</span>
                          )}
                        </div>
                      </div>
                    </td>
                    <td className="px-6 py-4 text-sm text-gray-600">
                      {member.email}
                    </td>
                    <td className="px-6 py-4">
                      <div className="flex gap-1">
                        {member.roleNames.map((role) => (
                          <RoleBadge key={role} role={role} />
                        ))}
                      </div>
                    </td>
                    <td className="px-6 py-4">
                      <PremiumStatusBadge member={member} />
                    </td>
                    <td className="px-6 py-4 text-sm text-gray-600">
                      {member.groupCount}개
                    </td>
                    <td className="px-6 py-4 text-sm text-gray-600">
                      {new Date(member.createdAt).toLocaleDateString()}
                    </td>
                    <td className="px-6 py-4 text-right">
                      <div className="flex justify-end gap-2">
                        {/* 프리미엄 관리 버튼 */}
                        <button
                          onClick={() => setSelectedMember(member)}
                          className={`flex items-center gap-1 px-3 py-1 text-xs rounded-lg transition-colors ${
                            member.isPremium
                              ? "bg-gradient-to-r from-yellow-100 to-orange-100 text-yellow-700 hover:from-yellow-200 hover:to-orange-200"
                              : "bg-gray-100 text-gray-600 hover:bg-gray-200"
                          }`}
                        >
                          <Crown size={12} />
                          {member.isPremium ? "프리미엄 관리" : "프리미엄 부여"}
                        </button>
                        {/* 관리자 권한 버튼 */}
                        <button
                          onClick={() => handleRoleChange(member.id, member.roleNames, "ADMIN")}
                          className={`px-3 py-1 text-xs rounded-lg transition-colors ${
                            member.roleNames.includes("ADMIN")
                              ? "bg-red-100 text-red-700 hover:bg-red-200"
                              : "bg-gray-100 text-gray-600 hover:bg-gray-200"
                          }`}
                          disabled={roleMutation.isPending}
                        >
                          {member.roleNames.includes("ADMIN") ? "관리자 해제" : "관리자 부여"}
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}
                {(!data?.dtoList || data.dtoList.length === 0) && (
                  <tr>
                    <td colSpan={7} className="px-6 py-12 text-center text-gray-400">
                      회원이 없습니다
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

      {/* 프리미엄 관리 모달 */}
      {selectedMember && (
        <PremiumManageModal
          member={selectedMember}
          onClose={() => setSelectedMember(null)}
          onGrant={handleGrantPremium}
          onAdjust={handleAdjustPremium}
          onRevoke={handleRevokePremium}
          isPending={isPremiumPending}
        />
      )}
    </div>
  );
}

export default MembersPage;
