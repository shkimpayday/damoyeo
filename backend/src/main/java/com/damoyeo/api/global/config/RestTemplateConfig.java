package com.damoyeo.api.global.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

/**
 * RestTemplate 설정
 *
 * 외부 API 호출용 RestTemplate 빈을 제공합니다.
 *
 * [사용처]
 * - 카카오페이 결제 API 호출
 * - 외부 서비스 연동
 *
 * [타임아웃 설정]
 * - 연결 타임아웃: 5초
 * - 읽기 타임아웃: 30초
 */
@Configuration
public class RestTemplateConfig {

    /**
     * RestTemplate 빈 생성
     *
     * @param builder RestTemplateBuilder (Spring Boot 제공)
     * @return RestTemplate 인스턴스
     */
    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder
                .setConnectTimeout(Duration.ofSeconds(5))
                .setReadTimeout(Duration.ofSeconds(30))
                .build();
    }
}
