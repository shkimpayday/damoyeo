-- ============================================================
-- 다모여 (DaMoYeo) 포트폴리오 데모 데이터 시드
-- ============================================================
--
-- [실행 순서]
-- 1. 앱 재시작 (DataInitializer가 demo/admin 계정 생성 + 비밀번호 수정)
-- 2. MariaDB 접속 후 이 파일 실행:
--
--    # 컨테이너 접속
--    docker exec -it <mariadb_container_name> mysql -u damoyeo -pdamoyeo damoyeo
--
--    # 파일 직접 실행 (호스트에서)
--    docker exec -i <mariadb_container_name> mysql -u damoyeo -pdamoyeo damoyeo < seed.sql
--
-- [비밀번호 전략]
-- 추가 데모 사용자의 비밀번호는 demo@damoyeo.store 계정과 동일(demo1234).
-- BCrypt 해시를 직접 계산하지 않고 기존 계정 해시를 참조합니다.
--
-- [주의]
-- 이 스크립트는 한 번만 실행하세요. 중복 실행 시 데이터가 추가됩니다.
-- club, meeting, chat_message, gallery_post 테이블은 유니크 제약이 없어 INSERT IGNORE 미사용.
-- ============================================================

-- ============================================================
-- STEP 1. 기존 계정 ID 및 비밀번호 해시 가져오기
-- ============================================================

SET @admin_id  = (SELECT id FROM member WHERE email = 'admin@damoyeo.store');
SET @demo_id   = (SELECT id FROM member WHERE email = 'demo@damoyeo.store');
SET @demo_pw   = (SELECT password FROM member WHERE email = 'demo@damoyeo.store');

-- ============================================================
-- STEP 2. 추가 데모 멤버 생성 (비밀번호: demo1234)
-- ============================================================

INSERT IGNORE INTO member
    (email, password, nickname, profile_image, introduction, social, show_joined_groups, created_at, modified_at)
VALUES
    ('runner@damoyeo.store',   @demo_pw, '러닝메이트',   'https://picsum.photos/seed/m1/200/200',
     '매일 아침 달리기를 즐깁니다 🏃‍♂️', 0, 1, NOW(), NOW()),
    ('reader@damoyeo.store',   @demo_pw, '책읽는사람',   'https://picsum.photos/seed/m2/200/200',
     '한 달에 책 3권 읽기가 목표입니다 📚', 0, 1, NOW(), NOW()),
    ('devman@damoyeo.store',   @demo_pw, '개발자민수',   'https://picsum.photos/seed/m3/200/200',
     '백엔드 개발자 5년차입니다 💻', 0, 1, NOW(), NOW()),
    ('musician@damoyeo.store', @demo_pw, '기타리스트',   'https://picsum.photos/seed/m4/200/200',
     '인디 기타를 10년째 치고 있습니다 🎸', 0, 1, NOW(), NOW()),
    ('cook@damoyeo.store',     @demo_pw, '요리왕김철수', 'https://picsum.photos/seed/m5/200/200',
     '요리는 나의 힐링입니다 🍳', 0, 1, NOW(), NOW()),
    ('biker@damoyeo.store',    @demo_pw, '라이딩박',     'https://picsum.photos/seed/m6/200/200',
     '자전거로 전국 일주가 꿈입니다 🚴', 0, 1, NOW(), NOW());

-- 역할 부여 (USER)
INSERT IGNORE INTO member_role (member_id, member_role_list)
SELECT id, 'USER' FROM member
WHERE email IN (
    'runner@damoyeo.store', 'reader@damoyeo.store', 'devman@damoyeo.store',
    'musician@damoyeo.store', 'cook@damoyeo.store', 'biker@damoyeo.store'
);

-- 추가된 멤버 ID 캐싱
SET @runner_id   = (SELECT id FROM member WHERE email = 'runner@damoyeo.store');
SET @reader_id   = (SELECT id FROM member WHERE email = 'reader@damoyeo.store');
SET @devman_id   = (SELECT id FROM member WHERE email = 'devman@damoyeo.store');
SET @musician_id = (SELECT id FROM member WHERE email = 'musician@damoyeo.store');
SET @cook_id     = (SELECT id FROM member WHERE email = 'cook@damoyeo.store');
SET @biker_id    = (SELECT id FROM member WHERE email = 'biker@damoyeo.store');

-- 카테고리 ID 캐싱 (DataInitializer가 생성한 순서 기반)
SET @cat_sports   = (SELECT id FROM category WHERE name = '운동/스포츠');
SET @cat_outdoor  = (SELECT id FROM category WHERE name = '아웃도어/여행');
SET @cat_music    = (SELECT id FROM category WHERE name = '음악/악기');
SET @cat_books    = (SELECT id FROM category WHERE name = '독서');
SET @cat_cooking  = (SELECT id FROM category WHERE name = '요리');
SET @cat_it       = (SELECT id FROM category WHERE name = 'IT/개발');

-- ============================================================
-- STEP 3. 샘플 모임(club) 6개 생성
-- ============================================================

INSERT INTO club
    (name, description, category_id, cover_image, location, latitude, longitude,
     max_members, is_public, status, owner_id, created_at, modified_at)
VALUES
    -- 1. 운동/스포츠
    ('강남 새벽 러닝 크루',
     '매주 토/일 새벽 6시, 한강 반포대교 근처에서 5km~10km를 함께 달립니다.\n초보자 환영! 완주보다 함께 달리는 즐거움을 나누는 모임입니다. 🏃‍♀️🏃‍♂️\n\n• 매월 정기 러닝 외에 계절별 마라톤 대회 단체 참가\n• 초보자를 위한 페이스 그룹 운영\n• 달리기 후 브런치 함께해요!',
     @cat_sports,
     'https://picsum.photos/seed/running1/800/400',
     '서울 서초구 반포한강공원',
     37.5125, 127.0220,
     30, 1, 'ACTIVE', @runner_id, NOW(), NOW()),

    -- 2. IT/개발
    ('판교 백엔드 개발자 스터디',
     '현직 백엔드 개발자들의 기술 스터디 모임입니다. 💻\n매주 수요일 저녁, 판교 카페에서 진행됩니다.\n\n• 주간 기술 아티클 공유 및 토론\n• Spring Boot, JPA, Redis, Kafka 등 실무 주제\n• 사이드 프로젝트 팀 매칭\n• 알고리즘 / 코딩 인터뷰 스터디 병행',
     @cat_it,
     'https://picsum.photos/seed/coding1/800/400',
     '경기도 성남시 분당구 판교역로',
     37.3948, 127.1109,
     20, 1, 'ACTIVE', @devman_id, NOW(), NOW()),

    -- 3. 음악/악기
    ('홍대 인디 기타 클럽',
     '어쿠스틱, 일렉, 베이스 모두 환영하는 인디 기타 동호회입니다. 🎸\n매월 2회 홍대 공연장에서 정기 합주를 진행합니다.\n\n• 수준별 그룹 (초급/중급/고급)\n• 오리지널 곡 작업 팀도 운영 중\n• 연말 소규모 버스킹 공연 예정',
     @cat_music,
     'https://picsum.photos/seed/guitar1/800/400',
     '서울 마포구 홍대입구역',
     37.5565, 126.9239,
     25, 1, 'ACTIVE', @musician_id, NOW(), NOW()),

    -- 4. 독서
    ('북서울 독서 모임',
     '한 달에 한 권, 함께 읽고 이야기 나누는 독서 모임입니다. 📚\n매월 마지막 토요일 오후 2시, 노원 카페에서 만납니다.\n\n• 다음 달 책은 구성원 투표로 결정\n• 장르 제한 없음 (소설, 에세이, 자기계발, 인문학 등)\n• 독서 후 맛집 탐방도 함께해요',
     @cat_books,
     'https://picsum.photos/seed/books1/800/400',
     '서울 노원구 노원역',
     37.6551, 127.0675,
     15, 1, 'ACTIVE', @reader_id, NOW(), NOW()),

    -- 5. 요리
    ('마포 홈쿡 클래스',
     '매월 다른 나라 요리를 배우는 홈쿡 모임입니다! 🍳\n이탈리안, 태국식, 일본 가정식... 맛있는 요리를 함께 만들어요.\n\n• 월 2회 연남동 쉐어 키친에서 진행\n• 재료비 인당 15,000~20,000원\n• 초보자도 OK, 요리 선생님이 함께합니다',
     @cat_cooking,
     'https://picsum.photos/seed/cooking1/800/400',
     '서울 마포구 연남동',
     37.5602, 126.9220,
     12, 1, 'ACTIVE', @cook_id, NOW(), NOW()),

    -- 6. 아웃도어/여행
    ('한강 주말 라이딩',
     '주말마다 한강변을 따라 자전거 라이딩하는 모임입니다. 🚴‍♂️\n여의도에서 출발해 암사대교까지 왕복 40km 코스가 기본!\n\n• 매주 토요일 오전 8시 여의도 출발\n• 자전거 종류 무관 (로드, MTB, 하이브리드 모두 가능)\n• 안전모 착용 필수\n• 초보자를 위한 단거리 코스 별도 운영',
     @cat_outdoor,
     'https://picsum.photos/seed/cycling1/800/400',
     '서울 영등포구 여의도한강공원',
     37.5280, 126.9319,
     40, 1, 'ACTIVE', @biker_id, NOW(), NOW());

-- 모임 ID 캐싱 (name으로 조회)
SET @g1_running  = (SELECT id FROM club WHERE name = '강남 새벽 러닝 크루'     AND owner_id = @runner_id);
SET @g2_dev      = (SELECT id FROM club WHERE name = '판교 백엔드 개발자 스터디' AND owner_id = @devman_id);
SET @g3_music    = (SELECT id FROM club WHERE name = '홍대 인디 기타 클럽'     AND owner_id = @musician_id);
SET @g4_books    = (SELECT id FROM club WHERE name = '북서울 독서 모임'         AND owner_id = @reader_id);
SET @g5_cooking  = (SELECT id FROM club WHERE name = '마포 홈쿡 클래스'         AND owner_id = @cook_id);
SET @g6_cycling  = (SELECT id FROM club WHERE name = '한강 주말 라이딩'         AND owner_id = @biker_id);

-- ============================================================
-- STEP 4. 모임 멤버(group_member) 등록
-- ============================================================
-- 각 모임: OWNER 1 + 일반 멤버 3~4 (demo 계정 포함)

INSERT IGNORE INTO group_member (group_id, member_id, role, status, created_at, modified_at)
VALUES
    -- 1. 러닝 크루
    (@g1_running, @runner_id,   'OWNER',  'APPROVED', NOW(), NOW()),
    (@g1_running, @demo_id,     'MEMBER', 'APPROVED', NOW(), NOW()),
    (@g1_running, @reader_id,   'MEMBER', 'APPROVED', NOW(), NOW()),
    (@g1_running, @devman_id,   'MEMBER', 'APPROVED', NOW(), NOW()),
    (@g1_running, @biker_id,    'MEMBER', 'APPROVED', NOW(), NOW()),

    -- 2. 개발자 스터디
    (@g2_dev,   @devman_id,   'OWNER',   'APPROVED', NOW(), NOW()),
    (@g2_dev,   @demo_id,     'MEMBER',  'APPROVED', NOW(), NOW()),
    (@g2_dev,   @runner_id,   'MEMBER',  'APPROVED', NOW(), NOW()),
    (@g2_dev,   @musician_id, 'MANAGER', 'APPROVED', NOW(), NOW()),

    -- 3. 기타 클럽
    (@g3_music, @musician_id, 'OWNER',  'APPROVED', NOW(), NOW()),
    (@g3_music, @demo_id,     'MEMBER', 'APPROVED', NOW(), NOW()),
    (@g3_music, @biker_id,    'MEMBER', 'APPROVED', NOW(), NOW()),
    (@g3_music, @cook_id,     'MEMBER', 'APPROVED', NOW(), NOW()),

    -- 4. 독서 모임
    (@g4_books, @reader_id,   'OWNER',  'APPROVED', NOW(), NOW()),
    (@g4_books, @demo_id,     'MEMBER', 'APPROVED', NOW(), NOW()),
    (@g4_books, @cook_id,     'MEMBER', 'APPROVED', NOW(), NOW()),
    (@g4_books, @musician_id, 'MEMBER', 'APPROVED', NOW(), NOW()),
    (@g4_books, @admin_id,    'MEMBER', 'APPROVED', NOW(), NOW()),

    -- 5. 홈쿡 클래스
    (@g5_cooking, @cook_id,   'OWNER',  'APPROVED', NOW(), NOW()),
    (@g5_cooking, @demo_id,   'MEMBER', 'APPROVED', NOW(), NOW()),
    (@g5_cooking, @reader_id, 'MEMBER', 'APPROVED', NOW(), NOW()),
    (@g5_cooking, @biker_id,  'MEMBER', 'APPROVED', NOW(), NOW()),

    -- 6. 라이딩
    (@g6_cycling, @biker_id,   'OWNER',  'APPROVED', NOW(), NOW()),
    (@g6_cycling, @demo_id,    'MEMBER', 'APPROVED', NOW(), NOW()),
    (@g6_cycling, @runner_id,  'MANAGER','APPROVED', NOW(), NOW()),
    (@g6_cycling, @musician_id,'MEMBER', 'APPROVED', NOW(), NOW()),
    (@g6_cycling, @admin_id,   'MEMBER', 'APPROVED', NOW(), NOW());

-- ============================================================
-- STEP 5. 정모(meeting) 생성 — 모임당 예정 2개 + 지난 정모 2개
-- ============================================================

INSERT INTO meeting
    (group_id, title, description, location, latitude, longitude,
     meeting_date, max_attendees, fee, status, creator_id, created_at, modified_at)
VALUES
    -- ▼ 러닝 크루 ▼
    (@g1_running, '5월 정기 러닝 (지난)',
     '반포대교 북단에서 잠수교까지 왕복 7km 달렸습니다.',
     '서울 서초구 반포한강공원 안내센터 앞', 37.5125, 127.0220,
     DATE_SUB(NOW(), INTERVAL 45 DAY), 20, 0, 'COMPLETED', @runner_id, NOW(), NOW()),

    (@g1_running, '4월 벚꽃 런 (지난)',
     '석촌호수 벚꽃 런! 봄의 절정을 달리며 즐겼습니다.',
     '서울 송파구 석촌호수 동쪽 광장', 37.5057, 127.1005,
     DATE_SUB(NOW(), INTERVAL 75 DAY), 20, 0, 'COMPLETED', @runner_id, NOW(), NOW()),

    (@g1_running, '6월 첫째주 새벽 런',
     '한강 반포 코스 10km! 새벽 공기 마시며 달려요 🌅\n페이스 무관, 완주가 목표!',
     '서울 서초구 반포한강공원 안내센터 앞', 37.5125, 127.0220,
     DATE_ADD(NOW(), INTERVAL 7 DAY), 20, 0, 'SCHEDULED', @runner_id, NOW(), NOW()),

    (@g1_running, '6월 야간 런 & 치맥 🍺',
     '저녁 7시 야간 런 후 한강 치맥 파티! 5km 가볍게 달리고 맛있는 것 먹어요.',
     '서울 서초구 반포한강공원 주차장 B구역', 37.5120, 127.0235,
     DATE_ADD(NOW(), INTERVAL 21 DAY), 25, 0, 'SCHEDULED', @runner_id, NOW(), NOW()),

    -- ▼ 개발자 스터디 ▼
    (@g2_dev, 'Spring Boot 3.x 마이그레이션 실습 (지난)',
     'Spring Boot 2 → 3 마이그레이션 경험 공유 및 실습을 진행했습니다.',
     '경기도 성남시 분당구 카카오 판교아지트 근처 카페', 37.3943, 127.1106,
     DATE_SUB(NOW(), INTERVAL 30 DAY), 12, 0, 'COMPLETED', @devman_id, NOW(), NOW()),

    (@g2_dev, 'JPA N+1 문제와 QueryDSL 해결책 (지난)',
     'JPA 성능 이슈 사례와 QueryDSL 동적 쿼리 최적화 방법을 발표했습니다.',
     '경기도 성남시 분당구 스타벅스 판교점', 37.3950, 127.1112,
     DATE_SUB(NOW(), INTERVAL 60 DAY), 12, 0, 'COMPLETED', @devman_id, NOW(), NOW()),

    (@g2_dev, 'Redis 캐싱 전략 & 분산 락',
     '실무에서 쓰는 Redis 캐싱 패턴과 분산 락 구현 방법을 함께 공부합니다.\n발표자: 개발자민수',
     '경기도 성남시 분당구 카카오 판교아지트 근처 카페', 37.3943, 127.1106,
     DATE_ADD(NOW(), INTERVAL 10 DAY), 12, 0, 'SCHEDULED', @devman_id, NOW(), NOW()),

    (@g2_dev, 'Kafka 입문 & 이벤트 드리븐 아키텍처',
     'Kafka 기초부터 실무 적용 패턴까지 차근차근 알아봅니다.\n실습 코드는 GitHub에 공유 예정.',
     '경기도 성남시 분당구 스타벅스 판교점', 37.3950, 127.1112,
     DATE_ADD(NOW(), INTERVAL 24 DAY), 15, 0, 'SCHEDULED', @devman_id, NOW(), NOW()),

    -- ▼ 기타 클럽 ▼
    (@g3_music, '4월 정기 합주 (지난)',
     '이번 달 연습곡: 넬 - 기억을 걷는 시간, 잔나비 - 주저하는 연인들을 위해',
     '서울 마포구 홍대 스튜디오H', 37.5565, 126.9230,
     DATE_SUB(NOW(), INTERVAL 40 DAY), 15, 10000, 'COMPLETED', @musician_id, NOW(), NOW()),

    (@g3_music, '3월 버스킹 연습 (지난)',
     '5월 홍대 버스킹을 위한 사전 연습 모임이었습니다. 화음 맞추기 집중!',
     '서울 마포구 홍대입구역 6번 출구 앞', 37.5565, 126.9239,
     DATE_SUB(NOW(), INTERVAL 70 DAY), 10, 0, 'COMPLETED', @musician_id, NOW(), NOW()),

    (@g3_music, '6월 정기 합주 & 신입 오리엔테이션',
     '이번 달 연습곡: IU - 좋은 날, 에픽하이 - 우산\n신입 멤버 3명과 함께하는 특별한 합주!',
     '서울 마포구 홍대 스튜디오H', 37.5565, 126.9230,
     DATE_ADD(NOW(), INTERVAL 5 DAY), 15, 10000, 'SCHEDULED', @musician_id, NOW(), NOW()),

    (@g3_music, '홍대 버스킹 공연 🎵',
     '드디어 버스킹 공연날! 홍대 걷고싶은거리에서 저녁 7시 공연 예정입니다.\n응원 오실 분도 환영해요!',
     '서울 마포구 어울마당로 홍대 걷고싶은거리', 37.5557, 126.9244,
     DATE_ADD(NOW(), INTERVAL 28 DAY), 20, 0, 'SCHEDULED', @musician_id, NOW(), NOW()),

    -- ▼ 독서 모임 ▼
    (@g4_books, '5월 독서 토론 - 채식주의자 (지난)',
     '한강 작가의 채식주의자를 함께 읽고 깊은 이야기를 나눴습니다.',
     '서울 노원구 블루보틀 노원점', 37.6558, 127.0668,
     DATE_SUB(NOW(), INTERVAL 35 DAY), 10, 0, 'COMPLETED', @reader_id, NOW(), NOW()),

    (@g4_books, '4월 독서 토론 - 아몬드 (지난)',
     '손원평 작가의 아몬드. 감정에 관한 다양한 시각을 나눴습니다.',
     '서울 노원구 블루보틀 노원점', 37.6558, 127.0668,
     DATE_SUB(NOW(), INTERVAL 65 DAY), 10, 0, 'COMPLETED', @reader_id, NOW(), NOW()),

    (@g4_books, '6월 독서 토론 - 불편한 편의점',
     '이번 달 책: 불편한 편의점 (김호연)\n상반기 베스트셀러! 함께 읽어봐요 📖',
     '서울 노원구 블루보틀 노원점', 37.6558, 127.0668,
     DATE_ADD(NOW(), INTERVAL 14 DAY), 10, 0, 'SCHEDULED', @reader_id, NOW(), NOW()),

    (@g4_books, '7월 독서 토론 - 소년이 온다',
     '7월 책은 투표 결과 한강 작가의 소년이 온다로 결정!\n노벨상 작가의 작품을 깊이 읽어봅니다.',
     '서울 노원구 인디고 서원 카페', 37.6549, 127.0672,
     DATE_ADD(NOW(), INTERVAL 45 DAY), 12, 0, 'SCHEDULED', @reader_id, NOW(), NOW()),

    -- ▼ 홈쿡 클래스 ▼
    (@g5_cooking, '5월 이탈리안 쿠킹 (지난)',
     '생면 파스타와 티라미수를 직접 만들었습니다. 모두들 너무 맛있게 드셨어요!',
     '서울 마포구 연남동 쉐어 키친', 37.5602, 126.9220,
     DATE_SUB(NOW(), INTERVAL 25 DAY), 8, 18000, 'COMPLETED', @cook_id, NOW(), NOW()),

    (@g5_cooking, '4월 일본 가정식 (지난)',
     '카레 라이스, 된장국, 타마고야키 만들기. 모두 완벽하게 완성!',
     '서울 마포구 연남동 쉐어 키친', 37.5602, 126.9220,
     DATE_SUB(NOW(), INTERVAL 55 DAY), 8, 15000, 'COMPLETED', @cook_id, NOW(), NOW()),

    (@g5_cooking, '6월 태국 요리 클래스',
     '팟타이, 똠양꿍, 망고 찹쌀밥을 만들어봅니다! 🌶️\n재료비: 1인 18,000원 (현장 결제)',
     '서울 마포구 연남동 쉐어 키친', 37.5602, 126.9220,
     DATE_ADD(NOW(), INTERVAL 9 DAY), 8, 18000, 'SCHEDULED', @cook_id, NOW(), NOW()),

    (@g5_cooking, '7월 멕시칸 나이트 🌮',
     '타코, 과카몰리, 살사소스 직접 만들기!\n매운 걸 좋아하시는 분들 환영합니다.',
     '서울 마포구 연남동 쉐어 키친', 37.5602, 126.9220,
     DATE_ADD(NOW(), INTERVAL 39 DAY), 8, 18000, 'SCHEDULED', @cook_id, NOW(), NOW()),

    -- ▼ 라이딩 ▼
    (@g6_cycling, '5월 한강 풀코스 라이딩 (지난)',
     '여의도 → 암사대교 왕복 40km 완주! 날씨도 좋고 최고였습니다.',
     '서울 영등포구 여의도한강공원 자전거 대여소 앞', 37.5280, 126.9319,
     DATE_SUB(NOW(), INTERVAL 20 DAY), 25, 0, 'COMPLETED', @biker_id, NOW(), NOW()),

    (@g6_cycling, '4월 봄 라이딩 & 벚꽃 구경 (지난)',
     '여의도 벚꽃길 라이딩! 봄에만 할 수 있는 특별한 코스였습니다.',
     '서울 영등포구 여의도 윤중로', 37.5274, 126.9352,
     DATE_SUB(NOW(), INTERVAL 50 DAY), 30, 0, 'COMPLETED', @biker_id, NOW(), NOW()),

    (@g6_cycling, '6월 새벽 라이딩 (45km 코스)',
     '여의도 → 잠실 → 광진교 왕복 45km.\n오전 7시 30분 여의도 집결, 일정 완주 후 단체 조식!',
     '서울 영등포구 여의도한강공원 자전거 대여소 앞', 37.5280, 126.9319,
     DATE_ADD(NOW(), INTERVAL 6 DAY), 25, 0, 'SCHEDULED', @biker_id, NOW(), NOW()),

    (@g6_cycling, '여름맞이 인천 라이딩 🌊',
     '서울 → 인천 아라뱃길 코스 60km 도전!\n사전 신청 필수, 안전 장비 필수 착용.',
     '서울 강서구 개화산역 1번 출구', 37.5617, 126.8083,
     DATE_ADD(NOW(), INTERVAL 30 DAY), 20, 0, 'SCHEDULED', @biker_id, NOW(), NOW());

-- ============================================================
-- STEP 6. 정모 참석자(meeting_attendee) 등록
-- ============================================================
-- 예정된 정모에 demo 계정 + 2~3명 ATTENDING 등록

-- 예정 정모 ID 캐싱 (각 그룹의 3번째, 4번째 정모 = 예정된 것)
SET @m_running_u1  = (SELECT id FROM meeting WHERE group_id = @g1_running AND status = 'SCHEDULED' ORDER BY meeting_date LIMIT 1);
SET @m_running_u2  = (SELECT id FROM meeting WHERE group_id = @g1_running AND status = 'SCHEDULED' ORDER BY meeting_date LIMIT 1 OFFSET 1);
SET @m_dev_u1      = (SELECT id FROM meeting WHERE group_id = @g2_dev     AND status = 'SCHEDULED' ORDER BY meeting_date LIMIT 1);
SET @m_dev_u2      = (SELECT id FROM meeting WHERE group_id = @g2_dev     AND status = 'SCHEDULED' ORDER BY meeting_date LIMIT 1 OFFSET 1);
SET @m_music_u1    = (SELECT id FROM meeting WHERE group_id = @g3_music   AND status = 'SCHEDULED' ORDER BY meeting_date LIMIT 1);
SET @m_books_u1    = (SELECT id FROM meeting WHERE group_id = @g4_books   AND status = 'SCHEDULED' ORDER BY meeting_date LIMIT 1);
SET @m_cooking_u1  = (SELECT id FROM meeting WHERE group_id = @g5_cooking AND status = 'SCHEDULED' ORDER BY meeting_date LIMIT 1);
SET @m_cycling_u1  = (SELECT id FROM meeting WHERE group_id = @g6_cycling AND status = 'SCHEDULED' ORDER BY meeting_date LIMIT 1);

-- 지난 정모 ID 캐싱 (COMPLETED)
SET @m_running_p1  = (SELECT id FROM meeting WHERE group_id = @g1_running AND status = 'COMPLETED' ORDER BY meeting_date DESC LIMIT 1);
SET @m_dev_p1      = (SELECT id FROM meeting WHERE group_id = @g2_dev     AND status = 'COMPLETED' ORDER BY meeting_date DESC LIMIT 1);
SET @m_books_p1    = (SELECT id FROM meeting WHERE group_id = @g4_books   AND status = 'COMPLETED' ORDER BY meeting_date DESC LIMIT 1);

INSERT IGNORE INTO meeting_attendee (meeting_id, member_id, status, created_at, modified_at)
VALUES
    -- 러닝 크루 예정 1차
    (@m_running_u1, @demo_id,    'ATTENDING', NOW(), NOW()),
    (@m_running_u1, @runner_id,  'ATTENDING', NOW(), NOW()),
    (@m_running_u1, @reader_id,  'ATTENDING', NOW(), NOW()),
    (@m_running_u1, @biker_id,   'MAYBE',     NOW(), NOW()),
    -- 러닝 크루 예정 2차
    (@m_running_u2, @demo_id,    'ATTENDING', NOW(), NOW()),
    (@m_running_u2, @runner_id,  'ATTENDING', NOW(), NOW()),
    (@m_running_u2, @devman_id,  'ATTENDING', NOW(), NOW()),

    -- 러닝 크루 지난 정모 (이미 참석한 것처럼)
    (@m_running_p1, @demo_id,    'ATTENDING', NOW(), NOW()),
    (@m_running_p1, @runner_id,  'ATTENDING', NOW(), NOW()),
    (@m_running_p1, @reader_id,  'ATTENDING', NOW(), NOW()),
    (@m_running_p1, @biker_id,   'ATTENDING', NOW(), NOW()),

    -- 개발자 스터디
    (@m_dev_u1, @demo_id,     'ATTENDING', NOW(), NOW()),
    (@m_dev_u1, @devman_id,   'ATTENDING', NOW(), NOW()),
    (@m_dev_u1, @musician_id, 'ATTENDING', NOW(), NOW()),
    (@m_dev_u2, @demo_id,     'ATTENDING', NOW(), NOW()),
    (@m_dev_u2, @devman_id,   'ATTENDING', NOW(), NOW()),
    (@m_dev_p1, @demo_id,     'ATTENDING', NOW(), NOW()),
    (@m_dev_p1, @devman_id,   'ATTENDING', NOW(), NOW()),
    (@m_dev_p1, @runner_id,   'ATTENDING', NOW(), NOW()),

    -- 기타 클럽
    (@m_music_u1, @demo_id,     'ATTENDING', NOW(), NOW()),
    (@m_music_u1, @musician_id, 'ATTENDING', NOW(), NOW()),
    (@m_music_u1, @biker_id,    'ATTENDING', NOW(), NOW()),

    -- 독서 모임
    (@m_books_u1, @demo_id,     'ATTENDING', NOW(), NOW()),
    (@m_books_u1, @reader_id,   'ATTENDING', NOW(), NOW()),
    (@m_books_u1, @cook_id,     'ATTENDING', NOW(), NOW()),
    (@m_books_p1, @demo_id,     'ATTENDING', NOW(), NOW()),
    (@m_books_p1, @reader_id,   'ATTENDING', NOW(), NOW()),

    -- 홈쿡 클래스
    (@m_cooking_u1, @demo_id,   'ATTENDING', NOW(), NOW()),
    (@m_cooking_u1, @cook_id,   'ATTENDING', NOW(), NOW()),
    (@m_cooking_u1, @reader_id, 'MAYBE',     NOW(), NOW()),

    -- 라이딩
    (@m_cycling_u1, @demo_id,   'ATTENDING', NOW(), NOW()),
    (@m_cycling_u1, @biker_id,  'ATTENDING', NOW(), NOW()),
    (@m_cycling_u1, @runner_id, 'ATTENDING', NOW(), NOW());

-- ============================================================
-- STEP 7. 채팅 메시지(chat_message) — 모임당 7개 이상
-- ============================================================
-- SYSTEM 1개 + TEXT 6개 / 모임

INSERT INTO chat_message (group_id, sender_id, message, message_type, created_at, modified_at)
VALUES
    -- ▼ 러닝 크루 ▼
    (@g1_running, NULL,         '강남 새벽 러닝 크루 채팅방이 개설되었습니다. 환영합니다! 🎉', 'SYSTEM', DATE_SUB(NOW(), INTERVAL 90 DAY), DATE_SUB(NOW(), INTERVAL 90 DAY)),
    (@g1_running, @runner_id,   '안녕하세요! 러닝 크루에 오신 것을 환영합니다 🏃‍♂️', 'TEXT', DATE_SUB(NOW(), INTERVAL 89 DAY), DATE_SUB(NOW(), INTERVAL 89 DAY)),
    (@g1_running, @demo_id,     '반갑습니다! 오래 달리지는 못하지만 열심히 해볼게요 😅', 'TEXT', DATE_SUB(NOW(), INTERVAL 88 DAY), DATE_SUB(NOW(), INTERVAL 88 DAY)),
    (@g1_running, @reader_id,   '저도 잘 부탁드려요! 첫 참가 기대됩니다', 'TEXT', DATE_SUB(NOW(), INTERVAL 87 DAY), DATE_SUB(NOW(), INTERVAL 87 DAY)),
    (@g1_running, @runner_id,   '이번 주 토요일 새벽 6시에 반포한강공원 안내센터 앞에서 만나요!', 'TEXT', DATE_SUB(NOW(), INTERVAL 3 DAY), DATE_SUB(NOW(), INTERVAL 3 DAY)),
    (@g1_running, @demo_id,     '네! 꼭 참석하겠습니다 💪', 'TEXT', DATE_SUB(NOW(), INTERVAL 3 DAY), DATE_SUB(NOW(), INTERVAL 3 DAY)),
    (@g1_running, @biker_id,    '저도 갈게요~ 날씨 좋으면 더 멀리 달려봐요!', 'TEXT', DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY)),
    (@g1_running, @devman_id,   '오늘 달리기 완료! 7km 뛰었어요. 다들 수고하셨습니다 🎉', 'TEXT', DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY)),

    -- ▼ 개발자 스터디 ▼
    (@g2_dev, NULL,          '판교 백엔드 개발자 스터디 채팅방이 개설되었습니다. 환영합니다! 🎉', 'SYSTEM', DATE_SUB(NOW(), INTERVAL 80 DAY), DATE_SUB(NOW(), INTERVAL 80 DAY)),
    (@g2_dev, @devman_id,    '안녕하세요! 백엔드 스터디 개설했습니다. 같이 공부해요 💻', 'TEXT', DATE_SUB(NOW(), INTERVAL 79 DAY), DATE_SUB(NOW(), INTERVAL 79 DAY)),
    (@g2_dev, @demo_id,      '반갑습니다! 스터디 참여 기대됩니다', 'TEXT', DATE_SUB(NOW(), INTERVAL 78 DAY), DATE_SUB(NOW(), INTERVAL 78 DAY)),
    (@g2_dev, @musician_id,  '저도 잘 부탁드립니다. 발표 자료는 어디에 올리면 될까요?', 'TEXT', DATE_SUB(NOW(), INTERVAL 77 DAY), DATE_SUB(NOW(), INTERVAL 77 DAY)),
    (@g2_dev, @devman_id,    'GitHub 스터디 레포 만들었습니다! 주소 공유해드릴게요 📎', 'TEXT', DATE_SUB(NOW(), INTERVAL 5 DAY), DATE_SUB(NOW(), INTERVAL 5 DAY)),
    (@g2_dev, @runner_id,    '다음 주 스터디 주제가 Redis인가요? 미리 공부해 가겠습니다', 'TEXT', DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY)),
    (@g2_dev, @devman_id,    '맞아요! Redis 캐싱 전략과 분산 락 내용입니다. 실습 코드도 준비 중이에요', 'TEXT', DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY)),
    (@g2_dev, @demo_id,      '기대됩니다! 실무에서 꼭 써보고 싶었던 내용이에요 👍', 'TEXT', DATE_SUB(NOW(), INTERVAL 12 HOUR), DATE_SUB(NOW(), INTERVAL 12 HOUR)),

    -- ▼ 기타 클럽 ▼
    (@g3_music, NULL,         '홍대 인디 기타 클럽 채팅방이 개설되었습니다. 환영합니다! 🎉', 'SYSTEM', DATE_SUB(NOW(), INTERVAL 70 DAY), DATE_SUB(NOW(), INTERVAL 70 DAY)),
    (@g3_music, @musician_id, '기타 클럽 오픈했습니다! 장르 불문 함께 연주해요 🎸', 'TEXT', DATE_SUB(NOW(), INTERVAL 69 DAY), DATE_SUB(NOW(), INTERVAL 69 DAY)),
    (@g3_music, @demo_id,     '안녕하세요! 어쿠스틱 기타 초보인데 참여해도 될까요?', 'TEXT', DATE_SUB(NOW(), INTERVAL 68 DAY), DATE_SUB(NOW(), INTERVAL 68 DAY)),
    (@g3_music, @musician_id, '물론이죠! 초보자 환영입니다 😊 같이 배워가요', 'TEXT', DATE_SUB(NOW(), INTERVAL 68 DAY), DATE_SUB(NOW(), INTERVAL 68 DAY)),
    (@g3_music, @biker_id,    '저는 베이스 기타 칩니다~ 잘 부탁드려요!', 'TEXT', DATE_SUB(NOW(), INTERVAL 4 DAY), DATE_SUB(NOW(), INTERVAL 4 DAY)),
    (@g3_music, @musician_id, '이번 합주 곡 목록입니다: IU - 좋은 날 / 잔나비 - 주저하는 연인들을 위해', 'TEXT', DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY)),
    (@g3_music, @demo_id,     '좋은 날 코드 좀 어렵던데... 미리 연습해갈게요 🎵', 'TEXT', DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY)),

    -- ▼ 독서 모임 ▼
    (@g4_books, NULL,         '북서울 독서 모임 채팅방이 개설되었습니다. 환영합니다! 🎉', 'SYSTEM', DATE_SUB(NOW(), INTERVAL 60 DAY), DATE_SUB(NOW(), INTERVAL 60 DAY)),
    (@g4_books, @reader_id,   '독서 모임 개설했습니다! 매월 함께 책 읽고 이야기 나눠요 📚', 'TEXT', DATE_SUB(NOW(), INTERVAL 59 DAY), DATE_SUB(NOW(), INTERVAL 59 DAY)),
    (@g4_books, @demo_id,     '안녕하세요! 오랫동안 혼자 책 읽어왔는데 함께 하니 좋을 것 같아요', 'TEXT', DATE_SUB(NOW(), INTERVAL 58 DAY), DATE_SUB(NOW(), INTERVAL 58 DAY)),
    (@g4_books, @cook_id,     '저도 반갑습니다! 추천 책 있으시면 말씀해주세요', 'TEXT', DATE_SUB(NOW(), INTERVAL 57 DAY), DATE_SUB(NOW(), INTERVAL 57 DAY)),
    (@g4_books, @reader_id,   '이번 달 책은 불편한 편의점으로 결정됐습니다! 미리 읽어오세요 😊', 'TEXT', DATE_SUB(NOW(), INTERVAL 10 DAY), DATE_SUB(NOW(), INTERVAL 10 DAY)),
    (@g4_books, @musician_id, '완독했어요! 생각보다 따뜻한 이야기라 좋더라고요', 'TEXT', DATE_SUB(NOW(), INTERVAL 3 DAY), DATE_SUB(NOW(), INTERVAL 3 DAY)),
    (@g4_books, @demo_id,     '저도 절반 읽었어요. 독고 씨가 너무 매력있어요 ㅎㅎ', 'TEXT', DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY)),
    (@g4_books, @admin_id,    '토론 날 기대됩니다! 카페에서 봐요 ☕', 'TEXT', DATE_SUB(NOW(), INTERVAL 6 HOUR), DATE_SUB(NOW(), INTERVAL 6 HOUR)),

    -- ▼ 홈쿡 클래스 ▼
    (@g5_cooking, NULL,       '마포 홈쿡 클래스 채팅방이 개설되었습니다. 환영합니다! 🎉', 'SYSTEM', DATE_SUB(NOW(), INTERVAL 50 DAY), DATE_SUB(NOW(), INTERVAL 50 DAY)),
    (@g5_cooking, @cook_id,   '홈쿡 클래스 개설했습니다! 맛있는 요리 함께 만들어요 🍳', 'TEXT', DATE_SUB(NOW(), INTERVAL 49 DAY), DATE_SUB(NOW(), INTERVAL 49 DAY)),
    (@g5_cooking, @demo_id,   '안녕하세요! 요리 완전 초보인데 따라갈 수 있을까요?', 'TEXT', DATE_SUB(NOW(), INTERVAL 48 DAY), DATE_SUB(NOW(), INTERVAL 48 DAY)),
    (@g5_cooking, @cook_id,   '물론이죠! 처음부터 천천히 알려드릴게요 😊', 'TEXT', DATE_SUB(NOW(), INTERVAL 48 DAY), DATE_SUB(NOW(), INTERVAL 48 DAY)),
    (@g5_cooking, @reader_id, '지난번 이탈리안 요리 너무 맛있었어요!! 티라미수 레시피 공유해주실 수 있나요?', 'TEXT', DATE_SUB(NOW(), INTERVAL 5 DAY), DATE_SUB(NOW(), INTERVAL 5 DAY)),
    (@g5_cooking, @cook_id,   '다음 달 태국 요리 재료 목록 공유드립니다. 참고하세요! 🌶️', 'TEXT', DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY)),
    (@g5_cooking, @biker_id,  '태국 요리 기대돼요! 팟타이 좋아하는데 직접 만들어보고 싶었어요', 'TEXT', DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY)),

    -- ▼ 라이딩 ▼
    (@g6_cycling, NULL,        '한강 주말 라이딩 채팅방이 개설되었습니다. 환영합니다! 🎉', 'SYSTEM', DATE_SUB(NOW(), INTERVAL 40 DAY), DATE_SUB(NOW(), INTERVAL 40 DAY)),
    (@g6_cycling, @biker_id,   '라이딩 모임 개설했습니다! 한강을 달려봐요 🚴‍♂️', 'TEXT', DATE_SUB(NOW(), INTERVAL 39 DAY), DATE_SUB(NOW(), INTERVAL 39 DAY)),
    (@g6_cycling, @demo_id,    '안녕하세요! 입문용 자전거인데도 참가 가능할까요?', 'TEXT', DATE_SUB(NOW(), INTERVAL 38 DAY), DATE_SUB(NOW(), INTERVAL 38 DAY)),
    (@g6_cycling, @biker_id,   '물론이죠! 자전거 종류 무관합니다. 안전 장비만 갖춰오세요 😊', 'TEXT', DATE_SUB(NOW(), INTERVAL 38 DAY), DATE_SUB(NOW(), INTERVAL 38 DAY)),
    (@g6_cycling, @runner_id,  '이번 주 날씨 좋다고 하던데 40km 완주해봐요!', 'TEXT', DATE_SUB(NOW(), INTERVAL 4 DAY), DATE_SUB(NOW(), INTERVAL 4 DAY)),
    (@g6_cycling, @musician_id,'오전 7시 30분이 좀 이른데 30분 늦출 수 있을까요? 😅', 'TEXT', DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY)),
    (@g6_cycling, @biker_id,   '이번 주는 7시 30분으로 유지할게요. 날씨가 더워지기 전에 출발해야 해서요!', 'TEXT', DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY)),
    (@g6_cycling, @demo_id,    '넵! 알겠습니다. 내일 뵈어요 🚴', 'TEXT', DATE_SUB(NOW(), INTERVAL 8 HOUR), DATE_SUB(NOW(), INTERVAL 8 HOUR));

-- ============================================================
-- STEP 8. 갤러리 포스트(gallery_post) 생성
-- ============================================================
-- 각 모임당 1개 포스트 (3장 이상 이미지)

INSERT INTO gallery_post (group_id, uploader_id, caption, created_at, modified_at)
VALUES
    (@g1_running,  @runner_id,   '5월 정기 러닝 - 반포한강공원에서 10km 완주! 다들 수고하셨어요 🏃‍♂️💨',       DATE_SUB(NOW(), INTERVAL 44 DAY), DATE_SUB(NOW(), INTERVAL 44 DAY)),
    (@g2_dev,      @devman_id,   'JPA 스터디 현장 사진 - 열띤 토론과 코드 리뷰를 진행했습니다 💻',           DATE_SUB(NOW(), INTERVAL 29 DAY), DATE_SUB(NOW(), INTERVAL 29 DAY)),
    (@g3_music,    @musician_id, '4월 정기 합주 사진 - 처음으로 완벽하게 합주가 맞았어요! 🎸🎵',             DATE_SUB(NOW(), INTERVAL 39 DAY), DATE_SUB(NOW(), INTERVAL 39 DAY)),
    (@g4_books,    @reader_id,   '5월 독서 토론 - 채식주의자를 읽고 2시간 넘게 토론했습니다 📚',             DATE_SUB(NOW(), INTERVAL 34 DAY), DATE_SUB(NOW(), INTERVAL 34 DAY)),
    (@g5_cooking,  @cook_id,     '이탈리안 쿠킹 클래스 - 생면 파스타 완성! 너무 맛있었어요 🍝',               DATE_SUB(NOW(), INTERVAL 24 DAY), DATE_SUB(NOW(), INTERVAL 24 DAY)),
    (@g6_cycling,  @biker_id,    '한강 풀코스 40km 완주 기념 단체 사진 🚴‍♂️🚴‍♀️ 모두 수고하셨습니다!',        DATE_SUB(NOW(), INTERVAL 19 DAY), DATE_SUB(NOW(), INTERVAL 19 DAY));

-- 갤러리 포스트 ID 캐싱
SET @gp_running  = (SELECT id FROM gallery_post WHERE group_id = @g1_running  AND uploader_id = @runner_id   ORDER BY id DESC LIMIT 1);
SET @gp_dev      = (SELECT id FROM gallery_post WHERE group_id = @g2_dev      AND uploader_id = @devman_id   ORDER BY id DESC LIMIT 1);
SET @gp_music    = (SELECT id FROM gallery_post WHERE group_id = @g3_music    AND uploader_id = @musician_id ORDER BY id DESC LIMIT 1);
SET @gp_books    = (SELECT id FROM gallery_post WHERE group_id = @g4_books    AND uploader_id = @reader_id   ORDER BY id DESC LIMIT 1);
SET @gp_cooking  = (SELECT id FROM gallery_post WHERE group_id = @g5_cooking  AND uploader_id = @cook_id     ORDER BY id DESC LIMIT 1);
SET @gp_cycling  = (SELECT id FROM gallery_post WHERE group_id = @g6_cycling  AND uploader_id = @biker_id    ORDER BY id DESC LIMIT 1);

-- ============================================================
-- STEP 9. 갤러리 이미지(gallery_image) — 포스트당 3장
-- ============================================================

INSERT INTO gallery_image
    (post_id, group_id, uploader_id, image_url, caption, original_file_name, file_size, created_at, modified_at)
VALUES
    -- 러닝 크루 (3장)
    (@gp_running, @g1_running, @runner_id, 'https://picsum.photos/seed/run1a/600/400', '반포한강공원 출발 전 단체 사진',  'run1a.jpg', 524288, DATE_SUB(NOW(), INTERVAL 44 DAY), DATE_SUB(NOW(), INTERVAL 44 DAY)),
    (@gp_running, @g1_running, @runner_id, 'https://picsum.photos/seed/run1b/600/400', '5km 반환점에서',                  'run1b.jpg', 489216, DATE_SUB(NOW(), INTERVAL 44 DAY), DATE_SUB(NOW(), INTERVAL 44 DAY)),
    (@gp_running, @g1_running, @runner_id, 'https://picsum.photos/seed/run1c/600/400', '완주 후 기념 사진! 10km 달성 🎉', 'run1c.jpg', 612352, DATE_SUB(NOW(), INTERVAL 44 DAY), DATE_SUB(NOW(), INTERVAL 44 DAY)),

    -- 개발자 스터디 (3장)
    (@gp_dev, @g2_dev, @devman_id, 'https://picsum.photos/seed/dev2a/600/400', '스터디룸 전경',                    'dev2a.jpg', 401408, DATE_SUB(NOW(), INTERVAL 29 DAY), DATE_SUB(NOW(), INTERVAL 29 DAY)),
    (@gp_dev, @g2_dev, @devman_id, 'https://picsum.photos/seed/dev2b/600/400', '화이트보드 아키텍처 설명 중',       'dev2b.jpg', 368640, DATE_SUB(NOW(), INTERVAL 29 DAY), DATE_SUB(NOW(), INTERVAL 29 DAY)),
    (@gp_dev, @g2_dev, @devman_id, 'https://picsum.photos/seed/dev2c/600/400', '스터디 끝나고 뒤풀이 🍺',          'dev2c.jpg', 445440, DATE_SUB(NOW(), INTERVAL 29 DAY), DATE_SUB(NOW(), INTERVAL 29 DAY)),

    -- 기타 클럽 (3장)
    (@gp_music, @g3_music, @musician_id, 'https://picsum.photos/seed/guitar3a/600/400', '합주 시작 전 세팅 중',           'guitar3a.jpg', 573440, DATE_SUB(NOW(), INTERVAL 39 DAY), DATE_SUB(NOW(), INTERVAL 39 DAY)),
    (@gp_music, @g3_music, @musician_id, 'https://picsum.photos/seed/guitar3b/600/400', '열정적인 합주 현장 🎸',          'guitar3b.jpg', 536576, DATE_SUB(NOW(), INTERVAL 39 DAY), DATE_SUB(NOW(), INTERVAL 39 DAY)),
    (@gp_music, @g3_music, @musician_id, 'https://picsum.photos/seed/guitar3c/600/400', '합주 후 단체 사진',              'guitar3c.jpg', 491520, DATE_SUB(NOW(), INTERVAL 39 DAY), DATE_SUB(NOW(), INTERVAL 39 DAY)),

    -- 독서 모임 (3장)
    (@gp_books, @g4_books, @reader_id, 'https://picsum.photos/seed/books4a/600/400', '이번 달 책 — 채식주의자',         'books4a.jpg', 327680, DATE_SUB(NOW(), INTERVAL 34 DAY), DATE_SUB(NOW(), INTERVAL 34 DAY)),
    (@gp_books, @g4_books, @reader_id, 'https://picsum.photos/seed/books4b/600/400', '토론 중인 멤버들',                'books4b.jpg', 360448, DATE_SUB(NOW(), INTERVAL 34 DAY), DATE_SUB(NOW(), INTERVAL 34 DAY)),
    (@gp_books, @g4_books, @reader_id, 'https://picsum.photos/seed/books4c/600/400', '토론 후 인증샷 📚',               'books4c.jpg', 344064, DATE_SUB(NOW(), INTERVAL 34 DAY), DATE_SUB(NOW(), INTERVAL 34 DAY)),

    -- 홈쿡 클래스 (3장)
    (@gp_cooking, @g5_cooking, @cook_id, 'https://picsum.photos/seed/cook5a/600/400', '재료 손질 중',                   'cook5a.jpg', 458752, DATE_SUB(NOW(), INTERVAL 24 DAY), DATE_SUB(NOW(), INTERVAL 24 DAY)),
    (@gp_cooking, @g5_cooking, @cook_id, 'https://picsum.photos/seed/cook5b/600/400', '파스타 면 삶는 중 🍝',           'cook5b.jpg', 425984, DATE_SUB(NOW(), INTERVAL 24 DAY), DATE_SUB(NOW(), INTERVAL 24 DAY)),
    (@gp_cooking, @g5_cooking, @cook_id, 'https://picsum.photos/seed/cook5c/600/400', '완성! 맛있게 먹겠습니다 😋',      'cook5c.jpg', 507904, DATE_SUB(NOW(), INTERVAL 24 DAY), DATE_SUB(NOW(), INTERVAL 24 DAY)),

    -- 라이딩 (3장)
    (@gp_cycling, @g6_cycling, @biker_id, 'https://picsum.photos/seed/bike6a/600/400', '여의도 출발 전 집결',             'bike6a.jpg', 589824, DATE_SUB(NOW(), INTERVAL 19 DAY), DATE_SUB(NOW(), INTERVAL 19 DAY)),
    (@gp_cycling, @g6_cycling, @biker_id, 'https://picsum.photos/seed/bike6b/600/400', '한강 라이딩 중 🚴',               'bike6b.jpg', 614400, DATE_SUB(NOW(), INTERVAL 19 DAY), DATE_SUB(NOW(), INTERVAL 19 DAY)),
    (@gp_cycling, @g6_cycling, @biker_id, 'https://picsum.photos/seed/bike6c/600/400', '40km 완주 기념! 모두 최고 🏅',   'bike6c.jpg', 557056, DATE_SUB(NOW(), INTERVAL 19 DAY), DATE_SUB(NOW(), INTERVAL 19 DAY));

-- ============================================================
-- 완료 확인 쿼리 (선택사항)
-- ============================================================
-- SELECT '=== 시드 데이터 삽입 완료 ===' AS message;
-- SELECT COUNT(*) AS members  FROM member;
-- SELECT COUNT(*) AS groups   FROM club;
-- SELECT COUNT(*) AS meetings FROM meeting;
-- SELECT COUNT(*) AS chats    FROM chat_message;
-- SELECT COUNT(*) AS gallery  FROM gallery_image;
