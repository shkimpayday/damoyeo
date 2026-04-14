package com.damoyeo.api.domain.member.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 회원가입 요청 DTO
 *
 * 회원가입 폼에서 받은 데이터를 담는 객체입니다.
 *
 * [프론트엔드 요청 예시]
 * POST /api/member/signup
 * {
 *   "email": "user@example.com",
 *   "password": "1234",
 *   "nickname": "홍길동"
 * }
 *
 * [유효성 검사]
 * @Valid 어노테이션과 함께 사용하면 자동으로 검증됩니다.
 * 검증 실패 시 GlobalExceptionHandler.handleValidationException()에서 처리됩니다.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MemberSignupRequest {

    /**
     * 이메일
     *
     * @NotBlank: 빈 문자열 불가
     * @Email: 이메일 형식 검증
     */
    @NotBlank(message = "이메일은 필수입니다.")
    @Email(message = "올바른 이메일 형식이 아닙니다.")
    private String email;

    /**
     * 비밀번호
     *
     * @NotBlank: 빈 문자열 불가
     * @Size(min = 4): 최소 4자
     */
    @NotBlank(message = "비밀번호는 필수입니다.")
    @Size(min = 4, message = "비밀번호는 4자 이상이어야 합니다.")
    private String password;

    /**
     * 닉네임
     *
     * @NotBlank: 빈 문자열 불가
     * @Size(min = 2, max = 20): 2~20자
     */
    @NotBlank(message = "닉네임은 필수입니다.")
    @Size(min = 2, max = 20, message = "닉네임은 2-20자 사이여야 합니다.")
    private String nickname;
}
