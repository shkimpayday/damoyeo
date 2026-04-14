package com.damoyeo.api.domain.admin.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 관리자용 회원 DTO
 *
 * [프리미엄 정보]
 * - isPremium: 프리미엄 회원 여부
 * - premiumStartDate: 프리미엄 시작일
 * - premiumEndDate: 프리미엄 종료일
 * - premiumDaysRemaining: 남은 일수
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminMemberDTO {

    /** 회원 ID */
    private Long id;

    /** 이메일 */
    private String email;

    /** 닉네임 */
    private String nickname;

    /** 프로필 이미지 URL */
    private String profileImage;

    /** 권한 목록 */
    private List<String> roleNames;

    /** 소셜 로그인 여부 */
    private boolean social;

    /** 가입일 */
    private LocalDateTime createdAt;

    /** 가입한 모임 수 */
    private int groupCount;

    // 프리미엄 정보

    /** 프리미엄 회원 여부 */
    @JsonProperty("isPremium")
    private boolean isPremium;

    /** 프리미엄 구독 유형 (PREMIUM_MONTHLY, PREMIUM_YEARLY) */
    private String premiumType;

    /** 프리미엄 시작일 */
    private LocalDateTime premiumStartDate;

    /** 프리미엄 종료일 */
    private LocalDateTime premiumEndDate;

    /** 남은 일수 */
    private long premiumDaysRemaining;
}
