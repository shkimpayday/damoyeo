package com.damoyeo.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 다모여 애플리케이션의 메인 진입점 (Entry Point)
 *
 * 이 클래스는 Spring Boot 애플리케이션을 시작하는 역할을 합니다.
 * Java의 main() 메서드처럼, Spring Boot 앱도 여기서 시작됩니다.
 *
 * ▶ @SpringBootApplication
 *   이 어노테이션 하나로 다음 3가지가 자동 적용됩니다:
 *   1. @Configuration: 이 클래스가 설정 클래스임을 표시
 *   2. @EnableAutoConfiguration: Spring Boot의 자동 설정 기능 활성화
 *   3. @ComponentScan: 이 패키지(com.damoyeo.api) 하위의 모든 컴포넌트를 자동으로 찾아서 등록
 *
 * ▶ @EnableJpaAuditing
 *   JPA Auditing 기능을 활성화합니다.
 *   - BaseEntity의 @CreatedDate, @LastModifiedDate가 동작하려면 필수!
 *   - 엔티티가 생성/수정될 때 자동으로 시간을 기록해줍니다.
 *   - 예: Member가 생성되면 createdAt에 현재 시간이 자동으로 들어감
 *
 * ▶ @EnableScheduling
 *   Spring의 스케줄링 기능을 활성화합니다.
 *   - @Scheduled 어노테이션이 동작하려면 필수!
 *   - 정기적으로 실행해야 하는 작업에 사용됩니다.
 *   - 예: 정모 상태 자동 업데이트 (MeetingStatusScheduler)
 *
 * [실행 방법]
 *   ./gradlew bootRun 또는 IDE에서 이 클래스를 Run
 *   → 내장 톰캣이 8080 포트에서 시작됨
 */
@SpringBootApplication
@EnableJpaAuditing
@EnableScheduling
public class DamoyeoApplication {

    public static void main(String[] args) {
        // Spring Boot 애플리케이션 실행
        // 내장 톰캣 서버가 시작되고, 모든 Bean들이 초기화됩니다.
        SpringApplication.run(DamoyeoApplication.class, args);
    }
}
