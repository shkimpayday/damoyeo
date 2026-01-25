package com.damoyeo.api.domain.member.controller;

import com.damoyeo.api.domain.member.service.EmailService;
import com.damoyeo.api.domain.member.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 이메일 인증 API Controller
 *
 * [엔드포인트]
 * POST /api/email/send    - 인증 코드 발송
 * POST /api/email/verify  - 인증 코드 검증
 */
@RestController
@RequestMapping("/api/email")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Email", description = "이메일 인증 API")
public class EmailController {

    private final EmailService emailService;
    private final MemberService memberService;

    /**
     * 인증 코드 발송
     *
     * [요청]
     * POST /api/email/send
     * { "email": "user@example.com" }
     *
     * [응답]
     * { "success": true, "message": "인증 코드가 발송되었습니다." }
     */
    @PostMapping("/send")
    @Operation(summary = "인증 코드 발송", description = "입력한 이메일로 6자리 인증 코드를 발송합니다.")
    public ResponseEntity<Map<String, Object>> sendVerificationCode(@RequestBody Map<String, String> request) {
        String email = request.get("email");

        if (email == null || email.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "이메일을 입력해주세요."
            ));
        }

        // 이미 가입된 이메일인지 확인
        if (memberService.existsByEmail(email)) {
            return ResponseEntity.ok(Map.of(
                    "success", false,
                    "message", "이미 가입된 이메일입니다."
            ));
        }

        log.info("Sending verification code to: {}", email);
        boolean sent = emailService.sendVerificationCode(email);

        if (sent) {
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "인증 코드가 발송되었습니다. 이메일을 확인해주세요."
            ));
        } else {
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "이메일 발송에 실패했습니다. 잠시 후 다시 시도해주세요."
            ));
        }
    }

    /**
     * 인증 코드 검증
     *
     * [요청]
     * POST /api/email/verify
     * { "email": "user@example.com", "code": "123456" }
     *
     * [응답]
     * { "success": true, "message": "이메일이 인증되었습니다." }
     */
    @PostMapping("/verify")
    @Operation(summary = "인증 코드 검증", description = "발송된 인증 코드를 검증합니다.")
    public ResponseEntity<Map<String, Object>> verifyCode(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String code = request.get("code");

        if (email == null || code == null) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "이메일과 인증 코드를 입력해주세요."
            ));
        }

        log.info("Verifying code for email: {}", email);
        boolean verified = emailService.verifyCode(email, code);

        if (verified) {
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "이메일이 인증되었습니다."
            ));
        } else {
            return ResponseEntity.ok(Map.of(
                    "success", false,
                    "message", "인증 코드가 올바르지 않거나 만료되었습니다."
            ));
        }
    }

    /**
     * 이메일 인증 상태 확인
     *
     * [요청]
     * GET /api/email/status?email=user@example.com
     */
    @GetMapping("/status")
    @Operation(summary = "인증 상태 확인", description = "이메일 인증 완료 여부를 확인합니다.")
    public ResponseEntity<Map<String, Object>> checkStatus(@RequestParam String email) {
        boolean verified = emailService.isVerified(email);
        return ResponseEntity.ok(Map.of(
                "email", email,
                "verified", verified
        ));
    }
}
