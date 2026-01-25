package com.damoyeo.api.domain.member.repository;

import com.damoyeo.api.domain.member.entity.EmailVerification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmailVerificationRepository extends JpaRepository<EmailVerification, Long> {

    Optional<EmailVerification> findByEmail(String email);

    Optional<EmailVerification> findByEmailAndCode(String email, String code);

    void deleteByEmail(String email);
}
