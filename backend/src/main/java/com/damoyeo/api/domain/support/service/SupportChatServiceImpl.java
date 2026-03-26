package com.damoyeo.api.domain.support.service;

import com.damoyeo.api.domain.member.dto.MemberSummaryDTO;
import com.damoyeo.api.domain.member.entity.Member;
import com.damoyeo.api.domain.member.entity.MemberRole;
import com.damoyeo.api.domain.member.repository.MemberRepository;
import com.damoyeo.api.domain.support.dto.CreateSupportChatRequest;
import com.damoyeo.api.domain.support.dto.SupportChatDTO;
import com.damoyeo.api.domain.support.dto.SupportMessageDTO;
import com.damoyeo.api.domain.support.entity.SupportChat;
import com.damoyeo.api.domain.support.entity.SupportChatStatus;
import com.damoyeo.api.domain.support.entity.SupportMessage;
import com.damoyeo.api.domain.support.repository.SupportChatRepository;
import com.damoyeo.api.domain.support.repository.SupportMessageRepository;
import com.damoyeo.api.global.common.dto.PageRequestDTO;
import com.damoyeo.api.global.common.dto.PageResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * ============================================================================
 * 상담 채팅 서비스 구현체
 * ============================================================================
 *
 * <p>상담 채팅 관련 비즈니스 로직을 구현합니다.</p>
 *
 * <h3>트랜잭션 정책:</h3>
 * <ul>
 *   <li>조회: @Transactional(readOnly = true)</li>
 *   <li>수정: @Transactional</li>
 * </ul>
 *
 * @author damoyeo
 * @since 2025-03-16
 */
@Service
@Log4j2
@RequiredArgsConstructor
@Transactional
public class SupportChatServiceImpl implements SupportChatService {

    private final SupportChatRepository supportChatRepository;
    private final SupportMessageRepository supportMessageRepository;
    private final MemberRepository memberRepository;

    // ========================================================================
    // 사용자 기능
    // ========================================================================

    /**
     * {@inheritDoc}
     *
     * <p>상담 생성 프로세스:</p>
     * <ol>
     *   <li>사용자 조회</li>
     *   <li>진행 중인 상담 여부 확인</li>
     *   <li>새 상담 엔티티 생성 (WAITING 상태)</li>
     *   <li>첫 메시지 저장</li>
     *   <li>DTO 변환 및 반환</li>
     * </ol>
     */
    @Override
    public SupportChatDTO createSupportChat(String email, CreateSupportChatRequest request) {
        log.info("상담 생성 요청: email={}, title={}", email, request.getTitle());

        // 1. 사용자 조회
        Member user = getMemberByEmail(email);

        // 2. 진행 중인 상담 확인
        supportChatRepository.findActiveByUserId(user.getId(),
                        Arrays.asList(SupportChatStatus.WAITING, SupportChatStatus.IN_PROGRESS))
                .ifPresent(chat -> {
                    throw new IllegalStateException("이미 진행 중인 상담이 있습니다.");
                });

        // 3. 상담 엔티티 생성
        SupportChat supportChat = SupportChat.builder()
                .user(user)
                .title(request.getTitle())
                .status(SupportChatStatus.WAITING)
                .build();
        supportChatRepository.save(supportChat);

        // 4. 첫 메시지 저장
        SupportMessage firstMessage = SupportMessage.builder()
                .supportChat(supportChat)
                .sender(user)
                .message(request.getMessage())
                .isAdmin(false)
                .build();
        supportMessageRepository.save(firstMessage);

        log.info("상담 생성 완료: chatId={}", supportChat.getId());

        // 5. DTO 반환
        return entityToDTO(supportChat, firstMessage);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<SupportChatDTO> getMySupportChats(String email) {
        Member user = getMemberByEmail(email);
        List<SupportChat> chats = supportChatRepository.findByUserId(user.getId());

        return chats.stream()
                .map(chat -> {
                    SupportMessage latestMessage = supportMessageRepository
                            .findLatestBySupportChatId(chat.getId())
                            .orElse(null);
                    return entityToDTO(chat, latestMessage);
                })
                .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public SupportChatDTO getActiveSupportChat(String email) {
        Member user = getMemberByEmail(email);
        return supportChatRepository.findActiveByUserId(user.getId(),
                        Arrays.asList(SupportChatStatus.WAITING, SupportChatStatus.IN_PROGRESS))
                .map(chat -> {
                    SupportMessage latestMessage = supportMessageRepository
                            .findLatestBySupportChatId(chat.getId())
                            .orElse(null);
                    return entityToDTO(chat, latestMessage);
                })
                .orElse(null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public SupportChatDTO getSupportChatDetail(Long supportChatId, String email) {
        SupportChat chat = getSupportChatById(supportChatId);
        validateAccess(supportChatId, email);

        SupportMessage latestMessage = supportMessageRepository
                .findLatestBySupportChatId(chat.getId())
                .orElse(null);

        return entityToDTO(chat, latestMessage);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SupportMessageDTO sendUserMessage(Long supportChatId, String email, String message) {
        SupportChat chat = getSupportChatById(supportChatId);
        Member user = getMemberByEmail(email);

        // 본인 상담인지 확인
        if (!chat.getUser().getId().equals(user.getId())) {
            throw new SecurityException("본인의 상담에만 메시지를 보낼 수 있습니다.");
        }

        // 종료된 상담인지 확인
        if (chat.getStatus() == SupportChatStatus.COMPLETED) {
            throw new IllegalStateException("종료된 상담에는 메시지를 보낼 수 없습니다.");
        }

        // 메시지 저장
        SupportMessage supportMessage = SupportMessage.builder()
                .supportChat(chat)
                .sender(user)
                .message(message)
                .isAdmin(false)
                .build();
        supportMessageRepository.save(supportMessage);

        log.info("사용자 메시지 전송: chatId={}, userId={}", supportChatId, user.getId());

        return messageEntityToDTO(supportMessage);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void rateSupportChat(Long supportChatId, String email, int rating) {
        SupportChat chat = getSupportChatById(supportChatId);
        Member user = getMemberByEmail(email);

        // 본인 상담인지 확인
        if (!chat.getUser().getId().equals(user.getId())) {
            throw new SecurityException("본인의 상담만 평가할 수 있습니다.");
        }

        // 완료된 상담인지 확인
        if (chat.getStatus() != SupportChatStatus.COMPLETED) {
            throw new IllegalStateException("완료된 상담만 평가할 수 있습니다.");
        }

        chat.rate(rating);
        log.info("상담 평가 완료: chatId={}, rating={}", supportChatId, rating);
    }

    // ========================================================================
    // 관리자 기능
    // ========================================================================

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public PageResponseDTO<SupportChatDTO> getWaitingSupportChats(PageRequestDTO pageRequest) {
        Pageable pageable = PageRequest.of(
                pageRequest.getPage() - 1,
                pageRequest.getSize(),
                Sort.by(Sort.Direction.ASC, "createdAt")
        );

        Page<SupportChat> page = supportChatRepository.findByStatus(
                SupportChatStatus.WAITING, pageable);

        List<SupportChatDTO> dtoList = page.getContent().stream()
                .map(chat -> {
                    SupportMessage latestMessage = supportMessageRepository
                            .findLatestBySupportChatId(chat.getId())
                            .orElse(null);
                    return entityToDTO(chat, latestMessage);
                })
                .collect(Collectors.toList());

        return PageResponseDTO.<SupportChatDTO>builder()
                .pageRequestDTO(pageRequest)
                .dtoList(dtoList)
                .totalCount((int) page.getTotalElements())
                .build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public PageResponseDTO<SupportChatDTO> getMyAssignedChats(String adminEmail, PageRequestDTO pageRequest) {
        Member admin = getMemberByEmail(adminEmail);
        validateAdmin(admin);

        Pageable pageable = PageRequest.of(
                pageRequest.getPage() - 1,
                pageRequest.getSize()
        );

        Page<SupportChat> page = supportChatRepository.findByAdminId(admin.getId(), pageable);

        List<SupportChatDTO> dtoList = page.getContent().stream()
                .map(chat -> {
                    SupportMessage latestMessage = supportMessageRepository
                            .findLatestBySupportChatId(chat.getId())
                            .orElse(null);
                    return entityToDTO(chat, latestMessage);
                })
                .collect(Collectors.toList());

        return PageResponseDTO.<SupportChatDTO>builder()
                .pageRequestDTO(pageRequest)
                .dtoList(dtoList)
                .totalCount((int) page.getTotalElements())
                .build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public PageResponseDTO<SupportChatDTO> getAllSupportChats(PageRequestDTO pageRequest) {
        Pageable pageable = PageRequest.of(
                pageRequest.getPage() - 1,
                pageRequest.getSize()
        );

        Page<SupportChat> page = supportChatRepository.findAllWithDetails(pageable);

        List<SupportChatDTO> dtoList = page.getContent().stream()
                .map(chat -> {
                    SupportMessage latestMessage = supportMessageRepository
                            .findLatestBySupportChatId(chat.getId())
                            .orElse(null);
                    return entityToDTO(chat, latestMessage);
                })
                .collect(Collectors.toList());

        return PageResponseDTO.<SupportChatDTO>builder()
                .pageRequestDTO(pageRequest)
                .dtoList(dtoList)
                .totalCount((int) page.getTotalElements())
                .build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SupportChatDTO assignSupportChat(Long supportChatId, String adminEmail) {
        SupportChat chat = getSupportChatById(supportChatId);
        Member admin = getMemberByEmail(adminEmail);
        validateAdmin(admin);

        // 대기 중인 상담인지 확인
        if (chat.getStatus() != SupportChatStatus.WAITING) {
            throw new IllegalStateException("대기 중인 상담만 배정할 수 있습니다.");
        }

        // 관리자 배정
        chat.assignAdmin(admin);

        // 시스템 메시지 추가 (선택적)
        SupportMessage systemMessage = SupportMessage.builder()
                .supportChat(chat)
                .sender(admin)
                .message("상담사 " + admin.getNickname() + "님이 상담을 시작합니다.")
                .isAdmin(true)
                .build();
        supportMessageRepository.save(systemMessage);

        log.info("상담 배정 완료: chatId={}, adminId={}", supportChatId, admin.getId());

        return entityToDTO(chat, systemMessage);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SupportMessageDTO sendAdminMessage(Long supportChatId, String adminEmail, String message) {
        SupportChat chat = getSupportChatById(supportChatId);
        Member admin = getMemberByEmail(adminEmail);
        validateAdmin(admin);

        // 담당 관리자인지 확인
        if (chat.getAdmin() == null || !chat.getAdmin().getId().equals(admin.getId())) {
            throw new SecurityException("담당 관리자만 메시지를 보낼 수 있습니다.");
        }

        // 종료된 상담인지 확인
        if (chat.getStatus() == SupportChatStatus.COMPLETED) {
            throw new IllegalStateException("종료된 상담에는 메시지를 보낼 수 없습니다.");
        }

        // 메시지 저장
        SupportMessage supportMessage = SupportMessage.builder()
                .supportChat(chat)
                .sender(admin)
                .message(message)
                .isAdmin(true)
                .build();
        supportMessageRepository.save(supportMessage);

        log.info("관리자 메시지 전송: chatId={}, adminId={}", supportChatId, admin.getId());

        return messageEntityToDTO(supportMessage);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SupportChatDTO completeSupportChat(Long supportChatId, String adminEmail) {
        SupportChat chat = getSupportChatById(supportChatId);
        Member admin = getMemberByEmail(adminEmail);
        validateAdmin(admin);

        // 담당 관리자인지 확인
        if (chat.getAdmin() == null || !chat.getAdmin().getId().equals(admin.getId())) {
            throw new SecurityException("담당 관리자만 상담을 완료할 수 있습니다.");
        }

        // 진행 중인 상담인지 확인
        if (chat.getStatus() != SupportChatStatus.IN_PROGRESS) {
            throw new IllegalStateException("진행 중인 상담만 완료할 수 있습니다.");
        }

        // 상담 완료
        chat.complete();

        // 종료 메시지 추가
        SupportMessage closeMessage = SupportMessage.builder()
                .supportChat(chat)
                .sender(admin)
                .message("상담이 종료되었습니다. 서비스 이용에 만족하셨다면 평가를 부탁드립니다.")
                .isAdmin(true)
                .build();
        supportMessageRepository.save(closeMessage);

        log.info("상담 완료: chatId={}", supportChatId);

        return entityToDTO(chat, closeMessage);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public long getWaitingCount() {
        return supportChatRepository.countByStatus(SupportChatStatus.WAITING);
    }

    // ========================================================================
    // 공통 기능
    // ========================================================================

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public PageResponseDTO<SupportMessageDTO> getMessages(
            Long supportChatId, String email, PageRequestDTO pageRequest) {

        validateAccess(supportChatId, email);

        Pageable pageable = PageRequest.of(
                pageRequest.getPage() - 1,
                pageRequest.getSize(),
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        Page<SupportMessage> page = supportMessageRepository.findBySupportChatId(
                supportChatId, pageable);

        List<SupportMessageDTO> dtoList = page.getContent().stream()
                .map(this::messageEntityToDTO)
                .collect(Collectors.toList());

        return PageResponseDTO.<SupportMessageDTO>builder()
                .pageRequestDTO(pageRequest)
                .dtoList(dtoList)
                .totalCount((int) page.getTotalElements())
                .build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public void validateAccess(Long supportChatId, String email) {
        SupportChat chat = getSupportChatById(supportChatId);
        Member member = getMemberByEmail(email);

        // 관리자이거나 상담 요청자인 경우 접근 허용
        boolean isAdmin = member.getMemberRoleList().contains(MemberRole.ADMIN);
        boolean isUser = chat.getUser().getId().equals(member.getId());
        boolean isAssignedAdmin = chat.getAdmin() != null &&
                chat.getAdmin().getId().equals(member.getId());

        if (!isAdmin && !isUser && !isAssignedAdmin) {
            throw new SecurityException("해당 상담에 접근 권한이 없습니다.");
        }
    }

    // ========================================================================
    // 헬퍼 메서드
    // ========================================================================

    /**
     * 이메일로 회원 조회
     *
     * @param email 이메일
     * @return Member 엔티티
     * @throws IllegalArgumentException 회원이 존재하지 않는 경우
     */
    private Member getMemberByEmail(String email) {
        return memberRepository.getWithRoles(email)
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다: " + email));
    }

    /**
     * ID로 상담 조회
     *
     * @param supportChatId 상담 ID
     * @return SupportChat 엔티티
     * @throws IllegalArgumentException 상담이 존재하지 않는 경우
     */
    private SupportChat getSupportChatById(Long supportChatId) {
        return supportChatRepository.findById(supportChatId)
                .orElseThrow(() -> new IllegalArgumentException("상담을 찾을 수 없습니다: " + supportChatId));
    }

    /**
     * 관리자 권한 검증
     *
     * @param member 검증할 회원
     * @throws SecurityException 관리자가 아닌 경우
     */
    private void validateAdmin(Member member) {
        if (!member.getMemberRoleList().contains(MemberRole.ADMIN)) {
            throw new SecurityException("관리자 권한이 필요합니다.");
        }
    }

    /**
     * SupportChat 엔티티를 DTO로 변환
     *
     * @param entity 상담 엔티티
     * @param latestMessage 최신 메시지 (nullable)
     * @return SupportChatDTO
     */
    private SupportChatDTO entityToDTO(SupportChat entity, SupportMessage latestMessage) {
        return SupportChatDTO.builder()
                .id(entity.getId())
                .user(MemberSummaryDTO.from(entity.getUser()))
                .admin(entity.getAdmin() != null ? MemberSummaryDTO.from(entity.getAdmin()) : null)
                .title(entity.getTitle())
                .status(entity.getStatus())
                .latestMessage(latestMessage != null ? messageEntityToDTO(latestMessage) : null)
                .unreadCount(0)
                .createdAt(entity.getCreatedAt())
                .completedAt(entity.getCompletedAt())
                .rating(entity.getRating())
                .build();
    }

    /**
     * SupportMessage 엔티티를 DTO로 변환
     *
     * @param entity 메시지 엔티티
     * @return SupportMessageDTO
     */
    private SupportMessageDTO messageEntityToDTO(SupportMessage entity) {
        return SupportMessageDTO.builder()
                .id(entity.getId())
                .supportChatId(entity.getSupportChat().getId())
                .sender(MemberSummaryDTO.from(entity.getSender()))
                .message(entity.getMessage())
                .isAdmin(entity.isAdmin())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
