import {
  useQuery,
  useInfiniteQuery,
  useMutation,
  useQueryClient,
} from "@tanstack/react-query";
import {
  getGroups,
  getGroup,
  createGroup,
  updateGroup,
  deleteGroup,
  joinGroup,
  leaveGroup,
  getMyGroups,
  getNearbyGroups,
  getRecommendedGroups,
  getGroupMembers,
} from "../api";
import type { GroupSearchParams, GroupUpdateRequest } from "../types";

/**
 * 모임 목록 조회 (무한 스크롤)
 */
export const useGroupsInfinite = (params: GroupSearchParams) => {
  return useInfiniteQuery({
    queryKey: ["groups", params],
    queryFn: ({ pageParam = 1 }) =>
      getGroups({ ...params, page: pageParam }),
    initialPageParam: 1,
    getNextPageParam: (lastPage) =>
      lastPage.next ? lastPage.current + 1 : undefined,
    staleTime: 5 * 60 * 1000,
  });
};

/**
 * 모임 목록 조회 (일반)
 */
export const useGroupsList = (params: GroupSearchParams) => {
  return useQuery({
    queryKey: ["groups", "list", params],
    queryFn: () => getGroups(params),
    staleTime: 5 * 60 * 1000,
  });
};

/**
 * 모임 상세 조회
 */
export const useGroupDetail = (groupId: number) => {
  return useQuery({
    queryKey: ["group", groupId],
    queryFn: () => getGroup(groupId),
    enabled: !!groupId,
    staleTime: 2 * 60 * 1000,
  });
};

/**
 * 내 모임 목록 조회
 */
export const useMyGroups = () => {
  return useQuery({
    queryKey: ["groups", "my"],
    queryFn: getMyGroups,
    staleTime: 2 * 60 * 1000,
  });
};

/**
 * 근처 모임 조회
 */
export const useNearbyGroups = (
  lat: number,
  lng: number,
  radius?: number
) => {
  return useQuery({
    queryKey: ["groups", "nearby", lat, lng, radius],
    queryFn: () => getNearbyGroups(lat, lng, radius),
    enabled: !!lat && !!lng,
    staleTime: 5 * 60 * 1000,
  });
};

/**
 * 추천 모임 조회
 */
export const useRecommendedGroups = () => {
  return useQuery({
    queryKey: ["groups", "recommended"],
    queryFn: getRecommendedGroups,
    staleTime: 5 * 60 * 1000,
  });
};

/**
 * 모임 멤버 목록 조회
 */
export const useGroupMembers = (groupId: number) => {
  return useQuery({
    queryKey: ["group", groupId, "members"],
    queryFn: () => getGroupMembers(groupId),
    enabled: !!groupId,
    staleTime: 2 * 60 * 1000,
  });
};

/**
 * 모임 생성
 */
export const useCreateGroup = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: createGroup,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["groups"] });
    },
  });
};

/**
 * 모임 수정
 */
export const useUpdateGroup = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({
      groupId,
      request,
    }: {
      groupId: number;
      request: GroupUpdateRequest;
    }) => updateGroup(groupId, request),
    onSuccess: (_, { groupId }) => {
      queryClient.invalidateQueries({ queryKey: ["group", groupId] });
      queryClient.invalidateQueries({ queryKey: ["groups"] });
    },
  });
};

/**
 * 모임 삭제
 */
export const useDeleteGroup = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: deleteGroup,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["groups"] });
    },
  });
};

/**
 * 모임 가입
 */
export const useJoinGroup = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: joinGroup,
    onSuccess: (_, groupId) => {
      queryClient.invalidateQueries({ queryKey: ["group", groupId] });
      queryClient.invalidateQueries({ queryKey: ["groups", "my"] });
    },
  });
};

/**
 * 모임 탈퇴
 */
export const useLeaveGroup = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: leaveGroup,
    onSuccess: (_, groupId) => {
      queryClient.invalidateQueries({ queryKey: ["group", groupId] });
      queryClient.invalidateQueries({ queryKey: ["groups", "my"] });
    },
  });
};
