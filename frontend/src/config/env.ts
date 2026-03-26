export const ENV = {
  API_URL: import.meta.env.VITE_API_HOST || 'http://localhost:8080',
  KAKAO_CLIENT_ID: import.meta.env.VITE_KAKAO_CLIENT_ID || '',
IS_DEV: import.meta.env.DEV,
  IS_PROD: import.meta.env.PROD,
} as const;
