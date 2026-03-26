package com.damoyeo.api.domain.member.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ============================================================================
 * 회원 정보 수정 요청 DTO
 * ============================================================================
 *
 * [역할]
 * 프로필 수정 폼에서 받은 데이터를 담는 객체입니다.
 *
 * [프론트엔드 요청 예시]
 * PUT /api/member/modify
 * {
 *   "nickname": "새닉네임",
 *   "introduction": "안녕하세요!",
 *   "profileImage": "image.jpg"
 * }
 *
 * [특징]
 * - 모든 필드가 optional (null 허용)
 * - null이 아닌 필드만 수정됩니다.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MemberModifyRequest {

    /**
     * 닉네임 (선택)
     *
     * null이면 기존 값 유지
     */
    @Size(min = 2, max = 20, message = "닉네임은 2-20자 사이여야 합니다.")
    private String nickname;

    /**
     * 프로필 이미지 URL (선택)
     */
    private String profileImage;

    /**
     * 자기소개 (선택)
     */
    @Size(max = 200, message = "자기소개는 200자 이하여야 합니다.")
    private String introduction;

    /**
     * 새 비밀번호 (선택)
     *
     * null이면 비밀번호 변경하지 않음
     */
    @Size(min = 4, message = "비밀번호는 4자 이상이어야 합니다.")
    private String password;

    /**
     * 활동 모임 공개 여부 (선택, 프리미엄 회원 전용)
     *
     * null이면 기존 값 유지
     */
    private Boolean showJoinedGroups;
}
