package com.damoyeo.api.domain.member.service;

/**
 * 이메일 인증 서비스 인터페이스
 */
public interface EmailService {

    /**
     * 인증 코드 발송
     * @param email 수신자 이메일
     * @return 발송 성공 여부
     */
    boolean sendVerificationCode(String email);

    /**
     * 인증 코드 검증
     * @param email 이메일
     * @param code 인증 코드
     * @return 검증 성공 여부
     */
    boolean verifyCode(String email, String code);

    /**
     * 이메일 인증 완료 여부 확인
     * @param email 이메일
     * @return 인증 완료 여부
     */
    boolean isVerified(String email);
}
