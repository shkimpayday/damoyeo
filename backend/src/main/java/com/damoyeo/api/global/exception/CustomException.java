package com.damoyeo.api.global.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * ============================================================================
 * 커스텀 예외 클래스
 * ============================================================================
 *
 * [이 클래스의 역할]
 * 비즈니스 로직에서 발생하는 예외를 표준화된 형태로 처리합니다.
 * HTTP 상태 코드와 에러 메시지를 함께 담아 클라이언트에게 전달합니다.
 *
 * [왜 필요한가?]
 * 1. 예외를 한 곳에서 일관되게 처리하기 위해
 * 2. 프론트엔드에서 예측 가능한 에러 응답을 받기 위해
 * 3. 코드 가독성 향상 (예: throw CustomException.notFound("모임을 찾을 수 없습니다"))
 *
 * [사용 예시 - Service에서]
 * public GroupDTO findById(Long id) {
 *     return groupRepository.findById(id)
 *         .orElseThrow(() -> CustomException.notFound("모임을 찾을 수 없습니다"));
 * }
 *
 * [사용 예시 - 권한 검사]
 * if (!isOwner) {
 *     throw CustomException.forbidden("모임장만 수정할 수 있습니다");
 * }
 *
 * [에러 응답 형식]
 * HTTP 404 Not Found
 * {
 *   "error": "NOT_FOUND",
 *   "message": "모임을 찾을 수 없습니다"
 * }
 *
 * ▶ RuntimeException
 *   - 체크되지 않는 예외(Unchecked Exception)입니다.
 *   - try-catch나 throws 선언 없이 어디서든 던질 수 있습니다.
 *   - GlobalExceptionHandler에서 일괄 처리됩니다.
 */
@Getter
public class CustomException extends RuntimeException {

    /**
     * HTTP 상태 코드
     *
     * 예외 발생 시 클라이언트에게 반환할 HTTP 상태 코드입니다.
     * - 400 BAD_REQUEST: 잘못된 요청
     * - 401 UNAUTHORIZED: 인증 필요
     * - 403 FORBIDDEN: 권한 없음
     * - 404 NOT_FOUND: 리소스 없음
     */
    private final HttpStatus status;

    /**
     * 생성자
     *
     * @param message 에러 메시지 (프론트엔드에 전달됨)
     * @param status HTTP 상태 코드
     */
    public CustomException(String message, HttpStatus status) {
        super(message);  // RuntimeException에 메시지 전달
        this.status = status;
    }

    /**
     * 404 Not Found 예외 생성 (팩토리 메서드)
     *
     * 요청한 리소스를 찾을 수 없을 때 사용합니다.
     *
     * [사용 예시]
     * throw CustomException.notFound("해당 모임이 존재하지 않습니다");
     * throw CustomException.notFound("회원을 찾을 수 없습니다");
     *
     * @param message 에러 메시지
     * @return CustomException 객체
     */
    public static CustomException notFound(String message) {
        return new CustomException(message, HttpStatus.NOT_FOUND);
    }

    /**
     * 400 Bad Request 예외 생성 (팩토리 메서드)
     *
     * 클라이언트의 요청이 잘못되었을 때 사용합니다.
     *
     * [사용 예시]
     * throw CustomException.badRequest("이미 가입된 이메일입니다");
     * throw CustomException.badRequest("비밀번호가 일치하지 않습니다");
     *
     * @param message 에러 메시지
     * @return CustomException 객체
     */
    public static CustomException badRequest(String message) {
        return new CustomException(message, HttpStatus.BAD_REQUEST);
    }

    /**
     * 401 Unauthorized 예외 생성 (팩토리 메서드)
     *
     * 인증이 필요한 요청인데 인증되지 않았을 때 사용합니다.
     *
     * [사용 예시]
     * throw CustomException.unauthorized("로그인이 필요합니다");
     * throw CustomException.unauthorized("토큰이 만료되었습니다");
     *
     * @param message 에러 메시지
     * @return CustomException 객체
     */
    public static CustomException unauthorized(String message) {
        return new CustomException(message, HttpStatus.UNAUTHORIZED);
    }

    /**
     * 403 Forbidden 예외 생성 (팩토리 메서드)
     *
     * 인증은 되었지만 해당 리소스에 접근할 권한이 없을 때 사용합니다.
     *
     * [401 vs 403]
     * - 401: "당신이 누구인지 모르겠어요" (로그인 안 됨)
     * - 403: "당신이 누구인지는 알지만, 권한이 없어요"
     *
     * [사용 예시]
     * throw CustomException.forbidden("모임장만 수정할 수 있습니다");
     * throw CustomException.forbidden("해당 모임의 멤버가 아닙니다");
     *
     * @param message 에러 메시지
     * @return CustomException 객체
     */
    public static CustomException forbidden(String message) {
        return new CustomException(message, HttpStatus.FORBIDDEN);
    }
}
