package com.damoyeo.api.global.security.handler;

import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

/**
 * 로그인 실패 핸들러
 *
 * [이 클래스의 역할]
 * Spring Security가 로그인 인증에 실패했을 때 호출됩니다.
 * 401 상태 코드와 함께 에러 메시지를 JSON으로 반환합니다.
 *
 * [호출 시점]
 * POST /api/member/login 요청이 실패했을 때
 * - 이메일이 존재하지 않거나
 * - 비밀번호가 일치하지 않을 때
 *
 * [사용되는 곳]
 * - SecurityConfig.filterChain()에서 .failureHandler()로 등록됨
 *
 * ▶ AuthenticationFailureHandler
 *   - Spring Security에서 제공하는 인터페이스입니다.
 *   - 인증 실패 시 실행할 로직을 정의합니다.
 */
@Slf4j
public class APILoginFailHandler implements AuthenticationFailureHandler {

    /**
     * 로그인 실패 시 호출되는 메서드
     *
     * @param request HTTP 요청 (사용하지 않음)
     * @param response HTTP 응답 (에러 JSON 반환)
     * @param exception 인증 실패 원인을 담은 예외
     *
     * [응답 형식]
     * HTTP 401 Unauthorized
     * {
     *   "error": "LOGIN_FAILED",
     *   "message": "이메일 또는 비밀번호가 올바르지 않습니다."
     * }
     *
     * [프론트엔드 처리]
     * 프론트엔드에서 이 응답을 받으면 로그인 폼에 에러 메시지를 표시합니다.
     *
     * [보안 고려사항]
     * "이메일이 없습니다" 또는 "비밀번호가 틀렸습니다" 처럼
     * 구체적인 에러 메시지를 주지 않습니다.
     * 구체적인 메시지는 공격자에게 힌트를 줄 수 있기 때문입니다.
     * 예: "이메일이 없습니다" → 공격자가 존재하는 이메일을 파악할 수 있음
     */
    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {

        log.info("Login failed: {}", exception.getMessage());

        // 401 Unauthorized 상태 코드 설정
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");

        // JSON 에러 응답 전송
        PrintWriter writer = response.getWriter();
        Gson gson = new Gson();
        writer.print(gson.toJson(Map.of(
                "error", "LOGIN_FAILED",
                "message", "이메일 또는 비밀번호가 올바르지 않습니다."
        )));
        writer.close();
    }
}
