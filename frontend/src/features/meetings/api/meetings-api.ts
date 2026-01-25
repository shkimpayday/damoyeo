import { jwtAxios } from "@/lib/axios";
import { ENV } from "@/config";
import type {
  MeetingDTO,
  MeetingListDTO,
  MeetingCreateRequest,
  MeetingUpdateRequest,
  MeetingAttendeeDTO,
  AttendRequest,
} from "../types";

/**
 * 모임의 정모 목록 조회
 */
export const getMeetingsByGroup = async (
  groupId: number
): Promise<MeetingListDTO[]> => {
  const res = await jwtAxios.get(
    `${ENV.API_URL}/api/groups/${groupId}/meetings`
  );
  return res.data;
};

/**
 * 정모 생성
 */
export const createMeeting = async (
  groupId: number,
  request: MeetingCreateRequest
): Promise<MeetingDTO> => {
  const res = await jwtAxios.post(
    `${ENV.API_URL}/api/groups/${groupId}/meetings`,
    request
  );
  return res.data;
};

/**
 * 정모 상세 조회
 */
export const getMeeting = async (meetingId: number): Promise<MeetingDTO> => {
  const res = await jwtAxios.get(`${ENV.API_URL}/api/meetings/${meetingId}`);
  return res.data;
};

/**
 * 정모 수정
 */
export const updateMeeting = async (
  meetingId: number,
  request: MeetingUpdateRequest
): Promise<MeetingDTO> => {
  const res = await jwtAxios.put(
    `${ENV.API_URL}/api/meetings/${meetingId}`,
    request
  );
  return res.data;
};

/**
 * 정모 취소
 */
export const cancelMeeting = async (
  meetingId: number
): Promise<{ message: string }> => {
  const res = await jwtAxios.delete(`${ENV.API_URL}/api/meetings/${meetingId}`);
  return res.data;
};

/**
 * 참석 등록
 */
export const attendMeeting = async (
  meetingId: number,
  request: AttendRequest
): Promise<{ message: string }> => {
  const res = await jwtAxios.post(
    `${ENV.API_URL}/api/meetings/${meetingId}/attend`,
    request
  );
  return res.data;
};

/**
 * 참석 취소
 */
export const cancelAttend = async (
  meetingId: number
): Promise<{ message: string }> => {
  const res = await jwtAxios.delete(
    `${ENV.API_URL}/api/meetings/${meetingId}/attend`
  );
  return res.data;
};

/**
 * 참석자 목록 조회
 */
export const getAttendees = async (
  meetingId: number
): Promise<MeetingAttendeeDTO[]> => {
  const res = await jwtAxios.get(
    `${ENV.API_URL}/api/meetings/${meetingId}/attendees`
  );
  return res.data;
};

/**
 * 다가오는 정모 목록 조회
 */
export const getUpcomingMeetings = async (): Promise<MeetingListDTO[]> => {
  const res = await jwtAxios.get(`${ENV.API_URL}/api/meetings/upcoming`);
  return res.data;
};

/**
 * 내가 참석할 정모 목록 조회
 */
export const getMyMeetings = async (): Promise<MeetingListDTO[]> => {
  const res = await jwtAxios.get(`${ENV.API_URL}/api/meetings/my`);
  return res.data;
};
