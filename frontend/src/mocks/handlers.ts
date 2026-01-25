// MSW 핸들러 - API 목업

import { http, HttpResponse, delay } from "msw";
import {
  DEMO_GROUPS,
  DEMO_GROUP_LIST,
  DEMO_MEETINGS,
  DEMO_MEETING_LIST,
  DEMO_CATEGORIES,
  DEMO_MEMBERS,
  DEMO_RECOMMENDED_GROUPS,
  DEMO_UPCOMING_MEETINGS,
  DEMO_EVENT_BANNERS,
  DEMO_EVENT_DETAILS,
  createPageResponse,
} from "./data";

// 약간의 지연을 추가하여 실제 API처럼 느껴지게
const DELAY_MS = 300;

export const handlers = [
  // ========== 카테고리 API ==========
  http.get("*/api/categories", async () => {
    await delay(DELAY_MS);
    return HttpResponse.json(DEMO_CATEGORIES);
  }),

  // ========== 모임 API ==========
  // 추천 모임 (먼저 정의해야 /api/groups/:groupId 보다 우선)
  http.get("*/api/groups/recommended", async () => {
    await delay(DELAY_MS);
    return HttpResponse.json(DEMO_RECOMMENDED_GROUPS);
  }),

  // 내 모임
  http.get("*/api/groups/my", async () => {
    await delay(DELAY_MS);
    // 데모용: 랜덤으로 3-5개 모임을 "내 모임"으로 반환
    const myGroups = DEMO_GROUP_LIST.slice(0, 4).map((g) => ({
      ...g,
      myRole: Math.random() > 0.5 ? "OWNER" : "MEMBER",
    }));
    return HttpResponse.json(myGroups);
  }),

  // 근처 모임
  http.get("*/api/groups/nearby", async () => {
    await delay(DELAY_MS);
    // 위치 기반 필터링 시뮬레이션 (실제로는 모든 모임 반환)
    const nearbyGroups = DEMO_GROUP_LIST.slice(0, 10).map((g) => ({
      ...g,
      distance: Math.floor(Math.random() * 3000), // 0~3km
    }));
    return HttpResponse.json(nearbyGroups);
  }),

  // 모임 목록 (검색/필터)
  http.get("*/api/groups", async ({ request }) => {
    await delay(DELAY_MS);
    const url = new URL(request.url);
    const page = Number(url.searchParams.get("page")) || 1;
    const size = Number(url.searchParams.get("size")) || 10;
    const categoryId = url.searchParams.get("categoryId");
    const keyword = url.searchParams.get("keyword");

    let filteredGroups = [...DEMO_GROUP_LIST];

    // 카테고리 필터
    if (categoryId && categoryId !== "0") {
      filteredGroups = filteredGroups.filter(
        (g) => g.category.id === Number(categoryId)
      );
    }

    // 키워드 검색
    if (keyword) {
      const lowerKeyword = keyword.toLowerCase();
      filteredGroups = filteredGroups.filter(
        (g) =>
          g.name.toLowerCase().includes(lowerKeyword) ||
          g.address.toLowerCase().includes(lowerKeyword)
      );
    }

    return HttpResponse.json(createPageResponse(filteredGroups, page, size));
  }),

  // 모임 멤버 목록
  http.get("*/api/groups/:groupId/members", async ({ params }) => {
    await delay(DELAY_MS);
    const groupId = Number(params.groupId);
    const group = DEMO_GROUPS.find((g) => g.id === groupId);

    if (!group) {
      return new HttpResponse(null, { status: 404 });
    }

    // 데모용: 멤버 목록 생성 (GroupMemberDTO 형식)
    const memberCount = Math.min(group.memberCount, DEMO_MEMBERS.length);
    const members = DEMO_MEMBERS.slice(0, memberCount).map((m, index) => ({
      id: index + 1,
      member: {
        id: m.id,
        nickname: m.nickname,
        profileImage: m.profileImage,
      },
      role: index === 0 ? "OWNER" : index < 2 ? "MANAGER" : "MEMBER",
      status: "APPROVED",
      joinedAt: new Date(
        Date.now() - Math.random() * 30 * 24 * 60 * 60 * 1000
      ).toISOString(),
    }));

    return HttpResponse.json(members);
  }),

  // 모임의 정모 목록
  http.get("*/api/groups/:groupId/meetings", async ({ params }) => {
    await delay(DELAY_MS);
    const groupId = Number(params.groupId);
    const meetings = DEMO_MEETING_LIST.filter((m) => m.groupId === groupId);
    return HttpResponse.json(meetings);
  }),

  // 모임 상세
  http.get("*/api/groups/:groupId", async ({ params }) => {
    await delay(DELAY_MS);
    const groupId = Number(params.groupId);
    const group = DEMO_GROUPS.find((g) => g.id === groupId);

    if (!group) {
      return new HttpResponse(null, { status: 404 });
    }

    return HttpResponse.json(group);
  }),

  // 모임 가입
  http.post("*/api/groups/:groupId/join", async () => {
    await delay(DELAY_MS);
    return HttpResponse.json({ message: "가입 신청이 완료되었습니다." });
  }),

  // 모임 탈퇴
  http.post("*/api/groups/:groupId/leave", async () => {
    await delay(DELAY_MS);
    return HttpResponse.json({ message: "탈퇴가 완료되었습니다." });
  }),

  // 모임 생성
  http.post("*/api/groups", async () => {
    await delay(DELAY_MS);
    const newId = DEMO_GROUPS.length + 1;
    return HttpResponse.json({ id: newId, message: "모임이 생성되었습니다." });
  }),

  // 모임 수정
  http.put("*/api/groups/:groupId", async () => {
    await delay(DELAY_MS);
    return HttpResponse.json({ message: "모임이 수정되었습니다." });
  }),

  // 모임 삭제
  http.delete("*/api/groups/:groupId", async () => {
    await delay(DELAY_MS);
    return HttpResponse.json({ message: "모임이 삭제되었습니다." });
  }),

  // 멤버 역할 변경
  http.put("*/api/groups/:groupId/members/:memberId", async () => {
    await delay(DELAY_MS);
    return HttpResponse.json({ message: "멤버 역할이 변경되었습니다." });
  }),

  // 멤버 강퇴
  http.delete("*/api/groups/:groupId/members/:memberId", async () => {
    await delay(DELAY_MS);
    return HttpResponse.json({ message: "멤버가 강퇴되었습니다." });
  }),

  // 가입 승인
  http.post("*/api/groups/:groupId/members/:memberId/approve", async () => {
    await delay(DELAY_MS);
    return HttpResponse.json({ message: "가입이 승인되었습니다." });
  }),

  // 가입 거절
  http.post("*/api/groups/:groupId/members/:memberId/reject", async () => {
    await delay(DELAY_MS);
    return HttpResponse.json({ message: "가입이 거절되었습니다." });
  }),

  // ========== 정모 API ==========
  // 다가오는 정모
  http.get("*/api/meetings/upcoming", async () => {
    await delay(DELAY_MS);
    return HttpResponse.json(DEMO_UPCOMING_MEETINGS);
  }),

  // 내 정모
  http.get("*/api/meetings/my", async () => {
    await delay(DELAY_MS);
    // 데모용: 일부 정모를 "내 정모"로 반환
    const myMeetings = DEMO_MEETING_LIST.slice(0, 3).map((m) => ({
      ...m,
      myStatus: "ATTENDING",
    }));
    return HttpResponse.json(myMeetings);
  }),

  // 정모 참석자 목록
  http.get("*/api/meetings/:meetingId/attendees", async ({ params }) => {
    await delay(DELAY_MS);
    const meetingId = Number(params.meetingId);
    const meeting = DEMO_MEETINGS.find((m) => m.id === meetingId);

    if (!meeting) {
      return new HttpResponse(null, { status: 404 });
    }

    // 데모용: 참석자 목록 생성 (MeetingAttendeeDTO 형식)
    const attendeeCount = Math.min(
      meeting.currentAttendees,
      DEMO_MEMBERS.length
    );
    const attendees = DEMO_MEMBERS.slice(0, attendeeCount).map((m, index) => ({
      id: index + 1,
      member: {
        id: m.id,
        nickname: m.nickname,
        profileImage: m.profileImage,
      },
      status: "ATTENDING",
      registeredAt: new Date(
        Date.now() - Math.random() * 7 * 24 * 60 * 60 * 1000
      ).toISOString(),
    }));

    return HttpResponse.json(attendees);
  }),

  // 정모 상세
  http.get("*/api/meetings/:meetingId", async ({ params }) => {
    await delay(DELAY_MS);
    const meetingId = Number(params.meetingId);
    const meeting = DEMO_MEETINGS.find((m) => m.id === meetingId);

    if (!meeting) {
      return new HttpResponse(null, { status: 404 });
    }

    return HttpResponse.json(meeting);
  }),

  // 정모 참석 등록
  http.post("*/api/meetings/:meetingId/attend", async () => {
    await delay(DELAY_MS);
    return HttpResponse.json({ message: "참석 등록이 완료되었습니다." });
  }),

  // 정모 참석 취소
  http.delete("*/api/meetings/:meetingId/attend", async () => {
    await delay(DELAY_MS);
    return HttpResponse.json({ message: "참석이 취소되었습니다." });
  }),

  // 정모 생성
  http.post("*/api/groups/:groupId/meetings", async () => {
    await delay(DELAY_MS);
    const newId = DEMO_MEETINGS.length + 1;
    return HttpResponse.json({ id: newId, message: "정모가 생성되었습니다." });
  }),

  // 정모 수정
  http.put("*/api/meetings/:meetingId", async () => {
    await delay(DELAY_MS);
    return HttpResponse.json({ message: "정모가 수정되었습니다." });
  }),

  // 정모 취소
  http.delete("*/api/meetings/:meetingId", async () => {
    await delay(DELAY_MS);
    return HttpResponse.json({ message: "정모가 취소되었습니다." });
  }),

  // ========== 인증 API ==========
  // 로그인
  http.post("*/api/member/login", async ({ request }) => {
    await delay(DELAY_MS);
    const formData = await request.formData();
    const email = formData.get("username") as string;
    const password = formData.get("password") as string;

    // 데모용: 아무 이메일/비밀번호나 허용
    if (email && password) {
      return HttpResponse.json({
        email,
        nickname: email.split("@")[0],
        profileImage: `https://picsum.photos/seed/${email}/200/200`,
        accessToken: "demo-access-token-" + Date.now(),
        refreshToken: "demo-refresh-token-" + Date.now(),
        roleNames: ["USER"],
        social: false,
      });
    }

    return HttpResponse.json(
      { error: "이메일 또는 비밀번호가 올바르지 않습니다." },
      { status: 401 }
    );
  }),

  // 토큰 갱신
  http.get("*/api/member/refresh", async () => {
    await delay(DELAY_MS);
    return HttpResponse.json({
      accessToken: "demo-access-token-refreshed-" + Date.now(),
      refreshToken: "demo-refresh-token-refreshed-" + Date.now(),
    });
  }),

  // 회원가입
  http.post("*/api/member/signup", async () => {
    await delay(DELAY_MS);
    return HttpResponse.json({ message: "회원가입이 완료되었습니다." });
  }),

  // 프로필 조회
  http.get("*/api/member/profile", async () => {
    await delay(DELAY_MS);
    return HttpResponse.json({
      email: "demo@example.com",
      nickname: "데모사용자",
      profileImage: "https://picsum.photos/seed/demo/200/200",
      introduction: "다모여에서 다양한 모임을 즐기고 있습니다!",
      location: { lat: 37.5007, lng: 127.0365 },
      address: "서울 강남구",
      interests: DEMO_CATEGORIES.slice(0, 3),
      createdAt: "2024-01-01T00:00:00Z",
    });
  }),

  // 프로필 수정
  http.put("*/api/member/profile", async () => {
    await delay(DELAY_MS);
    return HttpResponse.json({ message: "프로필이 수정되었습니다." });
  }),

  // 프로필 이미지 업로드
  http.post("*/api/member/profile/image", async () => {
    await delay(DELAY_MS);
    // 데모용: 랜덤 이미지 URL 반환
    const randomId = Math.floor(Math.random() * 1000);
    return HttpResponse.json({
      imageUrl: `https://picsum.photos/seed/${randomId}/200/200`,
    });
  }),

  // 위치 업데이트
  http.put("*/api/member/location", async () => {
    await delay(DELAY_MS);
    return HttpResponse.json({ message: "위치가 업데이트되었습니다." });
  }),

  // ========== 알림 API ==========
  // 알림 목록
  http.get("*/api/notifications", async () => {
    await delay(DELAY_MS);
    const notifications = [
      {
        id: 1,
        type: "GROUP_JOIN_APPROVED",
        title: "모임 가입 승인",
        message: "강남 러닝 크루 모임 가입이 승인되었습니다.",
        read: false,
        createdAt: new Date(Date.now() - 1000 * 60 * 30).toISOString(),
      },
      {
        id: 2,
        type: "MEETING_REMINDER",
        title: "정모 알림",
        message: "내일 '토요일 아침 한강 러닝 10km' 정모가 있습니다.",
        read: false,
        createdAt: new Date(Date.now() - 1000 * 60 * 60 * 2).toISOString(),
      },
      {
        id: 3,
        type: "NEW_MEETING",
        title: "새 정모 등록",
        message: "React 개발자 모임에 새로운 정모가 등록되었습니다.",
        read: true,
        createdAt: new Date(Date.now() - 1000 * 60 * 60 * 24).toISOString(),
      },
      {
        id: 4,
        type: "GROUP_ANNOUNCEMENT",
        title: "모임 공지",
        message: "월간 북클럽 1월 도서가 선정되었습니다: '아몬드'",
        read: true,
        createdAt: new Date(Date.now() - 1000 * 60 * 60 * 48).toISOString(),
      },
    ];
    return HttpResponse.json(notifications);
  }),

  // 안 읽은 알림 수
  http.get("*/api/notifications/unread-count", async () => {
    await delay(DELAY_MS);
    return HttpResponse.json({ count: 2 });
  }),

  // 알림 읽음 처리
  http.put("*/api/notifications/:id/read", async () => {
    await delay(DELAY_MS);
    return HttpResponse.json({ message: "읽음 처리되었습니다." });
  }),

  // 전체 읽음 처리
  http.put("*/api/notifications/read-all", async () => {
    await delay(DELAY_MS);
    return HttpResponse.json({ message: "모든 알림이 읽음 처리되었습니다." });
  }),

  // ========== 이벤트 API ==========
  // 이벤트 배너 목록
  http.get("*/api/events/banners", async () => {
    await delay(DELAY_MS);
    return HttpResponse.json(DEMO_EVENT_BANNERS);
  }),

  // 이벤트 상세
  http.get("*/api/events/:eventId", async ({ params }) => {
    await delay(DELAY_MS);
    const eventId = Number(params.eventId);
    const event = DEMO_EVENT_DETAILS.find((e) => e.id === eventId);

    if (!event) {
      return new HttpResponse(null, { status: 404 });
    }

    return HttpResponse.json(event);
  }),
];
