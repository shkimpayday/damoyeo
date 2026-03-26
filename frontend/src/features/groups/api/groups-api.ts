import { jwtAxios } from "@/lib/axios";
import { ENV } from "@/config";
import type {
  GroupDTO,
  GroupListDTO,
  GroupCreateRequest,
  GroupUpdateRequest,
  GroupSearchParams,
  GroupMemberDTO,
  MemberRoleUpdateRequest,
  PageResponseDTO,
} from "../types";

const prefix = `${ENV.API_URL}/api/groups`;

/**
 * 모임 목록 조회 (검색/필터)
 */
export const getGroups = async (
  params: GroupSearchParams
): Promise<PageResponseDTO<GroupListDTO>> => {
  const res = await jwtAxios.get(prefix, { params });
  return res.data;
};

/**
 * 모임 상세 조회
 */
export const getGroup = async (groupId: number): Promise<GroupDTO> => {
  const res = await jwtAxios.get(`${prefix}/${groupId}`);
  return res.data;
};

/**
 * 모임 생성
 */
export const createGroup = async (
  request: GroupCreateRequest
): Promise<GroupDTO> => {
  const formData = new FormData();
  formData.append("name", request.name);
  formData.append("description", request.description);
  formData.append("categoryId", String(request.categoryId));
  formData.append("location", request.address);
  formData.append("maxMembers", String(request.maxMembers));
  formData.append("isPublic", String(request.isPublic));

  if (request.lat != null) formData.append("latitude", String(request.lat));
  if (request.lng != null) formData.append("longitude", String(request.lng));
  if (request.coverImage) formData.append("coverImage", request.coverImage);

  const res = await jwtAxios.post(prefix, formData, {
    headers: { "Content-Type": "multipart/form-data" },
  });
  return res.data;
};

/**
 * 모임 수정
 */
export const updateGroup = async (
  groupId: number,
  request: GroupUpdateRequest
): Promise<GroupDTO> => {
  const formData = new FormData();

  if (request.name) formData.append("name", request.name);
  if (request.description !== undefined)
    formData.append("description", request.description);
  if (request.categoryId)
    formData.append("categoryId", String(request.categoryId));
  if (request.address) formData.append("location", request.address);
  if (request.maxMembers)
    formData.append("maxMembers", String(request.maxMembers));
  if (request.isPublic !== undefined)
    formData.append("isPublic", String(request.isPublic));
  if (request.lat != null) formData.append("latitude", String(request.lat));
  if (request.lng != null) formData.append("longitude", String(request.lng));
  if (request.coverImage) formData.append("coverImageFile", request.coverImage);

  const res = await jwtAxios.put(`${prefix}/${groupId}`, formData, {
    headers: { "Content-Type": "multipart/form-data" },
  });
  return res.data;
};

/**
 * 모임 삭제
 */
export const deleteGroup = async (
  groupId: number
): Promise<{ message: string }> => {
  const res = await jwtAxios.delete(`${prefix}/${groupId}`);
  return res.data;
};

/**
 * 모임 가입 (즉시 가입)
 */
export const joinGroup = async (
  groupId: number
): Promise<{ message: string }> => {
  const res = await jwtAxios.post(`${prefix}/${groupId}/join`);
  return res.data;
};

/**
 * 모임 탈퇴
 */
export const leaveGroup = async (
  groupId: number
): Promise<{ message: string }> => {
  const res = await jwtAxios.post(`${prefix}/${groupId}/leave`);
  return res.data;
};

/**
 * 모임 멤버 목록 조회
 */
export const getGroupMembers = async (
  groupId: number
): Promise<GroupMemberDTO[]> => {
  const res = await jwtAxios.get(`${prefix}/${groupId}/members`);
  return res.data;
};

/**
 * 멤버 역할 변경 (모임장용)
 */
export const updateMemberRole = async (
  groupId: number,
  memberId: number,
  request: MemberRoleUpdateRequest
): Promise<{ message: string }> => {
  const res = await jwtAxios.patch(
    `${prefix}/${groupId}/members/${memberId}/role`,
    null,
    { params: { role: request.role } }
  );
  return res.data;
};

/**
 * 멤버 강퇴 (모임장/운영진용)
 */
export const removeMember = async (
  groupId: number,
  memberId: number
): Promise<{ message: string }> => {
  const res = await jwtAxios.delete(
    `${prefix}/${groupId}/members/${memberId}`
  );
  return res.data;
};

/**
 * 내 모임 목록 조회
 */
export const getMyGroups = async (): Promise<GroupListDTO[]> => {
  const res = await jwtAxios.get(`${prefix}/my`);
  return res.data;
};

/**
 * 근처 모임 조회
 */
export const getNearbyGroups = async (
  lat: number,
  lng: number,
  radius: number = 10
): Promise<GroupListDTO[]> => {
  const res = await jwtAxios.get(`${prefix}/nearby`, {
    params: { lat, lng, radius },
  });
  return res.data;
};

/**
 * 추천 모임 조회
 */
export const getRecommendedGroups = async (): Promise<GroupListDTO[]> => {
  const res = await jwtAxios.get(`${prefix}/recommended`);
  return res.data;
};
