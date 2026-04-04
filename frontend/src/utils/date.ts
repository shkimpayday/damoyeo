/**
 * 날짜 유틸리티 함수들
 */

/**
 * 서버(EC2/UTC)에서 LocalDateTime을 timezone 없이 직렬화할 때
 * 브라우저가 로컬 시간(KST)으로 잘못 해석하지 않도록 Z를 붙여 UTC로 파싱합니다.
 */
const parseDate = (dateString: string): Date => {
  if (!dateString) return new Date(NaN);
  if (/Z|[+-]\d{2}:?\d{2}$/.test(dateString)) return new Date(dateString);
  return new Date(dateString + "Z");
};

/**
 * ISO 날짜 문자열을 한국식 포맷으로 변환
 * @example formatDate("2024-01-15T10:30:00") => "2024.01.15"
 */
export const formatDate = (dateString: string): string => {
  const date = parseDate(dateString);
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, "0");
  const day = String(date.getDate()).padStart(2, "0");
  return `${year}.${month}.${day}`;
};

/**
 * ISO 날짜 문자열을 한국식 날짜+시간 포맷으로 변환
 * @example formatDateTime("2024-01-15T10:30:00") => "2024.01.15 10:30"
 */
export const formatDateTime = (dateString: string): string => {
  const date = parseDate(dateString);
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, "0");
  const day = String(date.getDate()).padStart(2, "0");
  const hours = String(date.getHours()).padStart(2, "0");
  const minutes = String(date.getMinutes()).padStart(2, "0");
  return `${year}.${month}.${day} ${hours}:${minutes}`;
};

/**
 * 상대적 시간 표현 (몇 분 전, 몇 시간 전 등)
 * @example getRelativeTime("2024-01-15T10:30:00") => "2시간 전"
 */
export const getRelativeTime = (dateString: string): string => {
  const date = parseDate(dateString);
  const now = new Date();
  const diffMs = now.getTime() - date.getTime();
  const diffSecs = Math.floor(diffMs / 1000);
  const diffMins = Math.floor(diffSecs / 60);
  const diffHours = Math.floor(diffMins / 60);
  const diffDays = Math.floor(diffHours / 24);
  const diffWeeks = Math.floor(diffDays / 7);
  const diffMonths = Math.floor(diffDays / 30);

  if (diffSecs < 60) return "방금 전";
  if (diffMins < 60) return `${diffMins}분 전`;
  if (diffHours < 24) return `${diffHours}시간 전`;
  if (diffDays < 7) return `${diffDays}일 전`;
  if (diffWeeks < 4) return `${diffWeeks}주 전`;
  if (diffMonths < 12) return `${diffMonths}개월 전`;
  return formatDate(dateString);
};

/**
 * 요일 가져오기
 */
export const getDayOfWeek = (dateString: string): string => {
  const days = ["일", "월", "화", "수", "목", "금", "토"];
  const date = parseDate(dateString);
  return days[date.getDay()];
};

/**
 * Date 객체를 YYYY-MM-DD 형식으로 변환 (input[type="date"] 용)
 */
export const toInputDateFormat = (date: Date): string => {
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, "0");
  const day = String(date.getDate()).padStart(2, "0");
  return `${year}-${month}-${day}`;
};

/**
 * Date 객체를 YYYY-MM-DDTHH:mm 형식으로 변환 (input[type="datetime-local"] 용)
 */
export const toInputDateTimeFormat = (date: Date): string => {
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, "0");
  const day = String(date.getDate()).padStart(2, "0");
  const hours = String(date.getHours()).padStart(2, "0");
  const minutes = String(date.getMinutes()).padStart(2, "0");
  return `${year}-${month}-${day}T${hours}:${minutes}`;
};
