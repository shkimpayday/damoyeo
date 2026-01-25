package com.damoyeo.api.domain.member.dto;

import com.damoyeo.api.domain.member.entity.Member;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 회원 요약 정보 DTO
 *
 * 모임, 정모 등에서 회원 정보를 중첩 객체로 반환할 때 사용합니다.
 * 민감한 정보(email, password 등)는 포함하지 않습니다.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MemberSummaryDTO {

    private Long id;
    private String nickname;
    private String profileImage;

    /**
     * Entity -> DTO 변환
     */
    public static MemberSummaryDTO from(Member member) {
        if (member == null) return null;
        return MemberSummaryDTO.builder()
                .id(member.getId())
                .nickname(member.getNickname())
                .profileImage(member.getProfileImage())
                .build();
    }
}
