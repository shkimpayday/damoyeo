package com.damoyeo.api.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * ============================================================================
 * Spring MVC 웹 설정 클래스
 * ============================================================================
 *
 * [이 클래스의 역할]
 * Spring MVC 관련 추가 설정을 정의합니다.
 * 현재는 CORS 설정만 포함되어 있습니다.
 *
 * [SecurityConfig의 CORS 설정과의 관계]
 * Spring Security의 CORS 설정(SecurityConfig)과 Spring MVC의 CORS 설정(이 클래스)은
 * 모두 필요합니다. Spring Security 필터 체인을 통과한 후에도
 * Spring MVC 레벨에서 CORS 검증이 이루어지기 때문입니다.
 *
 * 일반적으로 두 설정을 동일하게 유지하는 것이 좋습니다.
 *
 * ▶ @Configuration
 *   - 이 클래스가 Spring 설정 클래스임을 표시합니다.
 *
 * ▶ WebMvcConfigurer
 *   - Spring MVC 설정을 커스터마이징하기 위한 인터페이스입니다.
 *   - 필요한 메서드만 오버라이드하면 됩니다.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${com.damoyeo.upload.path}")
    private String uploadPath;

    /**
     * 정적 리소스 핸들러 설정
     *
     * 업로드된 파일(이미지 등)을 외부에서 접근할 수 있도록 설정합니다.
     *
     * [설정 내용]
     * - /uploads/** URL로 요청 시 실제 파일 시스템의 uploads 폴더에서 파일을 제공
     * - 예: /uploads/profiles/abc.jpg → {uploadPath}/profiles/abc.jpg
     *
     * @param registry 리소스 핸들러 레지스트리
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadPath + "/");
    }

    /**
     * CORS(Cross-Origin Resource Sharing) 매핑 설정
     *
     * 브라우저의 동일 출처 정책(Same-Origin Policy)을 우회하여
     * 다른 도메인(프론트엔드)에서의 요청을 허용합니다.
     *
     * [설정 내용]
     * - addMapping("/**"): 모든 경로에 CORS 적용
     * - allowedOrigins: 허용할 출처 (프론트엔드 주소)
     * - allowedMethods: 허용할 HTTP 메서드
     * - allowedHeaders("*"): 모든 헤더 허용
     * - allowCredentials(true): 쿠키 포함 요청 허용
     * - maxAge(3600): preflight 요청 캐시 시간 (1시간)
     *
     * [CORS 에러 디버깅]
     * 브라우저 개발자 도구에서 "CORS policy" 에러가 나면:
     * 1. allowedOrigins에 프론트엔드 주소가 포함되어 있는지 확인
     * 2. allowCredentials가 true인데 allowedOrigins가 "*"이면 에러 발생
     * 3. SecurityConfig의 CORS 설정도 확인
     *
     * @param registry CORS 설정을 등록할 레지스트리
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                // 허용할 출처 (배포 시 실제 도메인 추가 필요)
                .allowedOrigins("http://localhost:5173", "http://localhost:3000")
                // 허용할 HTTP 메서드
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                // 모든 헤더 허용 (Authorization, Content-Type 등)
                .allowedHeaders("*")
                // 쿠키 포함 요청 허용 (withCredentials: true)
                .allowCredentials(true)
                // preflight 요청 캐시 시간 (초)
                // 브라우저가 OPTIONS 요청을 매번 보내지 않도록 함
                .maxAge(3600);
    }
}
