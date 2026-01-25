import { ENV } from "@/config";

/**
 * 이미지 URL을 전체 경로로 변환
 *
 * 백엔드에서 반환하는 상대 경로(/uploads/...)를 전체 URL로 변환합니다.
 * 이미 전체 URL이거나 data URL인 경우 그대로 반환합니다.
 *
 * @param url 이미지 URL (상대 경로 또는 전체 URL)
 * @returns 전체 이미지 URL
 *
 * @example
 * getImageUrl("/uploads/profiles/abc.jpg")
 * // => "http://localhost:8080/uploads/profiles/abc.jpg"
 *
 * getImageUrl("https://example.com/image.jpg")
 * // => "https://example.com/image.jpg"
 */
export function getImageUrl(url: string | null | undefined): string {
  if (!url) return "";

  // 이미 전체 URL이거나 data URL인 경우 그대로 반환
  if (url.startsWith("http://") || url.startsWith("https://") || url.startsWith("data:")) {
    return url;
  }

  // 상대 경로인 경우 API URL 붙이기
  if (url.startsWith("/uploads")) {
    return `${ENV.API_URL}${url}`;
  }

  return url;
}
