import { useNavigate } from "react-router";
import { useMyGroups, GroupCard } from "@/features/groups";
import { EmptyState, Spinner } from "@/components/ui";

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

  return (
    <div className="p-4">
      <h1 className="text-xl font-bold text-gray-900 mb-4">내 모임</h1>

      {!groups || groups.length === 0 ? (
        <EmptyState
          icon="🎉"
          title="아직 가입한 모임이 없어요"
          description="새로운 모임을 찾아보세요!"
          action={{
            label: "모임 찾기",
            onClick: () => navigate("/search"),
          }}
        />
      ) : (
        <div className="grid gap-4">
          {groups.map((group) => (
            <GroupCard key={group.id} group={group} />
          ))}
        </div>
      )}
    </div>
  );
}

export default MyGroupsPage;
