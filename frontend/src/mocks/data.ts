// 데모 데이터 - 풍성한 모임과 정모 데이터

import type { MemberSummary } from "@/features/auth";
import type { Category } from "@/features/groups/types";


// 샘플 프로필 이미지 (picsum.photos 사용)
const getProfileImage = (seed: number) =>
  `https://picsum.photos/seed/profile${seed}/200/200`;

// 샘플 커버 이미지
const getCoverImage = (seed: number) =>
  `https://picsum.photos/seed/cover${seed}/800/400`;

// 샘플 멤버들
export const DEMO_MEMBERS: MemberSummary[] = [
  {
    id: 1,
    nickname: "김민준",
    profileImage: getProfileImage(1),
  },
  {
    id: 2,
    nickname: "이서연",
    profileImage: getProfileImage(2),
  },
  {
    id: 3,
    nickname: "박지훈",
    profileImage: getProfileImage(3),
  },
  {
    id: 4,
    nickname: "최수빈",
    profileImage: getProfileImage(4),
  },
  {
    id: 5,
    nickname: "정하늘",
    profileImage: getProfileImage(5),
  },
  {
    id: 6,
    nickname: "강도윤",
    profileImage: getProfileImage(6),
  },
  {
    id: 7,
    nickname: "윤채원",
    profileImage: getProfileImage(7),
  },
  {
    id: 8,
    nickname: "신준혁",
    profileImage: getProfileImage(8),
  },
  {
    id: 9,
    nickname: "한소미",
    profileImage: getProfileImage(9),
  },
  {
    id: 10,
    nickname: "오지민",
    profileImage: getProfileImage(10),
  },
];

// 카테고리 이미지
const getCategoryImage = (id: number) =>
  `https://picsum.photos/seed/category${id}/400/300`;

// 카테고리 데이터
export const DEMO_CATEGORIES: Category[] = [
  { id: 1, name: "운동/스포츠", icon: "🏃", displayOrder: 1, color: "#FF6B6B", image: getCategoryImage(1) },
  { id: 2, name: "사교/인맥", icon: "🤝", displayOrder: 2, color: "#4ECDC4", image: getCategoryImage(2) },
  { id: 3, name: "아웃도어/여행", icon: "🏕️", displayOrder: 3, color: "#45B7D1", image: getCategoryImage(3) },
  { id: 4, name: "문화/공연", icon: "🎭", displayOrder: 4, color: "#96CEB4", image: getCategoryImage(4) },
  { id: 5, name: "음악/악기", icon: "🎸", displayOrder: 5, color: "#FFEAA7", image: getCategoryImage(5) },
  { id: 6, name: "외국어", icon: "🌍", displayOrder: 6, color: "#DDA0DD", image: getCategoryImage(6) },
  { id: 7, name: "독서", icon: "📚", displayOrder: 7, color: "#98D8C8", image: getCategoryImage(7) },
  { id: 8, name: "스터디", icon: "📖", displayOrder: 8, color: "#F7DC6F", image: getCategoryImage(8) },
  { id: 9, name: "게임/오락", icon: "🎮", displayOrder: 9, color: "#BB8FCE", image: getCategoryImage(9) },
  { id: 10, name: "사진/영상", icon: "📷", displayOrder: 10, color: "#85C1E9", image: getCategoryImage(10) },
  { id: 11, name: "요리", icon: "🍳", displayOrder: 11, color: "#F8B500", image: getCategoryImage(11) },
  { id: 12, name: "공예", icon: "🎨", displayOrder: 12, color: "#E59866", image: getCategoryImage(12) },
  { id: 13, name: "자기계발", icon: "💪", displayOrder: 13, color: "#58D68D", image: getCategoryImage(13) },
  { id: 14, name: "봉사활동", icon: "❤️", displayOrder: 14, color: "#EC7063", image: getCategoryImage(14) },
  { id: 15, name: "반려동물", icon: "🐕", displayOrder: 15, color: "#AF7AC5", image: getCategoryImage(15) },
  { id: 16, name: "IT/개발", icon: "💻", displayOrder: 16, color: "#5DADE2", image: getCategoryImage(16) },
  { id: 17, name: "금융/재테크", icon: "💰", displayOrder: 17, color: "#F4D03F", image: getCategoryImage(17) },
  { id: 18, name: "기타", icon: "✨", displayOrder: 18, color: "#AAB7B8", image: getCategoryImage(18) },
];

// 서울 주요 지역 (모임 생성에 사용)
export const DEMO_LOCATIONS = [
  { address: "서울 강남구 역삼동", lat: 37.5007, lng: 127.0365 },
  { address: "서울 마포구 홍대입구", lat: 37.5563, lng: 126.9237 },
  { address: "서울 송파구 잠실동", lat: 37.5133, lng: 127.1002 },
  { address: "서울 종로구 광화문", lat: 37.5759, lng: 126.9769 },
  { address: "서울 용산구 이태원동", lat: 37.5345, lng: 126.9946 },
  { address: "서울 서초구 서초동", lat: 37.4918, lng: 127.0072 },
  { address: "서울 영등포구 여의도동", lat: 37.5219, lng: 126.9245 },
  { address: "서울 성동구 성수동", lat: 37.5447, lng: 127.0557 },
  { address: "서울 강서구 마곡동", lat: 37.5584, lng: 126.8289 },
  { address: "서울 노원구 상계동", lat: 37.6548, lng: 127.0612 },
];

// 모임 데이터 생성
export const DEMO_GROUPS = [
  // 운동/스포츠
  {
    id: 1,
    name: "강남 러닝 크루",
    description:
      "매주 토요일 아침 한강에서 함께 달려요! 초보자도 환영합니다. 러닝 후에는 맛있는 브런치도 함께해요.",
    category: DEMO_CATEGORIES[0],
    coverImage: getCoverImage(101),
    thumbnailImage: getCoverImage(101),
    location: { lat: 37.5007, lng: 127.0365 },
    address: "서울 강남구 역삼동",
    maxMembers: 30,
    memberCount: 24,
    isPublic: true,
    status: "ACTIVE" as const,
    owner: DEMO_MEMBERS[0],
    myRole: null,
    createdAt: "2024-01-15T09:00:00Z",
    updatedAt: "2024-12-01T10:00:00Z",
  },
  {
    id: 2,
    name: "테니스 동호회 SMASH",
    description:
      "테니스를 사랑하는 사람들의 모임입니다. 주 2회 정기 레슨과 친선 경기를 진행합니다.",
    category: DEMO_CATEGORIES[0],
    coverImage: getCoverImage(102),
    thumbnailImage: getCoverImage(102),
    location: { lat: 37.5133, lng: 127.1002 },
    address: "서울 송파구 잠실동",
    maxMembers: 20,
    memberCount: 18,
    isPublic: true,
    status: "ACTIVE" as const,
    owner: DEMO_MEMBERS[1],
    myRole: null,
    createdAt: "2024-02-20T09:00:00Z",
    updatedAt: "2024-11-28T10:00:00Z",
  },
  {
    id: 3,
    name: "홍대 배드민턴 클럽",
    description:
      "즐거운 배드민턴 한판! 실력 무관 누구나 환영합니다. 매주 수요일, 금요일 저녁 모임.",
    category: DEMO_CATEGORIES[0],
    coverImage: getCoverImage(103),
    thumbnailImage: getCoverImage(103),
    location: { lat: 37.5563, lng: 126.9237 },
    address: "서울 마포구 홍대입구",
    maxMembers: 16,
    memberCount: 14,
    isPublic: true,
    status: "ACTIVE" as const,
    owner: DEMO_MEMBERS[2],
    myRole: null,
    createdAt: "2024-03-10T09:00:00Z",
    updatedAt: "2024-12-05T10:00:00Z",
  },

  // 사교/인맥
  {
    id: 4,
    name: "30대 직장인 네트워킹",
    description:
      "같은 세대 직장인들과 함께하는 네트워킹 모임. 다양한 업계 사람들과 소통하며 인맥을 넓혀보세요!",
    category: DEMO_CATEGORIES[1],
    coverImage: getCoverImage(104),
    thumbnailImage: getCoverImage(104),
    location: { lat: 37.5007, lng: 127.0365 },
    address: "서울 강남구 역삼동",
    maxMembers: 50,
    memberCount: 42,
    isPublic: true,
    status: "ACTIVE" as const,
    owner: DEMO_MEMBERS[3],
    myRole: null,
    createdAt: "2024-01-05T09:00:00Z",
    updatedAt: "2024-12-10T10:00:00Z",
  },
  {
    id: 5,
    name: "와인 테이스팅 클럽",
    description:
      "와인을 즐기며 대화하는 모임입니다. 매월 다른 와인을 시음하고 와인 지식도 쌓아가요.",
    category: DEMO_CATEGORIES[1],
    coverImage: getCoverImage(105),
    thumbnailImage: getCoverImage(105),
    location: { lat: 37.5345, lng: 126.9946 },
    address: "서울 용산구 이태원동",
    maxMembers: 25,
    memberCount: 21,
    isPublic: true,
    status: "ACTIVE" as const,
    owner: DEMO_MEMBERS[4],
    myRole: null,
    createdAt: "2024-04-01T09:00:00Z",
    updatedAt: "2024-11-30T10:00:00Z",
  },

  // 아웃도어/여행
  {
    id: 6,
    name: "주말 등산 모임",
    description:
      "서울 근교 산을 함께 오르는 등산 모임입니다. 북한산, 도봉산, 관악산 등 다양한 코스를 탐험해요!",
    category: DEMO_CATEGORIES[2],
    coverImage: getCoverImage(106),
    thumbnailImage: getCoverImage(106),
    location: { lat: 37.6548, lng: 127.0612 },
    address: "서울 노원구 상계동",
    maxMembers: 40,
    memberCount: 35,
    isPublic: true,
    status: "ACTIVE" as const,
    owner: DEMO_MEMBERS[5],
    myRole: null,
    createdAt: "2023-09-15T09:00:00Z",
    updatedAt: "2024-12-08T10:00:00Z",
  },
  {
    id: 7,
    name: "캠핑 러버스",
    description:
      "캠핑을 사랑하는 사람들의 모임! 장비 공유, 캠핑장 추천, 함께 떠나는 캠핑 여행까지!",
    category: DEMO_CATEGORIES[2],
    coverImage: getCoverImage(107),
    thumbnailImage: getCoverImage(107),
    location: { lat: 37.5219, lng: 126.9245 },
    address: "서울 영등포구 여의도동",
    maxMembers: 30,
    memberCount: 27,
    isPublic: true,
    status: "ACTIVE" as const,
    owner: DEMO_MEMBERS[6],
    myRole: null,
    createdAt: "2024-05-01T09:00:00Z",
    updatedAt: "2024-12-01T10:00:00Z",
  },

  // 문화/공연
  {
    id: 8,
    name: "뮤지컬 덕후 모임",
    description:
      "뮤지컬을 사랑하는 사람들의 모임입니다. 함께 뮤지컬 관람하고, 리뷰도 나눠요!",
    category: DEMO_CATEGORIES[3],
    coverImage: getCoverImage(108),
    thumbnailImage: getCoverImage(108),
    location: { lat: 37.5759, lng: 126.9769 },
    address: "서울 종로구 광화문",
    maxMembers: 35,
    memberCount: 30,
    isPublic: true,
    status: "ACTIVE" as const,
    owner: DEMO_MEMBERS[7],
    myRole: null,
    createdAt: "2024-02-14T09:00:00Z",
    updatedAt: "2024-12-12T10:00:00Z",
  },
  {
    id: 9,
    name: "인디밴드 공연 탐방",
    description:
      "홍대 인디씬을 탐험하는 모임! 숨겨진 명밴드를 발굴하고 함께 공연을 즐겨요.",
    category: DEMO_CATEGORIES[3],
    coverImage: getCoverImage(109),
    thumbnailImage: getCoverImage(109),
    location: { lat: 37.5563, lng: 126.9237 },
    address: "서울 마포구 홍대입구",
    maxMembers: 20,
    memberCount: 17,
    isPublic: true,
    status: "ACTIVE" as const,
    owner: DEMO_MEMBERS[8],
    myRole: null,
    createdAt: "2024-06-01T09:00:00Z",
    updatedAt: "2024-11-25T10:00:00Z",
  },

  // 음악/악기
  {
    id: 10,
    name: "기타 동호회 '코드'",
    description:
      "기타를 배우고 연주하는 모임입니다. 어쿠스틱, 일렉, 클래식 모두 환영해요!",
    category: DEMO_CATEGORIES[4],
    coverImage: getCoverImage(110),
    thumbnailImage: getCoverImage(110),
    location: { lat: 37.5447, lng: 127.0557 },
    address: "서울 성동구 성수동",
    maxMembers: 15,
    memberCount: 12,
    isPublic: true,
    status: "ACTIVE" as const,
    owner: DEMO_MEMBERS[9],
    myRole: null,
    createdAt: "2024-03-20T09:00:00Z",
    updatedAt: "2024-12-05T10:00:00Z",
  },
  {
    id: 11,
    name: "피아노 소모임",
    description:
      "피아노를 사랑하는 사람들의 모임. 클래식부터 재즈, 팝까지 다양한 장르를 연주해요.",
    category: DEMO_CATEGORIES[4],
    coverImage: getCoverImage(111),
    thumbnailImage: getCoverImage(111),
    location: { lat: 37.4918, lng: 127.0072 },
    address: "서울 서초구 서초동",
    maxMembers: 12,
    memberCount: 10,
    isPublic: true,
    status: "ACTIVE" as const,
    owner: DEMO_MEMBERS[0],
    myRole: null,
    createdAt: "2024-07-01T09:00:00Z",
    updatedAt: "2024-11-20T10:00:00Z",
  },

  // 외국어
  {
    id: 12,
    name: "영어 스피킹 클럽",
    description:
      "영어로 자유롭게 대화하는 모임! 다양한 주제로 토론하며 스피킹 실력을 키워요.",
    category: DEMO_CATEGORIES[5],
    coverImage: getCoverImage(112),
    thumbnailImage: getCoverImage(112),
    location: { lat: 37.5345, lng: 126.9946 },
    address: "서울 용산구 이태원동",
    maxMembers: 20,
    memberCount: 18,
    isPublic: true,
    status: "ACTIVE" as const,
    owner: DEMO_MEMBERS[1],
    myRole: null,
    createdAt: "2024-01-10T09:00:00Z",
    updatedAt: "2024-12-10T10:00:00Z",
  },
  {
    id: 13,
    name: "일본어 회화 모임",
    description:
      "일본어로 대화하며 일본 문화도 함께 즐기는 모임입니다. JLPT 준비도 함께해요!",
    category: DEMO_CATEGORIES[5],
    coverImage: getCoverImage(113),
    thumbnailImage: getCoverImage(113),
    location: { lat: 37.5007, lng: 127.0365 },
    address: "서울 강남구 역삼동",
    maxMembers: 15,
    memberCount: 13,
    isPublic: true,
    status: "ACTIVE" as const,
    owner: DEMO_MEMBERS[2],
    myRole: null,
    createdAt: "2024-04-15T09:00:00Z",
    updatedAt: "2024-12-01T10:00:00Z",
  },

  // 독서
  {
    id: 14,
    name: "월간 북클럽",
    description:
      "매월 한 권의 책을 선정하여 함께 읽고 토론하는 독서 모임입니다.",
    category: DEMO_CATEGORIES[6],
    coverImage: getCoverImage(114),
    thumbnailImage: getCoverImage(114),
    location: { lat: 37.5759, lng: 126.9769 },
    address: "서울 종로구 광화문",
    maxMembers: 25,
    memberCount: 22,
    isPublic: true,
    status: "ACTIVE" as const,
    owner: DEMO_MEMBERS[3],
    myRole: null,
    createdAt: "2023-11-01T09:00:00Z",
    updatedAt: "2024-12-08T10:00:00Z",
  },
  {
    id: 15,
    name: "SF/판타지 독서 모임",
    description:
      "SF와 판타지 장르를 사랑하는 독서가들의 모임. 국내외 명작부터 신작까지!",
    category: DEMO_CATEGORIES[6],
    coverImage: getCoverImage(115),
    thumbnailImage: getCoverImage(115),
    location: { lat: 37.5563, lng: 126.9237 },
    address: "서울 마포구 홍대입구",
    maxMembers: 20,
    memberCount: 16,
    isPublic: true,
    status: "ACTIVE" as const,
    owner: DEMO_MEMBERS[4],
    myRole: null,
    createdAt: "2024-02-01T09:00:00Z",
    updatedAt: "2024-11-28T10:00:00Z",
  },

  // 스터디
  {
    id: 16,
    name: "개발자 알고리즘 스터디",
    description:
      "코딩 테스트 대비 알고리즘 스터디! 백준, 프로그래머스 문제를 함께 풀어요.",
    category: DEMO_CATEGORIES[7],
    coverImage: getCoverImage(116),
    thumbnailImage: getCoverImage(116),
    location: { lat: 37.5007, lng: 127.0365 },
    address: "서울 강남구 역삼동",
    maxMembers: 12,
    memberCount: 10,
    isPublic: true,
    status: "ACTIVE" as const,
    owner: DEMO_MEMBERS[5],
    myRole: null,
    createdAt: "2024-03-01T09:00:00Z",
    updatedAt: "2024-12-12T10:00:00Z",
  },
  {
    id: 17,
    name: "토익 900점 목표반",
    description:
      "토익 고득점을 목표로 함께 공부하는 스터디입니다. 주 2회 모임, 모의고사 진행.",
    category: DEMO_CATEGORIES[7],
    coverImage: getCoverImage(117),
    thumbnailImage: getCoverImage(117),
    location: { lat: 37.5133, lng: 127.1002 },
    address: "서울 송파구 잠실동",
    maxMembers: 10,
    memberCount: 8,
    isPublic: true,
    status: "ACTIVE" as const,
    owner: DEMO_MEMBERS[6],
    myRole: null,
    createdAt: "2024-08-01T09:00:00Z",
    updatedAt: "2024-12-05T10:00:00Z",
  },

  // 게임/오락
  {
    id: 18,
    name: "보드게임 카페 탐방",
    description:
      "다양한 보드게임을 즐기는 모임! 매주 새로운 보드게임 카페를 탐방해요.",
    category: DEMO_CATEGORIES[8],
    coverImage: getCoverImage(118),
    thumbnailImage: getCoverImage(118),
    location: { lat: 37.5563, lng: 126.9237 },
    address: "서울 마포구 홍대입구",
    maxMembers: 16,
    memberCount: 14,
    isPublic: true,
    status: "ACTIVE" as const,
    owner: DEMO_MEMBERS[7],
    myRole: null,
    createdAt: "2024-05-15T09:00:00Z",
    updatedAt: "2024-12-01T10:00:00Z",
  },
  {
    id: 19,
    name: "PC방 정모 클럽",
    description:
      "롤, 발로란트, 오버워치 등 함께 게임하는 모임! 실력 무관 즐겜러 환영.",
    category: DEMO_CATEGORIES[8],
    coverImage: getCoverImage(119),
    thumbnailImage: getCoverImage(119),
    location: { lat: 37.5447, lng: 127.0557 },
    address: "서울 성동구 성수동",
    maxMembers: 20,
    memberCount: 18,
    isPublic: true,
    status: "ACTIVE" as const,
    owner: DEMO_MEMBERS[8],
    myRole: null,
    createdAt: "2024-06-20T09:00:00Z",
    updatedAt: "2024-11-30T10:00:00Z",
  },

  // 사진/영상
  {
    id: 20,
    name: "출사 동호회 '렌즈'",
    description:
      "서울의 아름다운 장소를 함께 촬영하는 출사 모임입니다. 초보자도 환영해요!",
    category: DEMO_CATEGORIES[9],
    coverImage: getCoverImage(120),
    thumbnailImage: getCoverImage(120),
    location: { lat: 37.5759, lng: 126.9769 },
    address: "서울 종로구 광화문",
    maxMembers: 25,
    memberCount: 20,
    isPublic: true,
    status: "ACTIVE" as const,
    owner: DEMO_MEMBERS[9],
    myRole: null,
    createdAt: "2024-01-20T09:00:00Z",
    updatedAt: "2024-12-10T10:00:00Z",
  },
  {
    id: 21,
    name: "유튜브 크리에이터 모임",
    description:
      "유튜브 채널 운영 노하우 공유! 촬영, 편집, 기획까지 함께 성장해요.",
    category: DEMO_CATEGORIES[9],
    coverImage: getCoverImage(121),
    thumbnailImage: getCoverImage(121),
    location: { lat: 37.5007, lng: 127.0365 },
    address: "서울 강남구 역삼동",
    maxMembers: 15,
    memberCount: 12,
    isPublic: true,
    status: "ACTIVE" as const,
    owner: DEMO_MEMBERS[0],
    myRole: null,
    createdAt: "2024-04-01T09:00:00Z",
    updatedAt: "2024-11-25T10:00:00Z",
  },

  // 요리
  {
    id: 22,
    name: "홈쿠킹 클래스",
    description:
      "집에서 만드는 근사한 요리! 매주 다른 레시피를 함께 만들어봐요.",
    category: DEMO_CATEGORIES[10],
    coverImage: getCoverImage(122),
    thumbnailImage: getCoverImage(122),
    location: { lat: 37.5345, lng: 126.9946 },
    address: "서울 용산구 이태원동",
    maxMembers: 12,
    memberCount: 10,
    isPublic: true,
    status: "ACTIVE" as const,
    owner: DEMO_MEMBERS[1],
    myRole: null,
    createdAt: "2024-02-28T09:00:00Z",
    updatedAt: "2024-12-08T10:00:00Z",
  },
  {
    id: 23,
    name: "베이킹 동호회",
    description: "빵과 디저트를 직접 만들어보는 베이킹 모임! 레시피 공유도 활발해요.",
    category: DEMO_CATEGORIES[10],
    coverImage: getCoverImage(123),
    thumbnailImage: getCoverImage(123),
    location: { lat: 37.4918, lng: 127.0072 },
    address: "서울 서초구 서초동",
    maxMembers: 10,
    memberCount: 9,
    isPublic: true,
    status: "ACTIVE" as const,
    owner: DEMO_MEMBERS[2],
    myRole: null,
    createdAt: "2024-07-15T09:00:00Z",
    updatedAt: "2024-12-01T10:00:00Z",
  },

  // IT/개발
  {
    id: 24,
    name: "React 개발자 모임",
    description:
      "React 개발자들의 네트워킹과 스터디 모임! 최신 트렌드와 기술을 공유해요.",
    category: DEMO_CATEGORIES[15],
    coverImage: getCoverImage(124),
    thumbnailImage: getCoverImage(124),
    location: { lat: 37.5007, lng: 127.0365 },
    address: "서울 강남구 역삼동",
    maxMembers: 30,
    memberCount: 26,
    isPublic: true,
    status: "ACTIVE" as const,
    owner: DEMO_MEMBERS[3],
    myRole: null,
    createdAt: "2024-01-01T09:00:00Z",
    updatedAt: "2024-12-12T10:00:00Z",
  },
  {
    id: 25,
    name: "AI/ML 스터디 그룹",
    description:
      "인공지능과 머신러닝을 공부하는 모임입니다. 논문 리뷰, 프로젝트 진행도 함께해요.",
    category: DEMO_CATEGORIES[15],
    coverImage: getCoverImage(125),
    thumbnailImage: getCoverImage(125),
    location: { lat: 37.5584, lng: 126.8289 },
    address: "서울 강서구 마곡동",
    maxMembers: 20,
    memberCount: 17,
    isPublic: true,
    status: "ACTIVE" as const,
    owner: DEMO_MEMBERS[4],
    myRole: null,
    createdAt: "2024-03-15T09:00:00Z",
    updatedAt: "2024-12-05T10:00:00Z",
  },
  {
    id: 26,
    name: "백엔드 개발자 커뮤니티",
    description:
      "Spring, Node.js, Go 등 백엔드 기술 스택을 공유하는 개발자 모임입니다.",
    category: DEMO_CATEGORIES[15],
    coverImage: getCoverImage(126),
    thumbnailImage: getCoverImage(126),
    location: { lat: 37.5447, lng: 127.0557 },
    address: "서울 성동구 성수동",
    maxMembers: 25,
    memberCount: 21,
    isPublic: true,
    status: "ACTIVE" as const,
    owner: DEMO_MEMBERS[5],
    myRole: null,
    createdAt: "2024-02-10T09:00:00Z",
    updatedAt: "2024-11-28T10:00:00Z",
  },

  // 금융/재테크
  {
    id: 27,
    name: "주식 투자 스터디",
    description:
      "가치투자, 기술적 분석 등 투자 전략을 공유하고 함께 공부하는 모임입니다.",
    category: DEMO_CATEGORIES[16],
    coverImage: getCoverImage(127),
    thumbnailImage: getCoverImage(127),
    location: { lat: 37.5219, lng: 126.9245 },
    address: "서울 영등포구 여의도동",
    maxMembers: 20,
    memberCount: 18,
    isPublic: true,
    status: "ACTIVE" as const,
    owner: DEMO_MEMBERS[6],
    myRole: null,
    createdAt: "2024-01-25T09:00:00Z",
    updatedAt: "2024-12-10T10:00:00Z",
  },
  {
    id: 28,
    name: "부동산 투자 연구회",
    description: "부동산 시장 분석과 투자 전략을 함께 연구하는 모임입니다.",
    category: DEMO_CATEGORIES[16],
    coverImage: getCoverImage(128),
    thumbnailImage: getCoverImage(128),
    location: { lat: 37.5007, lng: 127.0365 },
    address: "서울 강남구 역삼동",
    maxMembers: 15,
    memberCount: 13,
    isPublic: true,
    status: "ACTIVE" as const,
    owner: DEMO_MEMBERS[7],
    myRole: null,
    createdAt: "2024-04-20T09:00:00Z",
    updatedAt: "2024-12-01T10:00:00Z",
  },

  // 반려동물
  {
    id: 29,
    name: "강아지 산책 메이트",
    description:
      "반려견과 함께 산책하는 모임! 한강공원, 서울숲 등에서 함께 산책해요.",
    category: DEMO_CATEGORIES[14],
    coverImage: getCoverImage(129),
    thumbnailImage: getCoverImage(129),
    location: { lat: 37.5447, lng: 127.0557 },
    address: "서울 성동구 성수동",
    maxMembers: 20,
    memberCount: 16,
    isPublic: true,
    status: "ACTIVE" as const,
    owner: DEMO_MEMBERS[8],
    myRole: null,
    createdAt: "2024-05-01T09:00:00Z",
    updatedAt: "2024-12-08T10:00:00Z",
  },
  {
    id: 30,
    name: "고양이 집사 모임",
    description:
      "고양이를 키우는 집사들의 모임! 육아 팁, 용품 추천, 정보 공유해요.",
    category: DEMO_CATEGORIES[14],
    coverImage: getCoverImage(130),
    thumbnailImage: getCoverImage(130),
    location: { lat: 37.5563, lng: 126.9237 },
    address: "서울 마포구 홍대입구",
    maxMembers: 30,
    memberCount: 25,
    isPublic: true,
    status: "ACTIVE" as const,
    owner: DEMO_MEMBERS[9],
    myRole: null,
    createdAt: "2024-06-10T09:00:00Z",
    updatedAt: "2024-11-30T10:00:00Z",
  },
];

// GroupListDTO 형태로 변환
export const DEMO_GROUP_LIST = DEMO_GROUPS.map((g) => ({
  id: g.id,
  name: g.name,
  category: g.category,
  thumbnailImage: g.thumbnailImage,
  address: g.address,
  memberCount: g.memberCount,
  maxMembers: g.maxMembers,
  distance: Math.floor(Math.random() * 5000), // 0~5km
}));

// 정모(Meeting) 데이터 생성
const now = new Date();
const addDays = (days: number) => {
  const d = new Date(now);
  d.setDate(d.getDate() + days);
  return d.toISOString();
};

export const DEMO_MEETINGS = [
  // 강남 러닝 크루 정모
  {
    id: 1,
    groupId: 1,
    groupName: "강남 러닝 크루",
    title: "토요일 아침 한강 러닝 10km",
    description:
      "이번 주 토요일 아침 7시에 한강에서 만나요! 10km 코스로 진행하며, 러닝 후 브런치까지 함께합니다.",
    location: { lat: 37.5007, lng: 127.0365 },
    address: "서울 강남구 청담대교 남단",
    meetingDate: addDays(2),
    endDate: addDays(2),
    maxAttendees: 20,
    currentAttendees: 15,
    fee: 0,
    status: "SCHEDULED" as const,
    myStatus: null,
    createdBy: DEMO_MEMBERS[0],
    createdAt: "2024-12-10T09:00:00Z",
  },
  {
    id: 2,
    groupId: 1,
    groupName: "강남 러닝 크루",
    title: "다음주 수요일 야간 러닝",
    description: "퇴근 후 함께 달려요! 5km 코스로 가볍게 진행합니다.",
    location: { lat: 37.5007, lng: 127.0365 },
    address: "서울 강남구 선릉역 3번 출구",
    meetingDate: addDays(5),
    endDate: addDays(5),
    maxAttendees: 15,
    currentAttendees: 8,
    fee: 0,
    status: "SCHEDULED" as const,
    myStatus: null,
    createdBy: DEMO_MEMBERS[0],
    createdAt: "2024-12-11T09:00:00Z",
  },

  // 테니스 동호회 정모
  {
    id: 3,
    groupId: 2,
    groupName: "테니스 동호회 SMASH",
    title: "주말 테니스 친선 경기",
    description:
      "이번 주 일요일 오후 2시 잠실 테니스장에서 친선 경기를 진행합니다!",
    location: { lat: 37.5133, lng: 127.1002 },
    address: "서울 송파구 잠실 테니스장",
    meetingDate: addDays(3),
    endDate: addDays(3),
    maxAttendees: 16,
    currentAttendees: 12,
    fee: 10000,
    status: "SCHEDULED" as const,
    myStatus: null,
    createdBy: DEMO_MEMBERS[1],
    createdAt: "2024-12-09T09:00:00Z",
  },

  // 30대 직장인 네트워킹 정모
  {
    id: 4,
    groupId: 4,
    groupName: "30대 직장인 네트워킹",
    title: "금요일 저녁 네트워킹 파티",
    description:
      "강남역 근처 라운지바에서 네트워킹 파티를 진행합니다. 다양한 업계 분들과 소통해보세요!",
    location: { lat: 37.5007, lng: 127.0365 },
    address: "서울 강남구 역삼동 라운지바 W",
    meetingDate: addDays(4),
    endDate: addDays(4),
    maxAttendees: 40,
    currentAttendees: 32,
    fee: 30000,
    status: "SCHEDULED" as const,
    myStatus: null,
    createdBy: DEMO_MEMBERS[3],
    createdAt: "2024-12-08T09:00:00Z",
  },

  // 와인 테이스팅 클럽 정모
  {
    id: 5,
    groupId: 5,
    groupName: "와인 테이스팅 클럽",
    title: "1월 와인 테이스팅 - 이탈리아 와인",
    description:
      "이번 달은 이탈리아 와인을 시음합니다. 바롤로, 키안티 등 5종의 와인을 맛볼 예정입니다.",
    location: { lat: 37.5345, lng: 126.9946 },
    address: "서울 용산구 이태원동 와인바 소노",
    meetingDate: addDays(7),
    endDate: addDays(7),
    maxAttendees: 20,
    currentAttendees: 18,
    fee: 50000,
    status: "SCHEDULED" as const,
    myStatus: null,
    createdBy: DEMO_MEMBERS[4],
    createdAt: "2024-12-07T09:00:00Z",
  },

  // 주말 등산 모임 정모
  {
    id: 6,
    groupId: 6,
    groupName: "주말 등산 모임",
    title: "북한산 백운대 코스 등반",
    description:
      "이번 주 토요일 북한산 백운대 코스를 등반합니다. 초보자도 환영해요!",
    location: { lat: 37.6548, lng: 127.0612 },
    address: "서울 은평구 북한산 국립공원 입구",
    meetingDate: addDays(2),
    endDate: addDays(2),
    maxAttendees: 25,
    currentAttendees: 20,
    fee: 0,
    status: "SCHEDULED" as const,
    myStatus: null,
    createdBy: DEMO_MEMBERS[5],
    createdAt: "2024-12-10T09:00:00Z",
  },

  // 뮤지컬 덕후 모임 정모
  {
    id: 7,
    groupId: 8,
    groupName: "뮤지컬 덕후 모임",
    title: "지킬앤하이드 단체 관람",
    description:
      "뮤지컬 '지킬앤하이드' 단체 관람 후 뒷풀이까지! 좋은 좌석 확보했어요.",
    location: { lat: 37.5759, lng: 126.9769 },
    address: "서울 종로구 세종문화회관",
    meetingDate: addDays(10),
    endDate: addDays(10),
    maxAttendees: 30,
    currentAttendees: 27,
    fee: 80000,
    status: "SCHEDULED" as const,
    myStatus: null,
    createdBy: DEMO_MEMBERS[7],
    createdAt: "2024-12-05T09:00:00Z",
  },

  // 영어 스피킹 클럽 정모
  {
    id: 8,
    groupId: 12,
    groupName: "영어 스피킹 클럽",
    title: "이번 주 토픽: Tech & AI",
    description:
      "이번 주 주제는 'Tech & AI'입니다. AI의 미래에 대해 영어로 토론해봐요!",
    location: { lat: 37.5345, lng: 126.9946 },
    address: "서울 용산구 이태원동 카페 네이버스",
    meetingDate: addDays(1),
    endDate: addDays(1),
    maxAttendees: 15,
    currentAttendees: 12,
    fee: 5000,
    status: "SCHEDULED" as const,
    myStatus: null,
    createdBy: DEMO_MEMBERS[1],
    createdAt: "2024-12-12T09:00:00Z",
  },

  // 월간 북클럽 정모
  {
    id: 9,
    groupId: 14,
    groupName: "월간 북클럽",
    title: "1월 도서 토론 - '아몬드'",
    description:
      "이번 달 선정 도서 '아몬드'를 함께 읽고 토론합니다. 감동적인 이야기를 나눠요.",
    location: { lat: 37.5759, lng: 126.9769 },
    address: "서울 종로구 광화문 교보문고 내 북카페",
    meetingDate: addDays(8),
    endDate: addDays(8),
    maxAttendees: 20,
    currentAttendees: 16,
    fee: 0,
    status: "SCHEDULED" as const,
    myStatus: null,
    createdBy: DEMO_MEMBERS[3],
    createdAt: "2024-12-06T09:00:00Z",
  },

  // 개발자 알고리즘 스터디 정모
  {
    id: 10,
    groupId: 16,
    groupName: "개발자 알고리즘 스터디",
    title: "이번 주 문제 풀이 - DP 특집",
    description:
      "이번 주는 동적 프로그래밍(DP) 문제를 집중적으로 풀어봅니다. 5문제 준비했어요!",
    location: { lat: 37.5007, lng: 127.0365 },
    address: "서울 강남구 역삼동 스터디카페",
    meetingDate: addDays(3),
    endDate: addDays(3),
    maxAttendees: 10,
    currentAttendees: 8,
    fee: 5000,
    status: "SCHEDULED" as const,
    myStatus: null,
    createdBy: DEMO_MEMBERS[5],
    createdAt: "2024-12-11T09:00:00Z",
  },

  // 보드게임 카페 탐방 정모
  {
    id: 11,
    groupId: 18,
    groupName: "보드게임 카페 탐방",
    title: "이번 주 탐방: 홍대 레드버튼",
    description:
      "이번 주는 홍대 '레드버튼'에서 만나요! 신작 보드게임도 플레이해봅니다.",
    location: { lat: 37.5563, lng: 126.9237 },
    address: "서울 마포구 홍대입구 레드버튼",
    meetingDate: addDays(2),
    endDate: addDays(2),
    maxAttendees: 12,
    currentAttendees: 10,
    fee: 15000,
    status: "SCHEDULED" as const,
    myStatus: null,
    createdBy: DEMO_MEMBERS[7],
    createdAt: "2024-12-10T09:00:00Z",
  },

  // React 개발자 모임 정모
  {
    id: 12,
    groupId: 24,
    groupName: "React 개발자 모임",
    title: "React 19 신기능 세미나",
    description:
      "React 19의 새로운 기능들을 함께 살펴보는 세미나입니다. 실습 시간도 있어요!",
    location: { lat: 37.5007, lng: 127.0365 },
    address: "서울 강남구 역삼동 패스트파이브",
    meetingDate: addDays(6),
    endDate: addDays(6),
    maxAttendees: 25,
    currentAttendees: 22,
    fee: 0,
    status: "SCHEDULED" as const,
    myStatus: null,
    createdBy: DEMO_MEMBERS[3],
    createdAt: "2024-12-09T09:00:00Z",
  },

  // 출사 동호회 정모
  {
    id: 13,
    groupId: 20,
    groupName: "출사 동호회 '렌즈'",
    title: "야경 출사 - 남산타워",
    description:
      "서울의 야경을 담아봐요! 남산타워에서 야경 출사를 진행합니다.",
    location: { lat: 37.5509, lng: 126.9881 },
    address: "서울 중구 남산타워",
    meetingDate: addDays(4),
    endDate: addDays(4),
    maxAttendees: 15,
    currentAttendees: 12,
    fee: 0,
    status: "SCHEDULED" as const,
    myStatus: null,
    createdBy: DEMO_MEMBERS[9],
    createdAt: "2024-12-08T09:00:00Z",
  },

  // 홈쿠킹 클래스 정모
  {
    id: 14,
    groupId: 22,
    groupName: "홈쿠킹 클래스",
    title: "이번 주 메뉴: 파스타 3종",
    description:
      "까르보나라, 알리오올리오, 봉골레 파스타를 함께 만들어봐요!",
    location: { lat: 37.5345, lng: 126.9946 },
    address: "서울 용산구 이태원동 쿠킹스튜디오",
    meetingDate: addDays(5),
    endDate: addDays(5),
    maxAttendees: 10,
    currentAttendees: 9,
    fee: 35000,
    status: "SCHEDULED" as const,
    myStatus: null,
    createdBy: DEMO_MEMBERS[1],
    createdAt: "2024-12-07T09:00:00Z",
  },

  // 강아지 산책 메이트 정모
  {
    id: 15,
    groupId: 29,
    groupName: "강아지 산책 메이트",
    title: "서울숲 단체 산책",
    description:
      "서울숲에서 반려견과 함께 산책해요! 산책 후 카페에서 간식타임도 있습니다.",
    location: { lat: 37.5447, lng: 127.0557 },
    address: "서울 성동구 서울숲 입구",
    meetingDate: addDays(1),
    endDate: addDays(1),
    maxAttendees: 15,
    currentAttendees: 11,
    fee: 0,
    status: "SCHEDULED" as const,
    myStatus: null,
    createdBy: DEMO_MEMBERS[8],
    createdAt: "2024-12-12T09:00:00Z",
  },
];

// MeetingListDTO 형태로 변환
export const DEMO_MEETING_LIST = DEMO_MEETINGS.map((m) => ({
  id: m.id,
  groupId: m.groupId,
  groupName: m.groupName,
  title: m.title,
  address: m.address,
  meetingDate: m.meetingDate,
  maxAttendees: m.maxAttendees,
  currentAttendees: m.currentAttendees,
  status: m.status,
}));

// 추천 모임 (상위 8개)
export const DEMO_RECOMMENDED_GROUPS = DEMO_GROUP_LIST.slice(0, 8);

// 다가오는 정모 (가장 빠른 6개)
export const DEMO_UPCOMING_MEETINGS = DEMO_MEETING_LIST.slice(0, 6);

// 이벤트 배너 데이터
const getBannerImage = (seed: number) =>
  `https://picsum.photos/seed/banner${seed}/1920/384`;

export const DEMO_EVENT_BANNERS = [
  {
    id: 1,
    title: "신규 가입 이벤트",
    description: "다모여에 첫 가입하면 프리미엄 30일 무료!",
    imageUrl: getBannerImage(201),
    linkUrl: "/events/1",
    startDate: "2025-01-01T00:00:00Z",
    endDate: "2025-01-31T23:59:59Z",
    isActive: true,
  },
  {
    id: 2,
    title: "친구 초대 이벤트",
    description: "친구를 초대하고 포인트 받자! 초대할수록 더 많은 혜택!",
    imageUrl: getBannerImage(202),
    linkUrl: "/events/2",
    startDate: "2025-01-01T00:00:00Z",
    endDate: "2025-02-28T23:59:59Z",
    isActive: true,
  },
  {
    id: 3,
    title: "첫 모임 개설 이벤트",
    description: "모임을 처음 만들면 홍보 지원금 5만원 지급!",
    imageUrl: getBannerImage(203),
    linkUrl: "/events/3",
    startDate: "2025-01-01T00:00:00Z",
    endDate: "2025-01-31T23:59:59Z",
    isActive: true,
  },
  {
    id: 4,
    title: "겨울 아웃도어 특집",
    description: "겨울 산행, 스키, 보드 모임을 찾고 계신가요?",
    imageUrl: getBannerImage(204),
    linkUrl: "/events/4",
    startDate: "2025-01-01T00:00:00Z",
    endDate: "2025-02-28T23:59:59Z",
    isActive: true,
  },
  {
    id: 5,
    title: "설날 특별 이벤트",
    description: "설 연휴에도 다모여! 특별 정모에 참여하세요",
    imageUrl: getBannerImage(205),
    linkUrl: "/events/5",
    startDate: "2025-01-20T00:00:00Z",
    endDate: "2025-02-05T23:59:59Z",
    isActive: true,
  },
];

export const DEMO_EVENT_DETAILS = [
  {
    id: 1,
    title: "신규 가입 이벤트",
    description: "다모여에 첫 가입하면 프리미엄 30일 무료!",
    imageUrl: getBannerImage(201),
    linkUrl: "/events/1",
    startDate: "2025-01-01T00:00:00Z",
    endDate: "2025-01-31T23:59:59Z",
    isActive: true,
    content: `
## 신규 가입 이벤트

다모여에 처음 가입하시는 분들께 **프리미엄 멤버십 30일**을 무료로 드립니다!

### 혜택 내용
- 프리미엄 멤버십 30일 무료 체험
- 무제한 모임 가입
- 프리미엄 배지 지급
- 우선 정모 예약권 1회

### 참여 방법
1. 다모여 앱 다운로드
2. 회원가입 완료
3. 프로필 설정 완료
4. 자동으로 프리미엄 멤버십 적용!

### 유의사항
- 본 이벤트는 신규 가입자에 한해 적용됩니다.
- 기존 회원이 탈퇴 후 재가입 시 적용되지 않습니다.
- 이벤트 기간: 2025년 1월 1일 ~ 1월 31일
    `,
    tags: ["신규가입", "프리미엄", "무료체험"],
  },
  {
    id: 2,
    title: "친구 초대 이벤트",
    description: "친구를 초대하고 포인트 받자! 초대할수록 더 많은 혜택!",
    imageUrl: getBannerImage(202),
    linkUrl: "/events/2",
    startDate: "2025-01-01T00:00:00Z",
    endDate: "2025-02-28T23:59:59Z",
    isActive: true,
    content: `
## 친구 초대 이벤트

친구를 초대할수록 포인트가 쌓여요!

### 혜택 내용
| 초대 인원 | 지급 포인트 |
|---------|-----------|
| 1명 | 1,000P |
| 3명 | 5,000P |
| 5명 | 10,000P |
| 10명 | 25,000P |

### 참여 방법
1. 마이페이지 > 친구 초대 메뉴 클릭
2. 초대 링크 복사
3. 친구에게 공유
4. 친구가 가입하면 포인트 지급!

### 유의사항
- 초대받은 친구가 가입 후 첫 모임에 참여해야 포인트가 지급됩니다.
- 포인트는 정모 참가비 결제에 사용 가능합니다.
    `,
    tags: ["친구초대", "포인트", "리워드"],
  },
  {
    id: 3,
    title: "첫 모임 개설 이벤트",
    description: "모임을 처음 만들면 홍보 지원금 5만원 지급!",
    imageUrl: getBannerImage(203),
    linkUrl: "/events/3",
    startDate: "2025-01-01T00:00:00Z",
    endDate: "2025-01-31T23:59:59Z",
    isActive: true,
    content: `
## 첫 모임 개설 이벤트

당신의 취미를 나눌 모임을 만들어보세요!

### 혜택 내용
- 모임 홍보 지원금 **50,000원** 지급
- 다모여 메인 페이지 노출 기회
- 모임 운영 가이드북 제공

### 참여 조건
- 다모여 가입 후 첫 모임 개설
- 모임 설명 100자 이상 작성
- 커버 이미지 등록
- 첫 정모 등록

### 유의사항
- 지원금은 모임 운영에만 사용 가능합니다.
- 허위/부적절한 모임은 지원 대상에서 제외됩니다.
    `,
    tags: ["모임개설", "지원금", "호스트"],
  },
  {
    id: 4,
    title: "겨울 아웃도어 특집",
    description: "겨울 산행, 스키, 보드 모임을 찾고 계신가요?",
    imageUrl: getBannerImage(204),
    linkUrl: "/events/4",
    startDate: "2025-01-01T00:00:00Z",
    endDate: "2025-02-28T23:59:59Z",
    isActive: true,
    content: `
## 겨울 아웃도어 특집

추운 겨울, 더 뜨겁게 즐기자!

### 추천 모임
- 🎿 스키/보드 동호회
- 🏔️ 겨울 산행 모임
- ⛸️ 아이스링크 스케이팅
- 🏕️ 겨울 캠핑 러버스

### 특별 혜택
- 아웃도어 모임 첫 참가비 50% 할인
- 겨울 장비 렌탈 제휴 할인
- 아웃도어 브랜드 기프티콘 추첨

### 참여 방법
카테고리에서 '아웃도어/여행' 선택 후 겨울 관련 모임 검색!
    `,
    tags: ["아웃도어", "겨울", "스키", "등산"],
  },
  {
    id: 5,
    title: "설날 특별 이벤트",
    description: "설 연휴에도 다모여! 특별 정모에 참여하세요",
    imageUrl: getBannerImage(205),
    linkUrl: "/events/5",
    startDate: "2025-01-20T00:00:00Z",
    endDate: "2025-02-05T23:59:59Z",
    isActive: true,
    content: `
## 설날 특별 이벤트

설 연휴, 혼자 심심하신가요? 다모여와 함께하세요!

### 설 연휴 특별 정모
- 🎲 보드게임 올나잇 파티
- 🍜 설날 음식 만들기 클래스
- 🎬 영화 마라톤 모임
- 🚶 새해 소원 한강 산책

### 이벤트 혜택
- 설 연휴 기간 정모 참가비 **30% 할인**
- 세뱃돈 포인트 랜덤 지급 (최대 10,000P)
- 설날 한정 프로필 뱃지

### 기간
2025년 1월 20일 ~ 2월 5일
    `,
    tags: ["설날", "연휴", "특별정모"],
  },
];

// 페이지 응답 생성 헬퍼
export function createPageResponse<T>(
  items: T[],
  page: number = 1,
  size: number = 10
) {
  const startIndex = (page - 1) * size;
  const endIndex = startIndex + size;
  const paginatedItems = items.slice(startIndex, endIndex);
  const totalPage = Math.ceil(items.length / size);

  return {
    dtoList: paginatedItems,
    pageNumList: Array.from({ length: Math.min(totalPage, 10) }, (_, i) => i + 1),
    prev: page > 1,
    next: page < totalPage,
    totalCount: items.length,
    prevPage: page - 1,
    nextPage: page + 1,
    totalPage,
    current: page,
  };
}
