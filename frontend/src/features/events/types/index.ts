// 이벤트/배너 타입 정의

export interface EventBanner {
  id: number;
  title: string;
  description: string;
  imageUrl: string;
  linkUrl: string;
  startDate: string;
  endDate: string;
  isActive: boolean;
}

export interface EventDetail extends EventBanner {
  content: string; // 상세 내용 (HTML or markdown)
  type: string; // PROMOTION, NOTICE, SPECIAL, FEATURE
  tags: string[];
  createdAt?: string;
}
