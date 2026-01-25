package com.damoyeo.api.domain.member.service;

import com.damoyeo.api.domain.member.entity.EmailVerification;
import com.damoyeo.api.domain.member.repository.EmailVerificationRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

/**
 * 이메일 인증 서비스 구현
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final EmailVerificationRepository emailVerificationRepository;

    @Value("${com.damoyeo.mail.verification-code-expiration:300000}")
    private long codeExpiration; // 기본 5분

    @Override
    public boolean sendVerificationCode(String email) {
        try {
            // 6자리 인증 코드 생성
            String code = generateVerificationCode();

            // 만료 시간 설정 (5분)
            LocalDateTime expiresAt = LocalDateTime.now().plusSeconds(codeExpiration / 1000);

            // 기존 인증 정보가 있으면 업데이트, 없으면 생성
            Optional<EmailVerification> existing = emailVerificationRepository.findByEmail(email);
            if (existing.isPresent()) {
                existing.get().updateCode(code, expiresAt);
            } else {
                EmailVerification verification = EmailVerification.builder()
                        .email(email)
                        .code(code)
                        .expiresAt(expiresAt)
                        .build();
                emailVerificationRepository.save(verification);
            }

            // 이메일 발송
            sendEmail(email, code);

            log.info("Verification code sent to: {}", email);
            return true;
        } catch (Exception e) {
            log.error("Failed to send verification code to: {}", email, e);
            return false;
        }
    }

    @Override
    public boolean verifyCode(String email, String code) {
        Optional<EmailVerification> verification = emailVerificationRepository.findByEmailAndCode(email, code);

        if (verification.isEmpty()) {
            log.warn("Invalid verification code for email: {}", email);
            return false;
        }

        EmailVerification ev = verification.get();

        if (ev.isExpired()) {
            log.warn("Verification code expired for email: {}", email);
            return false;
        }

        if (ev.isVerified()) {
            log.warn("Email already verified: {}", email);
            return true;
        }

        // 인증 완료 처리
        ev.markAsVerified();
        log.info("Email verified successfully: {}", email);
        return true;
    }

    @Override
    public boolean isVerified(String email) {
        return emailVerificationRepository.findByEmail(email)
                .map(EmailVerification::isVerified)
                .orElse(false);
    }

    /**
     * 6자리 숫자 인증 코드 생성
     */
    private String generateVerificationCode() {
        Random random = new Random();
        int code = 100000 + random.nextInt(900000); // 100000 ~ 999999
        return String.valueOf(code);
    }

    /**
     * 이메일 발송
     */
    private void sendEmail(String to, String code) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setTo(to);
        helper.setSubject("[다모여] 이메일 인증 코드");

        String htmlContent = """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: 'Apple SD Gothic Neo', 'Malgun Gothic', sans-serif; }
                    .container { max-width: 600px; margin: 0 auto; padding: 40px 20px; }
                    .header { text-align: center; margin-bottom: 40px; }
                    .logo { font-size: 28px; font-weight: bold; color: #12B886; }
                    .content { background: #f8f9fa; border-radius: 12px; padding: 40px; text-align: center; }
                    .code { font-size: 36px; font-weight: bold; color: #12B886; letter-spacing: 8px; margin: 20px 0; }
                    .note { color: #868e96; font-size: 14px; margin-top: 20px; }
                    .footer { text-align: center; margin-top: 40px; color: #adb5bd; font-size: 12px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <div class="logo">다모여</div>
                    </div>
                    <div class="content">
                        <p>안녕하세요!</p>
                        <p>회원가입을 위한 이메일 인증 코드입니다.</p>
                        <div class="code">%s</div>
                        <p class="note">이 코드는 5분간 유효합니다.</p>
                    </div>
                    <div class="footer">
                        <p>이 이메일은 다모여 회원가입을 위해 발송되었습니다.</p>
                        <p>본인이 요청하지 않았다면 이 이메일을 무시해주세요.</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(code);

        helper.setText(htmlContent, true);
        mailSender.send(message);
    }
}
