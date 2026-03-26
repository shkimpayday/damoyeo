import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import {
  getMeetingsByGroup,
  getUpcomingMeetingsByGroup,
  getPastMeetingsByGroup,
  getMeeting,
  createMeeting,
  updateMeeting,
  cancelMeeting,
  attendMeeting,
  cancelAttend,
  getAttendees,
  getUpcomingMeetings,
  getMyMeetings,
} from "../api";
import type { MeetingCreateRequest, MeetingUpdateRequest, AttendRequest } from "../types";

/**
 * 모임의 정모 목록 조회 (전체)
 */
export const useMeetingsByGroup = (groupId: number) => {
  return useQuery({
    queryKey: ["meetings", "group", groupId],
    queryFn: () => getMeetingsByGroup(groupId),
    enabled: !!groupId,
    staleTime: 2 * 60 * 1000,
  });
};

/**
 * 모임의 예정된 정모 목록 조회
 * (SCHEDULED, ONGOING 상태)
 */
export const useUpcomingMeetingsByGroup = (groupId: number) => {
  return useQuery({
    queryKey: ["meetings", "group", groupId, "upcoming"],
    queryFn: () => getUpcomingMeetingsByGroup(groupId),
    enabled: !!groupId,
    staleTime: 2 * 60 * 1000,
  });
};

/**
 * 모임의 지난 정모 목록 조회
 * (COMPLETED 상태)
 */
export const usePastMeetingsByGroup = (groupId: number) => {
  return useQuery({
    queryKey: ["meetings", "group", groupId, "past"],
    queryFn: () => getPastMeetingsByGroup(groupId),
    enabled: !!groupId,
    staleTime: 2 * 60 * 1000,
  });
};

/**
 * 정모 상세 조회
 */
export const useMeetingDetail = (meetingId: number) => {
  return useQuery({
    queryKey: ["meeting", meetingId],
    queryFn: () => getMeeting(meetingId),
    enabled: !!meetingId,
    staleTime: 2 * 60 * 1000,
  });
};

/**
 * 다가오는 정모 목록
 */
export const useUpcomingMeetings = () => {
  return useQuery({
    queryKey: ["meetings", "upcoming"],
    queryFn: getUpcomingMeetings,
    staleTime: 2 * 60 * 1000,
  });
};

/**
 * 내가 참석할 정모 목록
 */
export const useMyMeetings = () => {
  return useQuery({
    queryKey: ["meetings", "my"],
    queryFn: getMyMeetings,
    staleTime: 2 * 60 * 1000,
  });
};

/**
 * 참석자 목록 조회
 */
export const useMeetingAttendees = (meetingId: number) => {
  return useQuery({
    queryKey: ["meeting", meetingId, "attendees"],
    queryFn: () => getAttendees(meetingId),
    enabled: !!meetingId,
    staleTime: 1 * 60 * 1000,
  });
};

/**
 * 정모 생성
 */
export const useCreateMeeting = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({
      groupId,
      request,
    }: {
      groupId: number;
      request: MeetingCreateRequest;
    }) => createMeeting({ ...request, groupId }),
    onSuccess: (_, { groupId }) => {
      queryClient.invalidateQueries({
        queryKey: ["meetings", "group", groupId],
      });
      queryClient.invalidateQueries({ queryKey: ["meetings", "upcoming"] });
    },
  });
};

/**
 * 정모 수정
 */
export const useUpdateMeeting = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({
      meetingId,
      request,
    }: {
      meetingId: number;
      request: MeetingUpdateRequest;
    }) => updateMeeting(meetingId, request),
    onSuccess: (_, { meetingId }) => {
      queryClient.invalidateQueries({ queryKey: ["meeting", meetingId] });
      queryClient.invalidateQueries({ queryKey: ["meetings"] });
    },
  });
};

/**
 * 정모 취소
 */
export const useCancelMeeting = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: cancelMeeting,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["meetings"] });
    },
  });
};

/**
 * 참석 등록
 */
export const useAttendMeeting = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({
      meetingId,
      request,
    }: {
      meetingId: number;
      request: AttendRequest;
    }) => attendMeeting(meetingId, request),
    onSuccess: (_, { meetingId }) => {
      queryClient.invalidateQueries({ queryKey: ["meeting", meetingId] });
      queryClient.invalidateQueries({ queryKey: ["meetings", "my"] });
    },
  });
};

/**
 * 참석 취소
 */
export const useCancelAttend = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: cancelAttend,
    onSuccess: (_, meetingId) => {
      queryClient.invalidateQueries({ queryKey: ["meeting", meetingId] });
      queryClient.invalidateQueries({ queryKey: ["meetings", "my"] });
    },
  });
};
