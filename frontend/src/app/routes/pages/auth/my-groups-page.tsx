import { useNavigate } from "react-router";
import { useMyGroups, GroupCard } from "@/features/groups";
import { EmptyState, Spinner } from "@/components/ui";
import { Plus } from "lucide-react";

/**
 * 내 모임 페이지
 *
 * @description
 * 사용자가 가입한 모임 목록을 컴팩트한 카드 형태로 표시합니다.
 * - 역할별 그룹핑 (모임장/운영진/멤버)
 * - 컴팩트 카드 레이아웃으로 한눈에 여러 모임 확인 가능
 */
function MyGroupsPage() {
  const { data: groups, isLoading, error } = useMyGroups();
  const navigate = useNavigate();

  if (isLoading) {
    return (
      <div className="flex items-center justify-center h-64">
        <Spinner size="lg" />
      </div>
    );
  }

  if (error) {
    return (
      <div className="p-4">
        <EmptyState
          icon="⚠️"
          title="오류가 발생했습니다"
          description="잠시 후 다시 시도해주세요."
        />
      </div>
    );
  }

  // 역할별 모임 분류
  const ownerGroups = groups?.filter((g) => g.myRole === "OWNER") || [];
  const managerGroups = groups?.filter((g) => g.myRole === "MANAGER") || [];
  const memberGroups = groups?.filter((g) => g.myRole === "MEMBER") || [];

  const hasGroups = groups && groups.length > 0;

  return (
    <div className="p-4 max-w-3xl mx-auto">
      {/* 헤더 */}
      <div className="flex items-center justify-between mb-6">
        <div>
          <h1 className="text-xl font-bold text-gray-900">내 모임</h1>
          {hasGroups && (
            <p className="text-sm text-gray-500 mt-1">
              총 {groups.length}개의 모임에 참여 중
            </p>
          )}
        </div>
        <button
          onClick={() => navigate("/groups/create")}
          className="flex items-center gap-1.5 px-3 py-2 bg-primary-500 text-white rounded-lg text-sm font-medium hover:bg-primary-600 transition-colors"
        >
          <Plus size={16} />
          모임 만들기
        </button>
      </div>

      {!hasGroups ? (
        <EmptyState
          icon="🎉"
          title="아직 가입한 모임이 없어요"
          description="새로운 모임을 찾아보세요!"
          action={{
            label: "모임 찾기",
            onClick: () => navigate("/groups/list"),
          }}
        />
      ) : (
        <div className="space-y-6">
          {/* 내가 만든 모임 */}
          {ownerGroups.length > 0 && (
            <section>
              <h2 className="text-sm font-semibold text-gray-700 mb-3 flex items-center gap-2">
                <span className="w-1.5 h-1.5 rounded-full bg-amber-500" />
                내가 만든 모임
                <span className="text-gray-400 font-normal">
                  {ownerGroups.length}
                </span>
              </h2>
              <div className="space-y-3">
                {ownerGroups.map((group) => (
                  <GroupCard key={group.id} group={group} variant="compact" />
                ))}
              </div>
            </section>
          )}

          {/* 운영 중인 모임 */}
          {managerGroups.length > 0 && (
            <section>
              <h2 className="text-sm font-semibold text-gray-700 mb-3 flex items-center gap-2">
                <span className="w-1.5 h-1.5 rounded-full bg-blue-500" />
                운영 중인 모임
                <span className="text-gray-400 font-normal">
                  {managerGroups.length}
                </span>
              </h2>
              <div className="space-y-3">
                {managerGroups.map((group) => (
                  <GroupCard key={group.id} group={group} variant="compact" />
                ))}
              </div>
            </section>
          )}

          {/* 참여 중인 모임 */}
          {memberGroups.length > 0 && (
            <section>
              <h2 className="text-sm font-semibold text-gray-700 mb-3 flex items-center gap-2">
                <span className="w-1.5 h-1.5 rounded-full bg-gray-400" />
                참여 중인 모임
                <span className="text-gray-400 font-normal">
                  {memberGroups.length}
                </span>
              </h2>
              <div className="space-y-3">
                {memberGroups.map((group) => (
                  <GroupCard key={group.id} group={group} variant="compact" />
                ))}
              </div>
            </section>
          )}
        </div>
      )}
    </div>
  );
}

export default MyGroupsPage;
