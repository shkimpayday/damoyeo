import axios from "axios";
import { jwtAxios } from "@/lib/axios";
import { ENV } from "@/config";
import type {
  MemberInfo,
  ProfileInfo,
  PublicProfile,
  SignupRequest,
  ProfileUpdateRequest,
  LocationUpdateRequest,
} from "../types";

const prefix = `${ENV.API_URL}/api/member`;

/**
 * 로그인
 */
export const loginPost = async (
  email: string,
  pw: string
): Promise<MemberInfo> => {
  // URLSearchParams를 사용하여 form-urlencoded 형식으로 전송
  const params = new URLSearchParams();
  params.append("email", email);  // SecurityConfig: usernameParameter("email")
  params.append("pw", pw);        // SecurityConfig: passwordParameter("pw")

  const res = await axios.post(`${prefix}/login`, params, {
    headers: { "Content-Type": "application/x-www-form-urlencoded" },
  });
  return res.data;
};

/**
 * 회원가입
 */
export const signupPost = async (
  request: SignupRequest
): Promise<{ message: string }> => {
  const res = await axios.post(`${prefix}/signup`, request);
  return res.data;
};

/**
 * 프로필 조회
 */
export const getProfile = async (): Promise<ProfileInfo> => {
  const res = await jwtAxios.get(`${prefix}/profile`);
  return res.data;
};

/**
 * 프로필 수정
 */
export const updateProfile = async (
  request: ProfileUpdateRequest
): Promise<ProfileInfo> => {
  const res = await jwtAxios.put(`${prefix}/modify`, request);
  return res.data;
};

/**
 * 프로필 이미지 업로드
 */
export const uploadProfileImage = async (
  file: File
): Promise<{ imageUrl: string }> => {
  const formData = new FormData();
  formData.append("file", file);

  const res = await jwtAxios.post(`${prefix}/profile/image`, formData, {
    headers: { "Content-Type": "multipart/form-data" },
  });
  return res.data;
};

/**
 * 위치 업데이트
 */
export const updateLocation = async (
  request: LocationUpdateRequest
): Promise<{ message: string }> => {
  const res = await jwtAxios.put(`${prefix}/location`, request);
  return res.data;
};

/**
 * 공개 프로필 조회
 *
 * 다른 회원의 공개 프로필을 조회합니다.
 * 이메일, 비밀번호 등 민감한 정보는 포함되지 않습니다.
 */
export const getPublicProfile = async (memberId: number): Promise<PublicProfile> => {
  const res = await jwtAxios.get(`${prefix}/${memberId}/profile`);
  return res.data;
};

/**
 * 카카오 로그인
 */
export const kakaoLogin = async (code: string): Promise<MemberInfo> => {
  const res = await axios.get(`${prefix}/kakao?code=${code}`);
  return res.data;
};

// ============ 이메일 인증 API ============

const emailPrefix = `${ENV.API_URL}/api/email`;

/**
 * 인증 코드 발송
 */
export const sendVerificationCode = async (
  email: string
): Promise<{ success: boolean; message: string }> => {
  try {
    const res = await axios.post(`${emailPrefix}/send`, { email });
    return res.data;
  } catch (error) {
    // 400 에러 등에서 백엔드가 보낸 응답 데이터 반환
    if (axios.isAxiosError(error) && error.response?.data) {
      return error.response.data;
    }
    throw error;
  }
};

/**
 * 인증 코드 검증
 */
export const verifyEmailCode = async (
  email: string,
  code: string
): Promise<{ success: boolean; message: string }> => {
  try {
    const res = await axios.post(`${emailPrefix}/verify`, { email, code });
    return res.data;
  } catch (error) {
    // 400 에러 등에서 백엔드가 보낸 응답 데이터 반환
    if (axios.isAxiosError(error) && error.response?.data) {
      return error.response.data;
    }
    throw error;
  }
};

/**
 * 이메일 인증 상태 확인
 */
export const checkEmailVerified = async (
  email: string
): Promise<{ email: string; verified: boolean }> => {
  const res = await axios.get(`${emailPrefix}/status?email=${email}`);
  return res.data;
};
