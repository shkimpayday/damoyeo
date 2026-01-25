import { useMutation, useQueryClient } from "@tanstack/react-query";
import { updateMemberRole, removeMember } from "../api";
import type { MemberRoleUpdateRequest } from "../types";
import { jwtAxios } from "@/lib/axios";
import { ENV } from "@/config";

const prefix = `${ENV.API_URL}/api/groups`;

/**
 * 가입 승인
 */
export const approveMember = async (
  groupId: number,
  memberId: number
): Promise<{ message: string }> => {
  const res = await jwtAxios.post(
    `${prefix}/${groupId}/members/${memberId}/approve`
  );
  return res.data;
};

/**
 * 가입 거절
 */
export const rejectMember = async (
  groupId: number,
  memberId: number
): Promise<{ message: string }> => {
  const res = await jwtAxios.post(
    `${prefix}/${groupId}/members/${memberId}/reject`
  );
  return res.data;
};

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

/**
 * 가입 승인 Hook
 */
export const useApproveMember = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({ groupId, memberId }: { groupId: number; memberId: number }) =>
      approveMember(groupId, memberId),
    onSuccess: (_, { groupId }) => {
      queryClient.invalidateQueries({ queryKey: ["group", groupId, "members"] });
      queryClient.invalidateQueries({ queryKey: ["group", groupId] });
    },
  });
};

/**
 * 가입 거절 Hook
 */
export const useRejectMember = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({ groupId, memberId }: { groupId: number; memberId: number }) =>
      rejectMember(groupId, memberId),
    onSuccess: (_, { groupId }) => {
      queryClient.invalidateQueries({ queryKey: ["group", groupId, "members"] });
    },
  });
};
