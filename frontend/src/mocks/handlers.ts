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

  // 모임 가입 (즉시 가입)
  http.post("*/api/groups/:groupId/join", async () => {
    await delay(DELAY_MS);
    return HttpResponse.json({ message: "모임에 가입되었습니다." });
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

  // 멤버 역할 변경 (PATCH)
  http.patch("*/api/groups/:groupId/members/:memberId/role", async () => {
    await delay(DELAY_MS);
    return HttpResponse.json({ message: "멤버 역할이 변경되었습니다." });
  }),

  // 멤버 강퇴
  http.delete("*/api/groups/:groupId/members/:memberId", async () => {
    await delay(DELAY_MS);
    return HttpResponse.json({ message: "멤버가 강퇴되었습니다." });
  }),


  // ========== 정모 API ==========
  // 모임의 예정된 정모 목록
  http.get("*/api/meetings/group/:groupId/upcoming", async ({ params }) => {
    await delay(DELAY_MS);
    const groupId = Number(params.groupId);
    const now = new Date();
    const upcomingMeetings = DEMO_MEETING_LIST.filter(
      (m) => m.groupId === groupId && new Date(m.meetingDate) > now && (m.status as string) !== "CANCELLED"
    );
    return HttpResponse.json(upcomingMeetings);
  }),

  // 모임의 지난 정모 목록
  http.get("*/api/meetings/group/:groupId/past", async ({ params }) => {
    await delay(DELAY_MS);
    const groupId = Number(params.groupId);
    const now = new Date();
    const pastMeetings = DEMO_MEETING_LIST.filter(
      (m) => m.groupId === groupId && new Date(m.meetingDate) <= now
    );
    return HttpResponse.json(pastMeetings);
  }),

  // 모임의 전체 정모 목록
  http.get("*/api/meetings/group/:groupId", async ({ params }) => {
    await delay(DELAY_MS);
    const groupId = Number(params.groupId);
    const meetings = DEMO_MEETING_LIST.filter((m) => m.groupId === groupId);
    return HttpResponse.json(meetings);
  }),

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

  // 정모 생성 (POST /api/meetings - 백엔드와 동일한 엔드포인트)
  http.post("*/api/meetings", async () => {
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
  // 로그인 (Spring Security 설정: usernameParameter("email"), passwordParameter("pw"))
  http.post("*/api/member/login", async ({ request }) => {
    await delay(DELAY_MS);
    const formData = await request.formData();
    const email = formData.get("email") as string;
    const password = formData.get("pw") as string;

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
      showJoinedGroups: true,
      roleNames: ["USER"],
    });
  }),

  // 공개 프로필 조회 (다른 회원)
  http.get("*/api/member/:memberId/profile", async ({ params }) => {
    await delay(DELAY_MS);
    const memberId = Number(params.memberId);
    const member = DEMO_MEMBERS.find((m) => m.id === memberId) || DEMO_MEMBERS[0];
    return HttpResponse.json({
      id: member.id,
      nickname: member.nickname,
      profileImage: member.profileImage,
      introduction: "다모여에서 즐겁게 활동 중입니다!",
      address: "서울",
      createdAt: "2024-01-15T00:00:00Z",
      groupCount: 3,
      joinedGroups: DEMO_GROUP_LIST.slice(0, 3).map((g) => ({
        id: g.id,
        name: g.name,
        thumbnailImage: g.thumbnailImage,
        categoryName: g.category.name,
      })),
      showJoinedGroups: true,
    });
  }),

  // 프로필 수정 (PUT /api/member/modify)
  http.put("*/api/member/modify", async () => {
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
  // 알림 목록 (PageResponseDTO 형식 - 백엔드와 동일한 구조)
  http.get("*/api/notifications", async ({ request }) => {
    await delay(DELAY_MS);
    const url = new URL(request.url);
    const page = Number(url.searchParams.get("page")) || 1;
    const size = Number(url.searchParams.get("size")) || 10;

    const dtoList = [
      {
        id: 1,
        type: "NEW_MEMBER",
        title: "새 멤버 가입",
        content: "홍길동님이 강남 러닝 크루에 가입했습니다.",
        referenceId: 1,
        referenceType: "GROUP",
        isRead: false,
        createdAt: new Date(Date.now() - 1000 * 60 * 30).toISOString(),
      },
      {
        id: 2,
        type: "MEETING_REMINDER",
        title: "정모 리마인더",
        content: "내일 '토요일 아침 한강 러닝 10km' 정모가 있습니다.",
        referenceId: 1,
        referenceType: "MEETING",
        isRead: false,
        createdAt: new Date(Date.now() - 1000 * 60 * 60 * 2).toISOString(),
      },
      {
        id: 3,
        type: "NEW_MEETING",
        title: "새 정모 등록",
        content: "React 개발자 모임에 새로운 정모가 등록되었습니다.",
        referenceId: 2,
        referenceType: "MEETING",
        isRead: true,
        createdAt: new Date(Date.now() - 1000 * 60 * 60 * 24).toISOString(),
      },
      {
        id: 4,
        type: "WELCOME",
        title: "환영합니다",
        content: "다모여에 오신 것을 환영합니다!",
        referenceId: null,
        referenceType: "SYSTEM",
        isRead: true,
        createdAt: new Date(Date.now() - 1000 * 60 * 60 * 48).toISOString(),
      },
    ];

    const totalCount = dtoList.length;
    const totalPage = Math.ceil(totalCount / size);
    return HttpResponse.json({
      dtoList,
      pageNumList: Array.from({ length: totalPage }, (_, i) => i + 1),
      prev: page > 1,
      next: page < totalPage,
      totalCount,
      prevPage: page - 1,
      nextPage: page + 1,
      totalPage,
      current: page,
    });
  }),

  // 안 읽은 알림 수 (백엔드 실제 경로: /unread/count)
  http.get("*/api/notifications/unread/count", async () => {
    await delay(DELAY_MS);
    return HttpResponse.json({ count: 2 });
  }),

  // 알림 읽음 처리 (PATCH)
  http.patch("*/api/notifications/read-all", async () => {
    await delay(DELAY_MS);
    return HttpResponse.json({ result: "SUCCESS" });
  }),

  // 알림 개별 읽음/삭제 처리 (PATCH)
  http.patch("*/api/notifications/:id/:action", async () => {
    await delay(DELAY_MS);
    return HttpResponse.json({ result: "SUCCESS" });
  }),

  // ========== 이벤트 API ==========
  // 이벤트 배너 목록
  http.get("*/api/events/banners", async () => {
    await delay(DELAY_MS);
    return HttpResponse.json(DEMO_EVENT_BANNERS);
  }),

  // 전체 이벤트 목록 (관리자용 - 비활성 포함)
  http.get("*/api/events", async () => {
    await delay(DELAY_MS);
    return HttpResponse.json(DEMO_EVENT_DETAILS);
  }),

  // 이벤트 생성 (관리자)
  http.post("*/api/events", async () => {
    await delay(DELAY_MS);
    return HttpResponse.json({ id: DEMO_EVENT_DETAILS.length + 1 });
  }),

  // 이벤트 수정 (관리자)
  http.put("*/api/events/:eventId", async ({ params }) => {
    await delay(DELAY_MS);
    const eventId = Number(params.eventId);
    const event = DEMO_EVENT_DETAILS.find((e) => e.id === eventId);
    return HttpResponse.json(event || DEMO_EVENT_DETAILS[0]);
  }),

  // 이벤트 활성화 토글 (관리자)
  http.patch("*/api/events/:eventId/toggle", async () => {
    await delay(DELAY_MS);
    return HttpResponse.json({ result: "SUCCESS" });
  }),

  // 이벤트 삭제 (관리자)
  http.delete("*/api/events/:eventId", async () => {
    await delay(DELAY_MS);
    return HttpResponse.json({ result: "SUCCESS" });
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

  // ========== 관리자 API ==========
  // 대시보드 통계
  http.get("*/api/admin/dashboard/stats", async () => {
    await delay(DELAY_MS);
    return HttpResponse.json({
      totalMembers: 1234,
      totalGroups: 87,
      totalMeetings: 342,
      todayNewMembers: 12,
      activeGroups: 65,
      upcomingMeetings: 28,
    });
  }),

  // 회원 목록
  http.get("*/api/admin/members", async ({ request }) => {
    await delay(DELAY_MS);
    const url = new URL(request.url);
    const page = Number(url.searchParams.get("page")) || 1;
    const size = Number(url.searchParams.get("size")) || 10;
    const members = DEMO_MEMBERS.map((m, i) => ({
      id: m.id,
      email: `${m.nickname.toLowerCase()}@example.com`,
      nickname: m.nickname,
      profileImage: m.profileImage,
      roleNames: i === 0 ? ["USER", "ADMIN"] : ["USER"],
      social: false,
      createdAt: new Date(Date.now() - i * 7 * 24 * 60 * 60 * 1000).toISOString(),
      groupCount: Math.floor(Math.random() * 5),
      isPremium: i === 1,
      premiumType: i === 1 ? "MONTHLY" : null,
      premiumStartDate: i === 1 ? new Date(Date.now() - 5 * 24 * 60 * 60 * 1000).toISOString() : null,
      premiumEndDate: i === 1 ? new Date(Date.now() + 25 * 24 * 60 * 60 * 1000).toISOString() : null,
      premiumDaysRemaining: i === 1 ? 25 : 0,
    }));
    return HttpResponse.json(createPageResponse(members, page, size));
  }),

  // 회원 역할 변경
  http.patch("*/api/admin/members/:memberId/role", async () => {
    await delay(DELAY_MS);
    return HttpResponse.json({ message: "역할이 변경되었습니다." });
  }),

  // 프리미엄 부여
  http.post("*/api/admin/members/:memberId/premium", async () => {
    await delay(DELAY_MS);
    return HttpResponse.json({ message: "프리미엄이 부여되었습니다." });
  }),

  // 프리미엄 기간 조정
  http.patch("*/api/admin/members/:memberId/premium/adjust", async () => {
    await delay(DELAY_MS);
    return HttpResponse.json({ message: "프리미엄 기간이 조정되었습니다." });
  }),

  // 프리미엄 취소
  http.delete("*/api/admin/members/:memberId/premium", async () => {
    await delay(DELAY_MS);
    return HttpResponse.json({ message: "프리미엄이 취소되었습니다." });
  }),

  // 모임 목록 (관리자)
  http.get("*/api/admin/groups", async ({ request }) => {
    await delay(DELAY_MS);
    const url = new URL(request.url);
    const page = Number(url.searchParams.get("page")) || 1;
    const size = Number(url.searchParams.get("size")) || 10;
    const groups = DEMO_GROUP_LIST.map((g) => ({
      id: g.id,
      name: g.name,
      categoryName: g.category.name,
      ownerNickname: DEMO_MEMBERS[0].nickname,
      ownerEmail: "owner@example.com",
      memberCount: g.memberCount,
      maxMembers: g.maxMembers,
      status: "ACTIVE",
      createdAt: new Date().toISOString(),
    }));
    return HttpResponse.json(createPageResponse(groups, page, size));
  }),

  // 모임 상태 변경 (관리자)
  http.patch("*/api/admin/groups/:groupId/status", async () => {
    await delay(DELAY_MS);
    return HttpResponse.json({ message: "모임 상태가 변경되었습니다." });
  }),

  // 모임 삭제 (관리자)
  http.delete("*/api/admin/groups/:groupId", async () => {
    await delay(DELAY_MS);
    return HttpResponse.json({ message: "모임이 삭제되었습니다." });
  }),

  // ========== 채팅 REST API ==========
  // 내 채팅방 목록
  http.get("*/api/chat/my-chats", async () => {
    await delay(DELAY_MS);
    return HttpResponse.json(
      DEMO_GROUP_LIST.slice(0, 3).map((g) => ({
        groupId: g.id,
        groupName: g.name,
        latestMessage: null,
        unreadCount: 0,
      }))
    );
  }),

  // 그룹 채팅 메시지 히스토리
  http.get("*/api/chat/:groupId/messages", async () => {
    await delay(DELAY_MS);
    return HttpResponse.json({
      dtoList: [],
      pageNumList: [],
      prev: false,
      next: false,
      totalCount: 0,
      prevPage: 0,
      nextPage: 0,
      totalPage: 0,
      current: 1,
    });
  }),

  // 그룹 채팅 읽지 않은 수
  http.get("*/api/chat/:groupId/unread-count", async () => {
    await delay(DELAY_MS);
    return HttpResponse.json(0);
  }),

  // 그룹 채팅 읽음 처리
  http.post("*/api/chat/:groupId/read", async () => {
    await delay(DELAY_MS);
    return HttpResponse.json({ result: "SUCCESS" });
  }),

  // 정모 채팅 메시지 히스토리
  http.get("*/api/chat/meeting/:meetingId/messages", async () => {
    await delay(DELAY_MS);
    return HttpResponse.json({
      dtoList: [],
      pageNumList: [],
      prev: false,
      next: false,
      totalCount: 0,
      prevPage: 0,
      nextPage: 0,
      totalPage: 0,
      current: 1,
    });
  }),

  // 정모 채팅 읽지 않은 수
  http.get("*/api/chat/meeting/:meetingId/unread-count", async () => {
    await delay(DELAY_MS);
    return HttpResponse.json(0);
  }),

  // 정모 채팅 읽음 처리
  http.post("*/api/chat/meeting/:meetingId/read", async () => {
    await delay(DELAY_MS);
    return HttpResponse.json({ result: "SUCCESS" });
  }),

  // ========== 갤러리 API ==========
  // 게시물 목록
  http.get("*/api/groups/:groupId/gallery", async ({ request }) => {
    await delay(DELAY_MS);
    const url = new URL(request.url);
    const page = Number(url.searchParams.get("page")) || 1;
    const size = Number(url.searchParams.get("size")) || 12;
    return HttpResponse.json(createPageResponse([], page, size));
  }),

  // 게시물 개수
  http.get("*/api/groups/:groupId/gallery/count", async () => {
    await delay(DELAY_MS);
    return HttpResponse.json({ count: 0 });
  }),

  // 최신 게시물 미리보기
  http.get("*/api/groups/:groupId/gallery/recent", async () => {
    await delay(DELAY_MS);
    return HttpResponse.json([]);
  }),

  // 게시물 업로드
  http.post("*/api/groups/:groupId/gallery", async () => {
    await delay(DELAY_MS);
    return HttpResponse.json({ id: 1, message: "게시물이 업로드되었습니다." });
  }),

  // 게시물 삭제
  http.delete("*/api/gallery/posts/:postId", async () => {
    await delay(DELAY_MS);
    return HttpResponse.json({ message: "게시물이 삭제되었습니다." });
  }),

  // 게시물 좋아요 토글
  http.post("*/api/gallery/posts/:postId/like", async () => {
    await delay(DELAY_MS);
    return HttpResponse.json({ liked: true, likeCount: 1 });
  }),

  // 댓글 목록
  http.get("*/api/gallery/posts/:postId/comments", async () => {
    await delay(DELAY_MS);
    return HttpResponse.json([]);
  }),

  // 댓글 작성
  http.post("*/api/gallery/posts/:postId/comments", async () => {
    await delay(DELAY_MS);
    return HttpResponse.json({ id: 1, message: "댓글이 작성되었습니다." });
  }),

  // 댓글 삭제
  http.delete("*/api/gallery/comments/:commentId", async () => {
    await delay(DELAY_MS);
    return HttpResponse.json({ message: "댓글이 삭제되었습니다." });
  }),

  // ========== 결제 API ==========
  // 프리미엄 상태 조회
  http.get("*/api/payments/premium-status", async () => {
    await delay(DELAY_MS);
    return HttpResponse.json({
      isPremium: false,
      premiumType: null,
      startDate: null,
      endDate: null,
      daysRemaining: 0,
    });
  }),

  // 결제 통계 (관리자)
  http.get("*/api/payments/stats", async () => {
    await delay(DELAY_MS);
    return HttpResponse.json({
      totalRevenue: 0,
      monthlyRevenue: 0,
      activePremiumCount: 0,
    });
  }),

  // 내 결제 내역
  http.get("*/api/payments/my", async ({ request }) => {
    await delay(DELAY_MS);
    const url = new URL(request.url);
    const page = Number(url.searchParams.get("page")) || 1;
    const size = Number(url.searchParams.get("size")) || 10;
    return HttpResponse.json(createPageResponse([], page, size));
  }),

  // 결제 준비
  http.post("*/api/payments/ready", async () => {
    await delay(DELAY_MS);
    return HttpResponse.json({
      orderId: "demo-order-" + Date.now(),
      nextRedirectPcUrl: "https://example.com/payment",
      nextRedirectMobileUrl: "https://example.com/payment",
      nextRedirectAppUrl: "https://example.com/payment",
      tid: "demo-tid",
    });
  }),

  // 결제 승인
  http.get("*/api/payments/approve", async () => {
    await delay(DELAY_MS);
    return HttpResponse.json({
      id: 1,
      orderId: "demo-order",
      status: "COMPLETED",
      amount: 9900,
      createdAt: new Date().toISOString(),
    });
  }),

  // 결제 취소
  http.get("*/api/payments/cancel", async () => {
    await delay(DELAY_MS);
    return new HttpResponse(null, { status: 204 });
  }),

  // 결제 실패
  http.get("*/api/payments/fail", async () => {
    await delay(DELAY_MS);
    return new HttpResponse(null, { status: 204 });
  }),

  // ========== 상담 API ==========
  // 활성 상담 조회 (진행 중인 상담 없음 = 204)
  http.get("*/api/support/active", async () => {
    await delay(DELAY_MS);
    return new HttpResponse(null, { status: 204 });
  }),

  // 내 상담 목록
  http.get("*/api/support/my-chats", async () => {
    await delay(DELAY_MS);
    return HttpResponse.json([]);
  }),

  // 관리자: 대기 중인 상담 개수
  http.get("*/api/support/admin/waiting-count", async () => {
    await delay(DELAY_MS);
    return HttpResponse.json(0);
  }),

  // 관리자: 대기 중인 상담 목록
  http.get("*/api/support/admin/waiting", async ({ request }) => {
    await delay(DELAY_MS);
    const url = new URL(request.url);
    const page = Number(url.searchParams.get("page")) || 1;
    const size = Number(url.searchParams.get("size")) || 20;
    return HttpResponse.json(createPageResponse([], page, size));
  }),

  // 관리자: 내 담당 상담 목록
  http.get("*/api/support/admin/my-assigned", async ({ request }) => {
    await delay(DELAY_MS);
    const url = new URL(request.url);
    const page = Number(url.searchParams.get("page")) || 1;
    const size = Number(url.searchParams.get("size")) || 20;
    return HttpResponse.json(createPageResponse([], page, size));
  }),

  // 관리자: 전체 상담 목록
  http.get("*/api/support/admin/all", async ({ request }) => {
    await delay(DELAY_MS);
    const url = new URL(request.url);
    const page = Number(url.searchParams.get("page")) || 1;
    const size = Number(url.searchParams.get("size")) || 20;
    return HttpResponse.json(createPageResponse([], page, size));
  }),

  // 관리자: 상담 배정
  http.post("*/api/support/admin/:chatId/assign", async () => {
    await delay(DELAY_MS);
    return HttpResponse.json({ message: "상담이 배정되었습니다." });
  }),

  // 관리자: 상담 완료 처리
  http.post("*/api/support/admin/:chatId/complete", async () => {
    await delay(DELAY_MS);
    return HttpResponse.json({ message: "상담이 완료되었습니다." });
  }),

  // 상담 생성
  http.post("*/api/support", async () => {
    await delay(DELAY_MS);
    return HttpResponse.json({
      id: 1,
      title: "데모 상담",
      status: "WAITING",
      createdAt: new Date().toISOString(),
    });
  }),

  // 상담 상세 조회
  http.get("*/api/support/:chatId", async ({ params }) => {
    await delay(DELAY_MS);
    return HttpResponse.json({
      id: Number(params.chatId),
      title: "데모 상담",
      status: "WAITING",
      createdAt: new Date().toISOString(),
    });
  }),

  // 상담 메시지 조회
  http.get("*/api/support/:chatId/messages", async ({ request }) => {
    await delay(DELAY_MS);
    const url = new URL(request.url);
    const page = Number(url.searchParams.get("page")) || 1;
    const size = Number(url.searchParams.get("size")) || 50;
    return HttpResponse.json(createPageResponse([], page, size));
  }),

  // 상담 평가
  http.post("*/api/support/:chatId/rate", async () => {
    await delay(DELAY_MS);
    return HttpResponse.json({ message: "평가가 등록되었습니다." });
  }),

  // ========== 이메일 인증 API ==========
  // 인증 코드 발송
  http.post("*/api/email/send", async () => {
    await delay(DELAY_MS);
    return HttpResponse.json({ success: true, message: "인증 코드가 발송되었습니다." });
  }),

  // 인증 코드 검증 (데모: 123456 고정)
  http.post("*/api/email/verify", async ({ request }) => {
    await delay(DELAY_MS);
    const body = await request.json() as { email: string; code: string };
    if (body.code === "123456") {
      return HttpResponse.json({ success: true, message: "이메일이 인증되었습니다." });
    }
    return HttpResponse.json({ success: false, message: "인증 코드가 올바르지 않습니다." });
  }),

  // 인증 상태 조회
  http.get("*/api/email/status", async () => {
    await delay(DELAY_MS);
    return HttpResponse.json({ email: "demo@example.com", verified: true });
  }),

  // 이메일 중복 확인
  http.get("*/api/member/check/email", async () => {
    await delay(DELAY_MS);
    return HttpResponse.json({ available: true });
  }),

  // 닉네임 중복 확인
  http.get("*/api/member/check/nickname", async () => {
    await delay(DELAY_MS);
    return HttpResponse.json({ available: true });
  }),
];
