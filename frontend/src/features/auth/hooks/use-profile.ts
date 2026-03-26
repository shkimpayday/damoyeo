import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import {
  getProfile,
  getPublicProfile,
  updateProfile,
  uploadProfileImage,
  updateLocation,
} from "../api";
import type { ProfileUpdateRequest, LocationUpdateRequest } from "../types";

/**
 * 프로필 조회 (내 프로필)
 */
export const useProfile = () => {
  return useQuery({
    queryKey: ["profile"],
    queryFn: getProfile,
    staleTime: 5 * 60 * 1000,
  });
};

/**
 * 공개 프로필 조회 (다른 회원)
 *
 * [캐시 정책]
 * - staleTime: 0 (항상 최신 데이터 요청)
 * - gcTime: 1분 (가비지 컬렉션까지 1분)
 *
 * 프라이버시 설정(showJoinedGroups) 변경이 다른 사용자에게
 * 즉시 반영되도록 캐시를 최소화합니다.
 */
export const usePublicProfile = (memberId: number | undefined) => {
  return useQuery({
    queryKey: ["member", "profile", memberId],
    queryFn: () => getPublicProfile(memberId!),
    enabled: !!memberId,
    staleTime: 0,  // 항상 최신 데이터 요청
    gcTime: 60 * 1000,  // 1분 후 캐시 삭제
  });
};

/**
 * 프로필 수정
 *
 * [캐시 무효화]
 * - ["profile"]: 내 프로필 조회 캐시
 * - ["member", "profile"]: 공개 프로필 조회 캐시 (showJoinedGroups 설정 반영)
 */
export const useUpdateProfile = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (request: ProfileUpdateRequest) => updateProfile(request),
    onSuccess: () => {
      // 내 프로필 캐시 무효화
      queryClient.invalidateQueries({ queryKey: ["profile"] });
      // 공개 프로필 캐시도 무효화 (활동 모임 공개 설정 등 반영)
      queryClient.invalidateQueries({ queryKey: ["member", "profile"] });
    },
  });
};

/**
 * 프로필 이미지 업로드
 */
export const useUploadProfileImage = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (file: File) => uploadProfileImage(file),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["profile"] });
    },
  });
};

/**
 * 위치 업데이트
 */
export const useUpdateLocation = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (request: LocationUpdateRequest) => updateLocation(request),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["profile"] });
    },
  });
};
