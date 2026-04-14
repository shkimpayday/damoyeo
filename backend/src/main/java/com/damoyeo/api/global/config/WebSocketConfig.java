package com.damoyeo.api.global.config;

import com.damoyeo.api.global.security.interceptor.JWTChannelInterceptor;
import com.damoyeo.api.global.util.JWTUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket & STOMP 설정
 *
 * [STOMP란?]
 * Simple Text Oriented Messaging Protocol
 * - WebSocket 위에서 동작하는 메시징 프로토콜
 * - pub/sub 패턴 지원 (브로드캐스트가 쉬움)
 * - Spring이 공식 지원
 *
 * [Message Broker]
 * 메시지를 받아서 구독자들에게 전달하는 중계자
 *
 * [엔드포인트 구조]
 * - /ws: WebSocket 연결 엔드포인트 (Handshake)
 * - /app: 클라이언트 → 서버 메시지 전송 (Application Destination)
 * - /topic: 1:N 브로드캐스트 (채팅방 구독)
 * - /queue: 1:1 메시지 (에러 메시지 등)
 * - /user: 특정 사용자에게만 전송
 *
 * [클라이언트 사용 예시]
 * ```typescript
 * // 1. WebSocket 연결
 * const client = new Client({
 *   webSocketFactory: () => new SockJS('http://localhost:8080/ws'),
 *   connectHeaders: {
 *     Authorization: 'Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...'
 *   }
 * });
 *
 * // 2. 채팅방 구독
 * client.subscribe('/topic/chat/1', (message) => {
 *   console.log('새 메시지:', JSON.parse(message.body));
 * });
 *
 * // 3. 메시지 전송
 * client.publish({
 *   destination: '/app/chat/1',
 *   body: JSON.stringify({ message: '안녕하세요' })
 * });
 * ```
 *
 * [보안]
 * - JWTChannelInterceptor로 CONNECT 시 JWT 토큰 검증
 * - SecurityConfig에서 /ws/** 경로 허용 필요
 *
 * [확장 고려사항]
 * - Production: RabbitMQ, ActiveMQ 같은 외부 브로커 사용 권장 (다중 서버 환경)
 * - 현재: Simple Broker (메모리 기반, 단일 서버 환경용)
 *
 */
@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final JWTUtil jwtUtil;

    /**
     * 허용할 WebSocket CORS 출처 (콤마 구분)
     * SecurityConfig의 cors.allowed-origins와 동일한 값 사용
     */
    @Value("${cors.allowed-origins:http://localhost:5173,http://localhost:3000}")
    private String allowedOriginsRaw;

    /**
     * Message Broker 설정
     *
     * [브로커 역할]
     * 메시지를 받아서 구독자들에게 전달하는 중계자
     *
     * [Simple Broker vs External Broker]
     * - Simple Broker: 메모리 기반, 단일 서버에서만 동작
     * - External Broker (RabbitMQ, ActiveMQ): 다중 서버 환경에서 사용
     *
     * @param config 브로커 레지스트리
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Simple Broker 활성화 (메모리 기반)
        // Production: RabbitMQ, ActiveMQ 같은 외부 브로커 사용 권장
        config.enableSimpleBroker("/topic", "/queue");

        // 클라이언트가 서버로 메시지 보낼 때 prefix
        // 예: client.publish({ destination: '/app/chat/1' })
        config.setApplicationDestinationPrefixes("/app");

        // 특정 사용자에게만 메시지 보낼 때 prefix
        // 예: messagingTemplate.convertAndSendToUser(email, "/queue/errors", error)
        config.setUserDestinationPrefix("/user");
    }

    /**
     * STOMP 엔드포인트 등록
     *
     * [엔드포인트]
     * 클라이언트가 WebSocket 연결을 시작하는 URL
     * 예: ws://localhost:8080/ws
     *
     * [SockJS]
     * WebSocket을 지원하지 않는 브라우저를 위한 폴백
     * - Long Polling, Server-Sent Events 등으로 대체
     *
     * [CORS]
     * 프론트엔드가 다른 도메인(localhost:5173)에서 실행되므로
     * allowedOrigins 설정 필요
     *
     * @param registry STOMP 엔드포인트 레지스트리
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 배포 시: CORS_ALLOWED_ORIGINS 환경변수 또는 cors.allowed-origins 프로퍼티로 설정
        String[] allowedOrigins = allowedOriginsRaw.split(",");
        registry.addEndpoint("/ws")
                // CORS 설정: 환경변수로 제어되는 프론트엔드 도메인 허용
                .setAllowedOrigins(allowedOrigins)
                // SockJS 폴백 활성화 (WebSocket 미지원 브라우저 대응)
                .withSockJS();
    }

    /**
     * 인바운드 채널 인터셉터 등록
     *
     * [목적]
     * 클라이언트 → 서버 메시지에 대해 JWT 인증 수행
     * CONNECT 프레임에서 JWT 토큰 검증
     *
     * [처리 흐름]
     * 1. 클라이언트: CONNECT 프레임 전송 (Authorization 헤더에 JWT 포함)
     * 2. JWTChannelInterceptor: JWT 토큰 검증
     * 3. 유효하면 사용자 principal 설정, 무효하면 연결 거부
     * 4. 이후 SEND, SUBSCRIBE 등은 인증된 세션으로 처리
     *
     * @param registration 채널 등록
     */
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        // JWT 인증 인터셉터 등록
        registration.interceptors(new JWTChannelInterceptor(jwtUtil));
    }
}
