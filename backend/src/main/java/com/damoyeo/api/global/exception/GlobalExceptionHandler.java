package com.damoyeo.api.global.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * ============================================================================
 * 전역 예외 처리 핸들러
 * ============================================================================
 *
 * [이 클래스의 역할]
 * 애플리케이션 전체에서 발생하는 예외를 한 곳에서 처리합니다.
 * Controller에서 발생한 예외를 가로채서 적절한 HTTP 응답으로 변환합니다.
 *
 * [왜 필요한가?]
 * 1. 모든 Controller에서 try-catch를 반복하지 않아도 됨
 * 2. 에러 응답 형식을 일관되게 유지할 수 있음
 * 3. 에러 로깅을 한 곳에서 관리할 수 있음
 * 4. 프론트엔드가 예측 가능한 에러 응답을 받을 수 있음
 *
 * [처리 순서]
 * 예외가 발생하면 가장 구체적인 타입부터 매칭합니다.
 * 예: CustomException → MethodArgumentNotValidException → ... → Exception
 *
 * ▶ @RestControllerAdvice
 *   - 모든 @RestController에서 발생한 예외를 처리합니다.
 *   - @ExceptionHandler 메서드의 반환값을 자동으로 JSON으로 변환합니다.
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 커스텀 예외 처리
     *
     * Service나 Controller에서 의도적으로 던진 예외를 처리합니다.
     * CustomException에 지정된 HTTP 상태 코드와 메시지를 그대로 반환합니다.
     *
     * [사용 예시]
     * Service: throw CustomException.notFound("모임을 찾을 수 없습니다")
     * 응답: HTTP 404 { "error": "모임을 찾을 수 없습니다" }
     *
     * @param e 발생한 CustomException
     * @return HTTP 응답 (상태 코드 + 에러 메시지)
     */
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<Map<String, String>> handleCustomException(CustomException e) {
        log.error("CustomException: {}", e.getMessage());
        Map<String, String> error = new HashMap<>();
        error.put("error", e.getMessage());
        return ResponseEntity.status(e.getStatus()).body(error);
    }

    /**
     * 유효성 검사 실패 예외 처리
     *
     * @Valid 어노테이션으로 검증 실패 시 발생하는 예외를 처리합니다.
     * 각 필드별로 어떤 검증이 실패했는지 반환합니다.
     *
     * [발생 상황 예시]
     * DTO:
     *   @NotBlank(message = "이름은 필수입니다")
     *   private String name;
     *
     * Controller:
     *   public ResponseEntity<?> create(@Valid @RequestBody GroupCreateRequest request)
     *
     * 요청: { "name": "" }
     * 응답: HTTP 400 { "name": "이름은 필수입니다" }
     *
     * @param e 유효성 검사 실패 예외
     * @return HTTP 400 + 필드별 에러 메시지
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationException(MethodArgumentNotValidException e) {
        log.error("Validation error: {}", e.getMessage());
        Map<String, String> errors = new HashMap<>();
        // 모든 필드 에러를 Map으로 수집
        e.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
        );
        return ResponseEntity.badRequest().body(errors);
    }

    /**
     * 인증 실패 예외 처리
     *
     * 로그인 시 이메일 또는 비밀번호가 틀렸을 때 발생합니다.
     *
     * [발생 상황]
     * - 존재하지 않는 이메일로 로그인 시도
     * - 비밀번호가 일치하지 않을 때
     *
     * [보안 고려사항]
     * 구체적인 에러 메시지(이메일 없음/비밀번호 틀림)를 주지 않습니다.
     * 공격자에게 힌트를 주지 않기 위함입니다.
     *
     * @param e 인증 실패 예외
     * @return HTTP 401 + 에러 메시지
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, String>> handleBadCredentialsException(BadCredentialsException e) {
        log.error("Bad credentials: {}", e.getMessage());
        Map<String, String> error = new HashMap<>();
        error.put("error", "이메일 또는 비밀번호가 올바르지 않습니다.");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    /**
     * 권한 거부 예외 처리
     *
     * @PreAuthorize 등의 권한 검사에 실패했을 때 발생합니다.
     *
     * [발생 상황 예시]
     * Controller:
     *   @PreAuthorize("hasRole('ADMIN')")
     *   public void deleteUser() { ... }
     *
     * 일반 USER 권한으로 접근 시 AccessDeniedException 발생
     *
     * @param e 권한 거부 예외
     * @return HTTP 403 + 에러 메시지
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, String>> handleAccessDeniedException(AccessDeniedException e) {
        log.error("Access denied: {}", e.getMessage());
        Map<String, String> error = new HashMap<>();
        error.put("error", "접근 권한이 없습니다.");
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    /**
     * 기타 모든 예외 처리 (최후의 방어선)
     *
     * 위에서 처리되지 않은 모든 예외를 처리합니다.
     * 예상치 못한 서버 오류를 사용자에게 친화적인 메시지로 반환합니다.
     *
     * [주의]
     * 실제 에러 내용은 로그에만 기록하고,
     * 사용자에게는 일반적인 메시지만 반환합니다.
     * (보안상 내부 에러 내용을 노출하면 안 됨)
     *
     * @param e 발생한 예외
     * @return HTTP 500 + 일반적인 에러 메시지
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleException(Exception e) {
        log.error("Unexpected error: ", e);  // 스택 트레이스 포함 로깅
        Map<String, String> error = new HashMap<>();
        error.put("error", "서버 오류가 발생했습니다.");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
