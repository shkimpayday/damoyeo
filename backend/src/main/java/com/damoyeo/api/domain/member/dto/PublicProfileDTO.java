package com.damoyeo.api.domain.member.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 공개 프로필 DTO
 *
 * 다른 회원이 볼 수 있는 공개 프로필 정보를 담습니다.
 * 이메일, 비밀번호 등 민감한 정보는 포함하지 않습니다.
 *
 * [사용처]
 * - 모임 멤버 프로필 조회
 * - 관리자 회원 상세 조회
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PublicProfileDTO {

    /** 회원 ID */
    private Long id;

    /** 닉네임 */
    private String nickname;

    /** 프로필 이미지 URL */
    private String profileImage;

    /** 자기소개 */
    private String introduction;

    /** 지역 */
    private String address;

    /** 가입일 */
    private LocalDateTime createdAt;

    /** 가입한 모임 수 */
    private int groupCount;

    /** 가입한 모임 목록 (공개 모임만) */
    private List<JoinedGroupDTO> joinedGroups;

    /** 활동 모임 공개 여부 */
    private boolean showJoinedGroups;

    /**
     * 가입한 모임 정보 (간략)
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class JoinedGroupDTO {
        private Long id;
        private String name;
        private String thumbnailImage;
        private String categoryName;
    }
}
