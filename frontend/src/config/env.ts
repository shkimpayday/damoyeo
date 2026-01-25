export const ENV = {
  API_URL: import.meta.env.VITE_API_HOST || 'http://localhost:8080',
  KAKAO_CLIENT_ID: import.meta.env.VITE_KAKAO_CLIENT_ID || '',
  KAKAO_REDIRECT_URI: import.meta.env.VITE_KAKAO_REDIRECT_URI || 'http://localhost:5173/member/kakao',
  IS_DEV: import.meta.env.DEV,
  IS_PROD: import.meta.env.PROD,
} as const;
