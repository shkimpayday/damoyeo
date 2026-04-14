package com.damoyeo.api.domain.support.service;

import com.damoyeo.api.domain.support.dto.CreateSupportChatRequest;
import com.damoyeo.api.domain.support.dto.SupportChatDTO;
import com.damoyeo.api.domain.support.dto.SupportMessageDTO;
import com.damoyeo.api.global.common.dto.PageRequestDTO;
import com.damoyeo.api.global.common.dto.PageResponseDTO;

import java.util.List;

/**
 * 상담 채팅 서비스 인터페이스
 *
 * <p>상담 채팅 관련 비즈니스 로직을 정의합니다.</p>
 *
 * <h3>주요 기능:</h3>
 * <ul>
 *   <li>상담 생성 및 관리</li>
 *   <li>실시간 메시지 송수신</li>
 *   <li>관리자 배정 및 상담 상태 관리</li>
 *   <li>상담 완료 및 평가</li>
 * </ul>
 *
 */
public interface SupportChatService {

    // 사용자 기능

    /**
     * 새 상담 시작
     *
     * <p>사용자가 새로운 상담을 시작합니다. 이미 진행 중인 상담이 있으면 예외가 발생합니다.</p>
     *
     * @param email 사용자 이메일
     * @param request 상담 생성 요청 (제목, 첫 메시지)
     * @return 생성된 상담 정보
     * @throws IllegalStateException 이미 진행 중인 상담이 있는 경우
     */
    SupportChatDTO createSupportChat(String email, CreateSupportChatRequest request);

    /**
     * 사용자의 상담 목록 조회
     *
     * @param email 사용자 이메일
     * @return 상담 목록 (최신순)
     */
    List<SupportChatDTO> getMySupportChats(String email);

    /**
     * 사용자의 활성 상담 조회 (진행 중인 상담)
     *
     * @param email 사용자 이메일
     * @return 진행 중인 상담 (없으면 null)
     */
    SupportChatDTO getActiveSupportChat(String email);

    /**
     * 상담 상세 조회
     *
     * @param supportChatId 상담 ID
     * @param email 요청자 이메일
     * @return 상담 상세 정보
     * @throws IllegalArgumentException 상담이 존재하지 않는 경우
     * @throws SecurityException 접근 권한이 없는 경우
     */
    SupportChatDTO getSupportChatDetail(Long supportChatId, String email);

    /**
     * 사용자 메시지 전송
     *
     * @param supportChatId 상담 ID
     * @param email 발신자 이메일
     * @param message 메시지 내용
     * @return 저장된 메시지 정보
     * @throws IllegalStateException 상담이 종료된 경우
     */
    SupportMessageDTO sendUserMessage(Long supportChatId, String email, String message);

    /**
     * 상담 평가
     *
     * @param supportChatId 상담 ID
     * @param email 사용자 이메일
     * @param rating 평점 (1-5)
     * @throws IllegalStateException 상담이 완료되지 않은 경우
     */
    void rateSupportChat(Long supportChatId, String email, int rating);

    // 관리자 기능

    /**
     * 대기 중인 상담 목록 조회 (관리자용)
     *
     * @param pageRequest 페이지 요청 정보
     * @return 대기 중인 상담 목록
     */
    PageResponseDTO<SupportChatDTO> getWaitingSupportChats(PageRequestDTO pageRequest);

    /**
     * 진행 중인 상담 목록 조회 (관리자용)
     *
     * @param adminEmail 관리자 이메일
     * @param pageRequest 페이지 요청 정보
     * @return 해당 관리자가 담당 중인 상담 목록
     */
    PageResponseDTO<SupportChatDTO> getMyAssignedChats(String adminEmail, PageRequestDTO pageRequest);

    /**
     * 전체 상담 목록 조회 (관리자용)
     *
     * @param pageRequest 페이지 요청 정보
     * @return 전체 상담 목록
     */
    PageResponseDTO<SupportChatDTO> getAllSupportChats(PageRequestDTO pageRequest);

    /**
     * 상담 배정 (관리자가 상담을 가져감)
     *
     * @param supportChatId 상담 ID
     * @param adminEmail 관리자 이메일
     * @return 업데이트된 상담 정보
     * @throws IllegalStateException 이미 다른 관리자가 배정된 경우
     */
    SupportChatDTO assignSupportChat(Long supportChatId, String adminEmail);

    /**
     * 관리자 메시지 전송
     *
     * @param supportChatId 상담 ID
     * @param adminEmail 관리자 이메일
     * @param message 메시지 내용
     * @return 저장된 메시지 정보
     * @throws SecurityException 해당 상담의 담당 관리자가 아닌 경우
     */
    SupportMessageDTO sendAdminMessage(Long supportChatId, String adminEmail, String message);

    /**
     * 상담 완료 처리
     *
     * @param supportChatId 상담 ID
     * @param adminEmail 관리자 이메일
     * @return 업데이트된 상담 정보
     * @throws SecurityException 해당 상담의 담당 관리자가 아닌 경우
     */
    SupportChatDTO completeSupportChat(Long supportChatId, String adminEmail);

    /**
     * 대기 중인 상담 개수 조회
     *
     * @return 대기 중인 상담 개수
     */
    long getWaitingCount();

    // 공통 기능

    /**
     * 상담 메시지 히스토리 조회
     *
     * @param supportChatId 상담 ID
     * @param email 요청자 이메일
     * @param pageRequest 페이지 요청 정보
     * @return 메시지 목록 (최신순)
     */
    PageResponseDTO<SupportMessageDTO> getMessages(Long supportChatId, String email, PageRequestDTO pageRequest);

    /**
     * 상담 접근 권한 검증
     *
     * @param supportChatId 상담 ID
     * @param email 요청자 이메일
     * @throws SecurityException 접근 권한이 없는 경우
     */
    void validateAccess(Long supportChatId, String email);
}
