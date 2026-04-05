package com.damoyeo.api.global.config;

import com.damoyeo.api.domain.category.entity.Category;
import com.damoyeo.api.domain.category.repository.CategoryRepository;
import com.damoyeo.api.domain.event.entity.Event;
import com.damoyeo.api.domain.event.entity.EventType;
import com.damoyeo.api.domain.event.repository.EventRepository;
import com.damoyeo.api.domain.member.entity.Member;
import com.damoyeo.api.domain.member.entity.MemberRole;
import com.damoyeo.api.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * ============================================================================
 * 초기 데이터 생성 클래스
 * ============================================================================
 *
 * [이 클래스의 역할]
 * 애플리케이션 시작 시 필요한 초기 데이터를 자동으로 생성합니다.
 * 현재는 카테고리 데이터만 생성합니다.
 *
 * [실행 시점]
 * Spring Boot 애플리케이션이 완전히 시작된 후
 * run() 메서드가 자동으로 실행됩니다.
 *
 * [동작 방식]
 * 1. 애플리케이션 시작
 * 2. 모든 Bean 초기화 완료
 * 3. CommandLineRunner.run() 호출
 * 4. 카테고리 테이블이 비어있으면 18개 카테고리 생성
 * 5. 이미 데이터가 있으면 아무것도 하지 않음 (중복 방지)
 *
 * ▶ @Component
 *   - Spring Bean으로 등록됩니다.
 *
 * ▶ CommandLineRunner
 *   - 애플리케이션 시작 후 실행할 코드를 정의하는 인터페이스입니다.
 *   - run() 메서드 하나만 구현하면 됩니다.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    /**
     * 카테고리 저장소
     *
     * 카테고리 데이터 조회 및 저장에 사용합니다.
     */
    private final CategoryRepository categoryRepository;

    /**
     * 이벤트 저장소
     *
     * 이벤트/배너 데이터 조회 및 저장에 사용합니다.
     */
    private final EventRepository eventRepository;

    /**
     * 회원 저장소 / 비밀번호 인코더 (데모 계정 생성용)
     */
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 애플리케이션 시작 후 실행되는 메서드
     *
     * @param args 커맨드라인 인자 (사용하지 않음)
     *
     * [중복 방지 로직]
     * categoryRepository.count() == 0 조건으로
     * 테이블이 비어있을 때만 데이터를 생성합니다.
     * 이미 데이터가 있으면 아무것도 하지 않습니다.
     */
    @Override
    public void run(String... args) {
        // 카테고리 테이블이 비어있을 때만 초기화
        if (categoryRepository.count() == 0) {
            initCategories();
        }

        // 이벤트 테이블이 비어있을 때만 초기화
        if (eventRepository.count() == 0) {
            initEvents();
        }

        // 데모 계정이 없을 때만 생성
        if (memberRepository.findByEmail("admin@damoyeo.store").isEmpty()) {
            initDemoAccounts();
        }
    }

    /**
     * 카테고리 초기 데이터 생성
     *
     * 소모임 앱에서 사용할 18개의 기본 카테고리를 생성합니다.
     * 각 카테고리는 이름, 아이콘(이모지), 표시 순서를 가집니다.
     *
     * [카테고리 목록] (18개)
     * - 운동/스포츠, 사교/인맥, 아웃도어/여행
     * - 문화/공연, 음악/악기, 외국어
     * - 독서, 스터디, 게임/오락
     * - 사진/영상, 요리, 공예
     * - 자기계발, 봉사활동, 반려동물
     * - IT/개발, 금융/재테크, 기타
     *
     * [displayOrder]
     * 프론트엔드에서 카테고리를 정렬할 때 사용합니다.
     * 숫자가 작을수록 먼저 표시됩니다.
     */
    private void initCategories() {
        List<Category> categories = Arrays.asList(
                // 활동적인 취미
                Category.builder().name("운동/스포츠").icon("⚽").displayOrder(1).build(),
                Category.builder().name("사교/인맥").icon("🤝").displayOrder(2).build(),
                Category.builder().name("아웃도어/여행").icon("🏕️").displayOrder(3).build(),

                // 문화/예술
                Category.builder().name("문화/공연").icon("🎭").displayOrder(4).build(),
                Category.builder().name("음악/악기").icon("🎵").displayOrder(5).build(),
                Category.builder().name("외국어").icon("🌍").displayOrder(6).build(),

                // 학습/자기계발
                Category.builder().name("독서").icon("📚").displayOrder(7).build(),
                Category.builder().name("스터디").icon("📝").displayOrder(8).build(),
                Category.builder().name("게임/오락").icon("🎮").displayOrder(9).build(),

                // 창작 활동
                Category.builder().name("사진/영상").icon("📷").displayOrder(10).build(),
                Category.builder().name("요리").icon("🍳").displayOrder(11).build(),
                Category.builder().name("공예").icon("🎨").displayOrder(12).build(),

                // 라이프스타일
                Category.builder().name("자기계발").icon("💪").displayOrder(13).build(),
                Category.builder().name("봉사활동").icon("💝").displayOrder(14).build(),
                Category.builder().name("반려동물").icon("🐾").displayOrder(15).build(),

                // 전문 분야
                Category.builder().name("IT/개발").icon("💻").displayOrder(16).build(),
                Category.builder().name("금융/재테크").icon("💰").displayOrder(17).build(),
                Category.builder().name("기타").icon("🔖").displayOrder(18).build()
        );

        // 모든 카테고리를 한 번에 저장
        categoryRepository.saveAll(categories);
        log.info("Categories initialized: {} categories created", categories.size());
    }

    /**
     * 이벤트/배너 초기 데이터 생성
     *
     * 메인 페이지 배너 슬라이더에 표시할 샘플 이벤트를 생성합니다.
     *
     * [이벤트 목록] (5개)
     * - 신규 가입 이벤트
     * - 친구 초대 이벤트
     * - 첫 모임 개설 이벤트
     * - 겨울 아웃도어 특집
     * - 설날 특별 이벤트
     */
    private void initEvents() {
        // 이벤트 기간 설정 (현재부터 2개월간)
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startDate = now.minusDays(1);
        LocalDateTime endDate = now.plusMonths(2);

        List<Event> events = Arrays.asList(
                Event.builder()
                        .title("신규 가입 이벤트")
                        .description("다모여에 첫 가입하면 프리미엄 30일 무료!")
                        .content("""
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
                                """)
                        .imageUrl("https://picsum.photos/seed/banner201/800/400")
                        .linkUrl("/events/1")
                        .type(EventType.PROMOTION)
                        .startDate(startDate)
                        .endDate(endDate)
                        .displayOrder(1)
                        .tags("신규가입,프리미엄,무료체험")
                        .isActive(true)
                        .build(),

                Event.builder()
                        .title("친구 초대 이벤트")
                        .description("친구를 초대하고 포인트 받자! 초대할수록 더 많은 혜택!")
                        .content("""
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
                                """)
                        .imageUrl("https://picsum.photos/seed/banner202/800/400")
                        .linkUrl("/events/2")
                        .type(EventType.PROMOTION)
                        .startDate(startDate)
                        .endDate(endDate.plusMonths(1))
                        .displayOrder(2)
                        .tags("친구초대,포인트,리워드")
                        .isActive(true)
                        .build(),

                Event.builder()
                        .title("첫 모임 개설 이벤트")
                        .description("모임을 처음 만들면 홍보 지원금 5만원 지급!")
                        .content("""
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
                                """)
                        .imageUrl("https://picsum.photos/seed/banner203/800/400")
                        .linkUrl("/events/3")
                        .type(EventType.PROMOTION)
                        .startDate(startDate)
                        .endDate(endDate)
                        .displayOrder(3)
                        .tags("모임개설,지원금,호스트")
                        .isActive(true)
                        .build(),

                Event.builder()
                        .title("겨울 아웃도어 특집")
                        .description("겨울 산행, 스키, 보드 모임을 찾고 계신가요?")
                        .content("""
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
                                """)
                        .imageUrl("https://picsum.photos/seed/banner204/800/400")
                        .linkUrl("/events/4")
                        .type(EventType.SPECIAL)
                        .startDate(startDate)
                        .endDate(endDate.plusMonths(1))
                        .displayOrder(4)
                        .tags("아웃도어,겨울,스키,등산")
                        .isActive(true)
                        .build(),

                Event.builder()
                        .title("설날 특별 이벤트")
                        .description("설 연휴에도 다모여! 특별 정모에 참여하세요")
                        .content("""
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
                                """)
                        .imageUrl("https://picsum.photos/seed/banner205/800/400")
                        .linkUrl("/events/5")
                        .type(EventType.SPECIAL)
                        .startDate(startDate)
                        .endDate(endDate)
                        .displayOrder(5)
                        .tags("설날,연휴,특별정모")
                        .isActive(true)
                        .build()
        );

        // 모든 이벤트를 한 번에 저장
        eventRepository.saveAll(events);
        log.info("Events initialized: {} events created", events.size());
    }

    /**
     * 데모 계정 초기 생성
     *
     * 포트폴리오 시연용 계정 2개를 생성합니다.
     * - 관리자: admin@damoyeo.store / Demo1234!
     * - 일반 사용자: demo@damoyeo.store / Demo1234!
     *
     * 이미 계정이 존재하면 생성하지 않습니다.
     */
    private void initDemoAccounts() {
        // 관리자 계정
        Member admin = Member.builder()
                .email("admin@damoyeo.store")
                .password(passwordEncoder.encode("admin1234"))
                .nickname("관리자")
                .introduction("다모여 관리자 계정입니다.")
                .social(false)
                .build();
        admin.addRole(MemberRole.USER);
        admin.addRole(MemberRole.ADMIN);
        memberRepository.save(admin);

        // 일반 사용자 계정
        Member demo = Member.builder()
                .email("demo@damoyeo.store")
                .password(passwordEncoder.encode("demo1234"))
                .nickname("데모유저")
                .introduction("다모여 데모 계정입니다.")
                .social(false)
                .build();
        demo.addRole(MemberRole.USER);
        memberRepository.save(demo);

        log.info("Demo accounts initialized: admin@damoyeo.store (admin1234), demo@damoyeo.store (demo1234)");
    }
}
