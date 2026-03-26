import { useMutation, useQueryClient } from "@tanstack/react-query";
import { updateMemberRole, removeMember } from "../api";
import type { MemberRoleUpdateRequest } from "../types";

/**
 * 멤버 역할 변경 Hook
 */
export const useUpdateMemberRole = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({
      groupId,
      memberId,
      request,
    }: {
      groupId: number;
      memberId: number;
      request: MemberRoleUpdateRequest;
    }) => updateMemberRole(groupId, memberId, request),
    onSuccess: (_, { groupId }) => {
      queryClient.invalidateQueries({ queryKey: ["group", groupId, "members"] });
    },
  });
};

/**
 * 멤버 강퇴 Hook
 */
export const useRemoveMember = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({ groupId, memberId }: { groupId: number; memberId: number }) =>
      removeMember(groupId, memberId),
    onSuccess: (_, { groupId }) => {
      queryClient.invalidateQueries({ queryKey: ["group", groupId, "members"] });
      queryClient.invalidateQueries({ queryKey: ["group", groupId] });
    },
  });
};
