package com.damoyeo.api.domain.payment.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ============================================================================
 * 카카오페이 결제 준비 응답 DTO
 * ============================================================================
 *
 * [용도]
 * 카카오페이 API의 /v1/payment/ready 응답을 매핑합니다.
 * 테스트 모드에서는 프론트엔드로 직접 반환됩니다.
 *
 * [주요 필드]
 * - tid: 결제 고유번호 (결제 승인 시 필요)
 * - nextRedirectPcUrl: PC 결제 페이지 URL
 * - nextRedirectMobileUrl: 모바일 결제 페이지 URL
 * - nextRedirectAppUrl: 카카오톡 앱 결제 URL
 *
 * [JSON 직렬화]
 * - 카카오페이 API 응답(snake_case)을 읽을 때: @JsonProperty로 매핑
 * - 프론트엔드로 응답할 때: camelCase 필드명 사용 (JsonAlias 사용)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KakaoPayReadyResponse {

    /**
     * 결제 고유번호
     * 결제 승인 시 필요
     */
    private String tid;

    /**
     * PC 결제 페이지 URL
     * 카카오페이 API에서는 next_redirect_pc_url로 오지만, camelCase로 응답
     */
    @JsonAlias("next_redirect_pc_url")
    private String nextRedirectPcUrl;

    /**
     * 모바일 결제 페이지 URL
     */
    @JsonAlias("next_redirect_mobile_url")
    private String nextRedirectMobileUrl;

    /**
     * 카카오톡 앱 결제 URL
     */
    @JsonAlias("next_redirect_app_url")
    private String nextRedirectAppUrl;

    /**
     * 안드로이드 앱 스킴
     */
    @JsonAlias("android_app_scheme")
    private String androidAppScheme;

    /**
     * iOS 앱 스킴
     */
    @JsonAlias("ios_app_scheme")
    private String iosAppScheme;

    /**
     * 결제 준비 생성 시간
     */
    @JsonAlias("created_at")
    private String createdAt;
}
