package com.damoyeo.api.global.security.interceptor;

import com.damoyeo.api.global.util.JWTUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * WebSocket JWT 인증 인터셉터
 *
 * WebSocket STOMP 연결 시 JWT 토큰을 검증하여 인증된 사용자만 연결을 허용합니다.
 *
 * [처리 흐름]
 * 1. 클라이언트가 STOMP CONNECT 프레임 전송 (헤더에 Authorization: Bearer {JWT} 포함)
 * 2. 인터셉터가 CONNECT 프레임 감지
 * 3. Authorization 헤더에서 JWT 토큰 추출
 * 4. JWTUtil로 토큰 검증 (서명, 만료 시간)
 * 5. 유효하면 사용자 principal 설정, 무효하면 연결 거부
 *
 * [프론트엔드 사용 예시]
 * ```typescript
 * const client = new Client({
 *   connectHeaders: {
 *     Authorization: `Bearer ${accessToken}`
 *   }
 * });
 * ```
 *
 * [보안]
 * - CONNECT 프레임에서만 인증을 수행합니다.
 * - SEND, SUBSCRIBE 등은 이미 인증된 세션이므로 생략합니다.
 * - 토큰이 없거나 유효하지 않으면 IllegalArgumentException을 발생시켜 연결을 거부합니다.
 *
 * - WebSocketConfig.configureClientInboundChannel()
 *
 */
@RequiredArgsConstructor
@Slf4j
public class JWTChannelInterceptor implements ChannelInterceptor {

    private final JWTUtil jwtUtil;

    /**
     * 메시지 전송 전 인터셉트
     *
     * CONNECT 프레임에서만 JWT 검증을 수행합니다.
     * (SEND, SUBSCRIBE 등은 이미 인증된 세션이므로 생략)
     *
     * @param message WebSocket 메시지
     * @param channel 메시지 채널
     * @return 처리된 메시지 (인증 정보 포함)
     * @throws IllegalArgumentException 토큰이 없거나 유효하지 않은 경우
     */
    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        // STOMP 헤더 접근자 생성
        StompHeaderAccessor accessor = MessageHeaderAccessor
                .getAccessor(message, StompHeaderAccessor.class);

        // CONNECT 프레임에서만 인증 수행
        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            // Authorization 헤더 추출
            String authHeader = accessor.getFirstNativeHeader("Authorization");

            log.debug("[WebSocket] CONNECT 시도 - Authorization: {}",
                authHeader != null ? "있음" : "없음");

            // Authorization 헤더 검증
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                log.warn("[WebSocket] JWT 토큰이 없거나 형식이 잘못되었습니다");
                throw new IllegalArgumentException("Missing or invalid Authorization header");
            }

            // "Bearer " 제거하고 토큰만 추출
            String token = authHeader.substring(7);

            try {
                // JWT 토큰 검증 및 사용자 정보 추출
                Map<String, Object> claims = jwtUtil.validateToken(token);
                String email = (String) claims.get("email");
                @SuppressWarnings("unchecked")
                List<String> roleNames = (List<String>) claims.get("roleNames");

                log.info("[WebSocket] JWT 검증 성공 - email: {}, roles: {}", email, roleNames);

                // 권한 목록 생성
                List<SimpleGrantedAuthority> authorities = roleNames != null
                        ? roleNames.stream()
                            .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                            .toList()
                        : Collections.emptyList();

                // 사용자 principal 설정
                // @MessageMapping에서 Principal 파라미터로 접근 가능
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                email,      // principal (사용자 식별자)
                                null,       // credentials (패스워드, WebSocket에서는 불필요)
                                authorities // 권한 목록
                        );

                accessor.setUser(authentication);

                log.info("[WebSocket] 사용자 인증 완료 - email: {}", email);

            } catch (Exception e) {
                log.error("[WebSocket] JWT 토큰 검증 실패: {}", e.getMessage());
                throw new IllegalArgumentException("Invalid JWT token: " + e.getMessage());
            }
        }

        return message;
    }
}
