import { useState } from "react";
import { useParams, useNavigate, Link } from "react-router";
import {
  useGroupDetail,
  useGroupMembers,
  useDeleteGroup,
  useUpdateMemberRole,
  useRemoveMember,
} from "@/features/groups";
import type { GroupMemberDTO, GroupRole } from "@/features/groups";
import { Avatar, EmptyState, Spinner, ResultModal } from "@/components/ui";
import { useNotificationStore, getUnreadCount } from "@/features/notifications";
import { MemberProfileModal } from "@/features/auth";

function GroupManagePage() {
  const { groupId } = useParams<{ groupId: string }>();
  const navigate = useNavigate();
  const [showModal, setShowModal] = useState(false);
  const [modalContent, setModalContent] = useState({ title: "", content: "" });
  const [selectedMember, setSelectedMember] = useState<GroupMemberDTO | null>(null);
  const [showRoleModal, setShowRoleModal] = useState(false);
  const [profileMemberId, setProfileMemberId] = useState<number | null>(null);

  const { data: group, isLoading } = useGroupDetail(Number(groupId));
  const { data: members } = useGroupMembers(Number(groupId));
  const deleteMutation = useDeleteGroup();
  const updateRoleMutation = useUpdateMemberRole();
  const removeMutation = useRemoveMember();
  const { setUnreadCount } = useNotificationStore();

  if (isLoading) {
    return (
      <div className="flex items-center justify-center h-64">
        <Spinner size="lg" />
      </div>
    );
  }

  if (!group) {
    return (
      <div className="p-4">
        <EmptyState
          icon="❌"
          title="모임을 찾을 수 없습니다"
          action={{ label: "돌아가기", onClick: () => navigate(-1) }}
        />
      </div>
    );
  }

  const handleDelete = async () => {
    if (!confirm("정말 모임을 삭제하시겠습니까? 이 작업은 되돌릴 수 없습니다.")) {
      return;
    }

    try {
      await deleteMutation.mutateAsync(group.id);
      setModalContent({ title: "삭제 완료", content: "모임이 삭제되었습니다." });
      setShowModal(true);
      setTimeout(() => navigate("/"), 1500);
    } catch {
      setModalContent({ title: "삭제 실패", content: "모임 삭제에 실패했습니다." });
      setShowModal(true);
    }
  };

  const handleRemoveMember = async (memberId: number, nickname: string) => {
    if (!confirm(`정말 ${nickname}님을 강퇴하시겠습니까?`)) {
      return;
    }

    try {
      await removeMutation.mutateAsync({ groupId: group.id, memberId });
      setModalContent({ title: "강퇴 완료", content: `${nickname}님이 강퇴되었습니다.` });
      setShowModal(true);
    } catch {
      setModalContent({ title: "강퇴 실패", content: "멤버 강퇴에 실패했습니다." });
      setShowModal(true);
    }
  };

  const handleRoleChange = async (newRole: GroupRole) => {
    if (!selectedMember) return;

    try {
      await updateRoleMutation.mutateAsync({
        groupId: group.id,
        memberId: selectedMember.member.id,
        request: { role: newRole },
      });
      setShowRoleModal(false);
      setSelectedMember(null);
      setModalContent({
        title: "역할 변경 완료",
        content: `${selectedMember.member.nickname}님의 역할이 변경되었습니다.`,
      });
      setShowModal(true);

      // 역할 변경 시 알림이 발송되므로 알림 수 즉시 새로고침
      const count = await getUnreadCount();
      setUnreadCount(count);
    } catch {
      setModalContent({ title: "변경 실패", content: "역할 변경에 실패했습니다." });
      setShowModal(true);
    }
  };

  // 모든 멤버가 가입 시 즉시 승인됨
  const approvedMembers = members || [];

  const getRoleLabel = (role: GroupRole) => {
    switch (role) {
      case "OWNER":
        return "모임장";
      case "MANAGER":
        return "운영진";
      default:
        return "멤버";
    }
  };

  return (
    <div className="p-4 pb-20">
      <h1 className="text-xl font-bold text-gray-900 mb-6">모임 관리</h1>

      {/* Edit Group Button */}
      <div className="mb-6">
        <Link
          to={`/groups/${group.id}/edit`}
          className="block w-full py-3 text-center border border-primary-500 text-primary-600 rounded-lg font-medium hover:bg-primary-50 transition-colors"
        >
          모임 정보 수정
        </Link>
      </div>

      {/* Members */}
      <div className="mb-6">
        <h2 className="text-lg font-bold text-gray-900 mb-3">
          멤버 ({approvedMembers.length})
        </h2>
        {approvedMembers.length > 0 ? (
          <div className="space-y-3">
            {approvedMembers.map((member) => (
              <div
                key={member.id}
                className="flex items-center justify-between p-3 bg-white rounded-lg border border-gray-200"
              >
                <div className="flex items-center gap-3">
                  <button
                    type="button"
                    onClick={() => setProfileMemberId(member.member.id)}
                  >
                    <Avatar
                      src={member.member.profileImage}
                      alt={member.member.nickname}
                      size="md"
                    />
                  </button>
                  <div>
                    <button
                      type="button"
                      onClick={() => setProfileMemberId(member.member.id)}
                      className="font-medium text-gray-900 hover:text-primary-600 hover:underline"
                    >
                      {member.member.nickname}
                    </button>
                    <button
                      onClick={() => {
                        if (member.role !== "OWNER" && group.myRole === "OWNER") {
                          setSelectedMember(member);
                          setShowRoleModal(true);
                        }
                      }}
                      className={`block text-xs ${
                        member.role !== "OWNER" && group.myRole === "OWNER"
                          ? "text-primary-600 hover:underline cursor-pointer"
                          : "text-gray-500"
                      }`}
                    >
                      {getRoleLabel(member.role)}
                      {member.role !== "OWNER" && group.myRole === "OWNER" && " (변경)"}
                    </button>
                  </div>
                </div>
                {member.role !== "OWNER" && (
                  <button
                    onClick={() => handleRemoveMember(member.id, member.member.nickname)}
                    disabled={removeMutation.isPending}
                    className="text-sm text-red-500 hover:text-red-700 disabled:text-gray-400"
                  >
                    강퇴
                  </button>
                )}
              </div>
            ))}
          </div>
        ) : (
          <p className="text-gray-500 text-center py-4">멤버가 없습니다.</p>
        )}
      </div>

      {/* Danger Zone */}
      {group.myRole === "OWNER" && (
        <div className="mt-8 p-4 border border-red-200 rounded-lg bg-red-50">
          <h2 className="text-lg font-bold text-red-600 mb-2">위험 구역</h2>
          <p className="text-sm text-red-500 mb-4">
            모임을 삭제하면 모든 데이터가 영구적으로 삭제됩니다.
          </p>
          <button
            onClick={handleDelete}
            disabled={deleteMutation.isPending}
            className="px-4 py-2 bg-red-500 text-white rounded-lg font-medium disabled:bg-gray-300"
          >
            모임 삭제
          </button>
        </div>
      )}

      {/* Role Change Modal */}
      {showRoleModal && selectedMember && (
        <div className="fixed inset-0 z-50 flex items-center justify-center">
          <div
            className="absolute inset-0 bg-black/50"
            onClick={() => {
              setShowRoleModal(false);
              setSelectedMember(null);
            }}
          />
          <div className="relative bg-white rounded-xl w-full max-w-sm mx-4 p-6">
            <h3 className="text-lg font-bold text-gray-900 mb-4">
              {selectedMember.member.nickname}님의 역할 변경
            </h3>
            <div className="space-y-2">
              <button
                onClick={() => handleRoleChange("MANAGER")}
                disabled={updateRoleMutation.isPending}
                className={`w-full py-3 rounded-lg font-medium transition-colors ${
                  selectedMember.role === "MANAGER"
                    ? "bg-primary-100 text-primary-700 border-2 border-primary-500"
                    : "bg-gray-100 text-gray-700 hover:bg-gray-200"
                }`}
              >
                운영진
              </button>
              <button
                onClick={() => handleRoleChange("MEMBER")}
                disabled={updateRoleMutation.isPending}
                className={`w-full py-3 rounded-lg font-medium transition-colors ${
                  selectedMember.role === "MEMBER"
                    ? "bg-primary-100 text-primary-700 border-2 border-primary-500"
                    : "bg-gray-100 text-gray-700 hover:bg-gray-200"
                }`}
              >
                일반 멤버
              </button>
            </div>
            <button
              onClick={() => {
                setShowRoleModal(false);
                setSelectedMember(null);
              }}
              className="w-full mt-4 py-2 text-gray-500"
            >
              취소
            </button>
          </div>
        </div>
      )}

      {showModal && (
        <ResultModal
          title={modalContent.title}
          content={modalContent.content}
          callbackFn={() => setShowModal(false)}
        />
      )}

      {/* Member Profile Modal */}
      {profileMemberId && (
        <MemberProfileModal
          memberId={profileMemberId}
          onClose={() => setProfileMemberId(null)}
        />
      )}
    </div>
  );
}

export default GroupManagePage;
