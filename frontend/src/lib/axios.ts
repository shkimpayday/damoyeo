/**
 * Axios 인스턴스 설정
 *
 * jwtAxios: JWT 인증이 필요한 API 요청용
 * publicAxios: 인증이 필요 없는 공개 API 요청용
 */

import axios, {
  AxiosError,
  type AxiosResponse,
  type InternalAxiosRequestConfig,
} from "axios";
import { getCookie, setCookie, removeCookie } from "@/lib/cookie";
import { ENV } from "@/config";

// 공개 API용 axios 인스턴스
export const publicAxios = axios.create({
  baseURL: ENV.API_URL,
});

// JWT 인증이 필요한 API용 axios 인스턴스
export const jwtAxios = axios.create({
  baseURL: ENV.API_URL,
});

// 토큰 갱신 상태 관리
let isRefreshing = false;
let failedQueue: Array<{
  resolve: (token: string) => void;
  reject: (error: Error) => void;
}> = [];

/**
 * 대기열에 있는 모든 요청을 처리
 */
const processQueue = (error: Error | null, token: string | null) => {
  failedQueue.forEach((prom) => {
    if (error) {
      prom.reject(error);
    } else if (token) {
      prom.resolve(token);
    }
  });
  failedQueue = [];
};

/**
 * 로그인 페이지로 리다이렉트
 */
const redirectToLogin = () => {
  removeCookie("member");
  if (
    typeof window !== "undefined" &&
    !window.location.pathname.includes("/member/login")
  ) {
    window.location.href = "/member/login";
  }
};

/**
 * JWT 토큰 갱신
 */
const refreshJWT = async (
  accessToken: string,
  refreshToken: string
): Promise<{ accessToken: string; refreshToken: string }> => {
  const header = { headers: { Authorization: `Bearer ${accessToken}` } };

  const res = await axios.get(
    `${ENV.API_URL}/api/member/refresh?refreshToken=${refreshToken}`,
    header
  );

  return res.data;
};

/**
 * 요청 인터셉터: 모든 요청에 JWT 토큰을 헤더에 추가
 */
const beforeReq = (config: InternalAxiosRequestConfig) => {
  const memberInfo = getCookie("member");

  // 토큰이 없어도 요청은 보냄 (서버에서 401 처리)
  if (memberInfo?.accessToken) {
    config.headers.Authorization = `Bearer ${memberInfo.accessToken}`;
  }

  return config;
};

const requestFail = (err: AxiosError) => {
  return Promise.reject(err);
};

/**
 * 응답 성공 인터셉터
 */
const beforeRes = async (res: AxiosResponse): Promise<AxiosResponse> => {
  return res;
};

/**
 * 응답 에러 핸들러: 401 에러 시 토큰 갱신 시도
 */
const responseFail = async (err: AxiosError): Promise<AxiosResponse> => {
  const originalRequest = err.config as InternalAxiosRequestConfig & {
    _retry?: boolean;
  };

  if (err.response?.status === 401) {
    const errorData = err.response.data as { error?: string; message?: string };

    // Access Token 만료
    if (errorData?.error === "ERROR_ACCESS_TOKEN") {
      if (originalRequest._retry) {
        redirectToLogin();
        return Promise.reject(new Error("TOKEN_REFRESH_FAILED"));
      }

      // 다른 요청이 이미 토큰 갱신 중이면 대기열에 추가
      if (isRefreshing) {
        return new Promise((resolve, reject) => {
          failedQueue.push({
            resolve: (token: string) => {
              originalRequest.headers.Authorization = `Bearer ${token}`;
              resolve(axios(originalRequest));
            },
            reject: (error: Error) => {
              reject(error);
            },
          });
        });
      }

      originalRequest._retry = true;
      isRefreshing = true;

      try {
        const memberCookieValue = getCookie("member");

        if (
          !memberCookieValue?.accessToken ||
          !memberCookieValue?.refreshToken
        ) {
          throw new Error("NO_TOKENS");
        }

        const result = await refreshJWT(
          memberCookieValue.accessToken,
          memberCookieValue.refreshToken
        );

        memberCookieValue.accessToken = result.accessToken;
        memberCookieValue.refreshToken = result.refreshToken;
        setCookie("member", JSON.stringify(memberCookieValue), 1);

        processQueue(null, result.accessToken);

        originalRequest.headers.Authorization = `Bearer ${result.accessToken}`;
        return axios(originalRequest);
      } catch (refreshError) {
        processQueue(refreshError as Error, null);
        redirectToLogin();
        return Promise.reject(refreshError);
      } finally {
        isRefreshing = false;
      }
    }

    // 잘못된 토큰 또는 토큰 없음
    if (
      errorData?.error === "ERROR_INVALID_TOKEN" ||
      errorData?.error === "ERROR_NO_TOKEN"
    ) {
      redirectToLogin();
      return Promise.reject(err);
    }
  }

  return Promise.reject(err);
};

jwtAxios.interceptors.request.use(beforeReq, requestFail);
jwtAxios.interceptors.response.use(beforeRes, responseFail);

export default jwtAxios;
