package com.damoyeo.api.domain.chat.service;

import com.damoyeo.api.domain.chat.dto.ChatMessageDTO;
import com.damoyeo.api.domain.chat.dto.ChatRoomDTO;
import com.damoyeo.api.domain.chat.entity.ChatMessage;
import com.damoyeo.api.domain.chat.entity.ChatRead;
import com.damoyeo.api.domain.chat.entity.MessageType;
import com.damoyeo.api.domain.chat.repository.ChatMessageRepository;
import com.damoyeo.api.domain.chat.repository.ChatReadRepository;
import com.damoyeo.api.domain.group.entity.Group;
import com.damoyeo.api.domain.group.entity.GroupMember;
import com.damoyeo.api.domain.group.entity.GroupRole;
import com.damoyeo.api.domain.group.entity.JoinStatus;
import com.damoyeo.api.domain.group.repository.GroupMemberRepository;
import com.damoyeo.api.domain.group.repository.GroupRepository;
import com.damoyeo.api.domain.meeting.entity.Meeting;
import com.damoyeo.api.domain.meeting.repository.MeetingAttendeeRepository;
import com.damoyeo.api.domain.meeting.repository.MeetingRepository;
import com.damoyeo.api.domain.member.dto.MemberSummaryDTO;
import com.damoyeo.api.domain.member.entity.Member;
import com.damoyeo.api.domain.member.repository.MemberRepository;
import com.damoyeo.api.global.common.dto.PageRequestDTO;
import com.damoyeo.api.global.common.dto.PageResponseDTO;
import com.damoyeo.api.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * ============================================================================
 * 채팅 서비스 구현체
 * ============================================================================
 *
 * [역할]
 * 채팅 관련 모든 비즈니스 로직을 구현합니다.
 *
 * [어노테이션 설명]
 * @Service: Spring 빈으로 등록 (서비스 계층)
 * @RequiredArgsConstructor: final 필드에 대한 생성자 자동 생성 (의존성 주입)
 * @Transactional: 모든 public 메서드에 트랜잭션 적용
 * @Slf4j: 로깅을 위한 log 객체 자동 생성
 *
 * [트랜잭션 전략]
 * - 기본: @Transactional (쓰기 작업)
 * - 조회만: @Transactional(readOnly = true) (성능 최적화)
 *
 * [사용하는 Repository]
 * - ChatMessageRepository: 메시지 CRUD
 * - ChatReadRepository: 읽음 상태 관리
 * - GroupRepository: 모임 조회
 * - GroupMemberRepository: 멤버십 검증
 * - MemberRepository: 회원 조회
 *
 * @author damoyeo
 * @since 2025-02-25
 */
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ChatServiceImpl implements ChatService {

    private final ChatMessageRepository chatMessageRepository;
    private final ChatReadRepository chatReadRepository;
    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final MemberRepository memberRepository;
    private final MeetingRepository meetingRepository;
    private final MeetingAttendeeRepository meetingAttendeeRepository;

    // ========================================================================
    // 메시지 관리
    // ========================================================================

    /**
     * 채팅 메시지 전송
     *
     * [처리 흐름]
     * 1. 발신자 검증 (존재하는 회원인지)
     * 2. 모임 검증 (존재하고 활성 상태인지)
     * 3. 멤버 권한 검증 (모임 멤버인지, 강퇴 상태가 아닌지)
     * 4. 메시지 유효성 검증 (길이 등)
     * 5. 메시지 저장 (ChatMessage 엔티티)
     * 6. DTO 변환 후 반환
     */
    @Override
    public ChatMessageDTO sendMessage(Long groupId, String email, String message) {
        log.info("[채팅] 메시지 전송 시도 - groupId: {}, email: {}", groupId, email);

        // 1. 발신자 조회
        Member sender = getMemberByEmail(email);

        // 2. 모임 조회
        Group group = getGroupById(groupId);

        // 3. 멤버 권한 검증
        validateGroupMember(group, sender);

        // 4. 메시지 유효성 검증
        validateMessage(message);

        // 5. 메시지 저장
        ChatMessage chatMessage = ChatMessage.builder()
                .group(group)
                .sender(sender)
                .message(message.trim())
                .messageType(MessageType.TEXT)
                .build();

        ChatMessage saved = chatMessageRepository.save(chatMessage);

        log.info("[채팅] 메시지 저장 완료 - id: {}, groupId: {}", saved.getId(), groupId);

        // 6. DTO 변환
        return entityToDTO(saved);
    }

    /**
     * 메시지 히스토리 조회 (페이지네이션)
     *
     * [권한 검증]
     * 요청한 사용자가 해당 모임의 멤버인지 확인합니다.
     *
     * [정렬]
     * createdAt DESC: 최신 메시지부터 반환
     */
    @Override
    @Transactional(readOnly = true)
    public PageResponseDTO<ChatMessageDTO> getMessages(Long groupId, String email, PageRequestDTO pageRequest) {
        log.info("[채팅] 메시지 히스토리 조회 - groupId: {}, page: {}", groupId, pageRequest.getPage());

        // 1. 회원 조회
        Member member = getMemberByEmail(email);

        // 2. 모임 조회
        Group group = getGroupById(groupId);

        // 3. 멤버 권한 검증
        validateGroupMember(group, member);

        // 4. 페이지네이션 설정
        Pageable pageable = PageRequest.of(
                pageRequest.getPage() - 1,  // Spring Data JPA는 0부터 시작
                pageRequest.getSize()
        );

        // 5. 메시지 조회
        Page<ChatMessage> messagePage = chatMessageRepository
                .findByGroupIdOrderByCreatedAtDesc(groupId, pageable);

        // 6. DTO 변환
        List<ChatMessageDTO> dtoList = messagePage.getContent().stream()
                .map(this::entityToDTO)
                .collect(Collectors.toList());

        log.info("[채팅] 메시지 조회 완료 - total: {}, current: {}",
                messagePage.getTotalElements(), dtoList.size());

        // 7. PageResponseDTO 생성
        return PageResponseDTO.<ChatMessageDTO>builder()
                .dtoList(dtoList)
                .totalCount((int) messagePage.getTotalElements())
                .pageRequestDTO(pageRequest)
                .build();
    }

    // ========================================================================
    // 읽음 상태 관리
    // ========================================================================

    /**
     * 읽지 않은 메시지 개수 조회
     *
     * [계산 방식]
     * ChatRead.lastReadMessageId보다 큰 ID를 가진 메시지의 개수
     */
    @Override
    @Transactional(readOnly = true)
    public int getUnreadCount(Long groupId, String email) {
        // 1. 회원 조회
        Member member = getMemberByEmail(email);

        // 2. 읽음 상태 조회
        ChatRead chatRead = chatReadRepository
                .findByGroupIdAndMemberId(groupId, member.getId())
                .orElse(null);

        // 읽음 상태가 없으면 모든 메시지가 읽지 않은 상태
        if (chatRead == null) {
            return (int) chatMessageRepository.countByGroupId(groupId);
        }

        // 3. lastReadMessageId보다 큰 메시지 개수 조회
        return chatMessageRepository.countByGroupIdAndIdGreaterThan(
                groupId,
                chatRead.getLastReadMessageId()
        );
    }

    /**
     * 메시지 읽음 처리
     *
     * [처리 흐름]
     * 1. ChatRead 엔티티 조회 (없으면 새로 생성)
     * 2. lastReadMessageId 업데이트
     * 3. lastReadAt 현재 시각으로 업데이트
     */
    @Override
    public void markAsRead(Long groupId, String email, Long messageId) {
        log.info("[채팅] 읽음 처리 - groupId: {}, email: {}, messageId: {}",
                groupId, email, messageId);

        // 1. 회원 조회
        Member member = getMemberByEmail(email);

        // 2. 모임 조회
        Group group = getGroupById(groupId);

        // 3. 읽음 상태 조회 또는 생성
        ChatRead chatRead = chatReadRepository
                .findByGroupIdAndMemberId(groupId, member.getId())
                .orElseGet(() -> {
                    ChatRead newChatRead = ChatRead.builder()
                            .group(group)
                            .member(member)
                            .lastReadMessageId(0L)
                            .lastReadAt(LocalDateTime.now())
                            .build();
                    return chatReadRepository.save(newChatRead);
                });

        // 4. 읽음 상태 업데이트
        chatRead.updateLastRead(messageId);

        log.info("[채팅] 읽음 처리 완료 - lastReadMessageId: {}", messageId);
    }

    // ========================================================================
    // 채팅방 목록
    // ========================================================================

    /**
     * 내가 속한 채팅방 목록 조회
     *
     * [반환 정보]
     * - 모임 기본 정보 (ID, 이름, 이미지)
     * - 최신 메시지 (미리보기)
     * - 읽지 않은 메시지 개수
     */
    @Override
    @Transactional(readOnly = true)
    public List<ChatRoomDTO> getMyChatRooms(String email) {
        log.info("[채팅] 채팅방 목록 조회 - email: {}", email);

        // 1. 회원 조회
        Member member = getMemberByEmail(email);

        // 2. 내가 속한 모임 조회 (APPROVED 상태만)
        List<GroupMember> myMemberships = groupMemberRepository
                .findMyGroups(member.getId());

        // 3. 각 모임별 채팅방 정보 생성
        List<ChatRoomDTO> chatRooms = myMemberships.stream()
                .map(membership -> {
                    Group group = membership.getGroup();
                    Long groupId = group.getId();

                    // 최신 메시지 조회
                    ChatMessage latestMessage = chatMessageRepository
                            .findLatestMessageByGroupId(groupId);

                    // 읽지 않은 메시지 개수
                    int unreadCount = getUnreadCount(groupId, email);

                    return ChatRoomDTO.builder()
                            .groupId(groupId)
                            .groupName(group.getName())
                            .groupImage(group.getCoverImage())
                            .latestMessage(latestMessage != null ? entityToDTO(latestMessage) : null)
                            .unreadCount(unreadCount)
                            .build();
                })
                .collect(Collectors.toList());

        log.info("[채팅] 채팅방 목록 조회 완료 - count: {}", chatRooms.size());

        return chatRooms;
    }

    // ========================================================================
    // 정모 채팅 (참석자 전용)
    // ========================================================================

    /**
     * 정모 채팅 메시지 전송
     *
     * [처리 흐름]
     * 1. 발신자 검증 (존재하는 회원인지)
     * 2. 정모 검증 (존재하는지)
     * 3. 참석자 권한 검증 (ATTENDING 상태인지)
     * 4. 메시지 저장 (ChatMessage 엔티티, meeting_id 설정)
     * 5. DTO 변환 후 반환
     */
    @Override
    public ChatMessageDTO sendMeetingMessage(Long meetingId, String email, String message) {
        log.info("[정모 채팅] 메시지 전송 시도 - meetingId: {}, email: {}", meetingId, email);

        // 1. 발신자 조회
        Member sender = getMemberByEmail(email);

        // 2. 정모 조회
        Meeting meeting = getMeetingById(meetingId);

        // 3. 참석자 권한 검증
        validateMeetingAttendee(meetingId, sender.getId());

        // 4. 메시지 유효성 검증
        validateMessage(message);

        // 5. 메시지 저장
        ChatMessage chatMessage = ChatMessage.builder()
                .meeting(meeting)
                .sender(sender)
                .message(message.trim())
                .messageType(MessageType.TEXT)
                .build();

        ChatMessage saved = chatMessageRepository.save(chatMessage);

        log.info("[정모 채팅] 메시지 저장 완료 - id: {}, meetingId: {}", saved.getId(), meetingId);

        // 6. DTO 변환
        return meetingEntityToDTO(saved);
    }

    /**
     * 정모 메시지 히스토리 조회 (페이지네이션)
     */
    @Override
    @Transactional(readOnly = true)
    public PageResponseDTO<ChatMessageDTO> getMeetingMessages(Long meetingId, String email, PageRequestDTO pageRequest) {
        log.info("[정모 채팅] 메시지 히스토리 조회 - meetingId: {}, page: {}", meetingId, pageRequest.getPage());

        // 1. 회원 조회
        Member member = getMemberByEmail(email);

        // 2. 참석자 권한 검증
        validateMeetingAttendee(meetingId, member.getId());

        // 3. 페이지네이션 설정
        Pageable pageable = PageRequest.of(
                pageRequest.getPage() - 1,
                pageRequest.getSize()
        );

        // 4. 메시지 조회
        Page<ChatMessage> messagePage = chatMessageRepository
                .findByMeetingIdOrderByCreatedAtDesc(meetingId, pageable);

        // 5. DTO 변환
        List<ChatMessageDTO> dtoList = messagePage.getContent().stream()
                .map(this::meetingEntityToDTO)
                .collect(Collectors.toList());

        log.info("[정모 채팅] 메시지 조회 완료 - total: {}, current: {}",
                messagePage.getTotalElements(), dtoList.size());

        // 6. PageResponseDTO 생성
        return PageResponseDTO.<ChatMessageDTO>builder()
                .dtoList(dtoList)
                .totalCount((int) messagePage.getTotalElements())
                .pageRequestDTO(pageRequest)
                .build();
    }

    /**
     * 정모 읽지 않은 메시지 개수 조회
     */
    @Override
    @Transactional(readOnly = true)
    public int getMeetingUnreadCount(Long meetingId, String email) {
        // 1. 회원 조회
        Member member = getMemberByEmail(email);

        // 2. 읽음 상태 조회
        ChatRead chatRead = chatReadRepository
                .findByMeetingIdAndMemberId(meetingId, member.getId())
                .orElse(null);

        // 읽음 상태가 없으면 모든 메시지가 읽지 않은 상태
        if (chatRead == null) {
            return (int) chatMessageRepository.countByMeetingId(meetingId);
        }

        // 3. lastReadMessageId보다 큰 메시지 개수 조회
        return chatMessageRepository.countByMeetingIdAndIdGreaterThan(
                meetingId,
                chatRead.getLastReadMessageId()
        );
    }

    /**
     * 정모 메시지 읽음 처리
     */
    @Override
    public void markMeetingAsRead(Long meetingId, String email, Long messageId) {
        log.info("[정모 채팅] 읽음 처리 - meetingId: {}, email: {}, messageId: {}",
                meetingId, email, messageId);

        // 1. 회원 조회
        Member member = getMemberByEmail(email);

        // 2. 정모 조회
        Meeting meeting = getMeetingById(meetingId);

        // 3. 읽음 상태 조회 또는 생성
        ChatRead chatRead = chatReadRepository
                .findByMeetingIdAndMemberId(meetingId, member.getId())
                .orElseGet(() -> {
                    ChatRead newChatRead = ChatRead.builder()
                            .meeting(meeting)
                            .member(member)
                            .lastReadMessageId(0L)
                            .lastReadAt(LocalDateTime.now())
                            .build();
                    return chatReadRepository.save(newChatRead);
                });

        // 4. 읽음 상태 업데이트
        chatRead.updateLastRead(messageId);

        log.info("[정모 채팅] 읽음 처리 완료 - lastReadMessageId: {}", messageId);
    }

    /**
     * 정모 채팅 접근 권한 검증
     */
    @Override
    @Transactional(readOnly = true)
    public void validateMeetingChatAccess(Long meetingId, String email) {
        Member member = getMemberByEmail(email);
        validateMeetingAttendee(meetingId, member.getId());
    }

    // ========================================================================
    // 유틸리티 메서드
    // ========================================================================

    /**
     * 이메일로 회원 조회
     */
    private Member getMemberByEmail(String email) {
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> CustomException.notFound("회원을 찾을 수 없습니다."));
    }

    /**
     * ID로 모임 조회
     */
    private Group getGroupById(Long groupId) {
        return groupRepository.findById(groupId)
                .orElseThrow(() -> CustomException.notFound("모임을 찾을 수 없습니다."));
    }

    /**
     * 모임 멤버 권한 검증
     *
     * [검증 내용]
     * 1. 모임 멤버인지 확인
     * 2. 강퇴 상태(BANNED)가 아닌지 확인
     */
    private void validateGroupMember(Group group, Member member) {
        GroupMember membership = groupMemberRepository
                .findByGroupIdAndMemberId(group.getId(), member.getId())
                .orElseThrow(() -> CustomException.forbidden("모임 멤버만 채팅을 사용할 수 있습니다."));

        // 강퇴 상태 확인
        if (membership.getStatus() == JoinStatus.BANNED) {
            throw CustomException.forbidden("강퇴된 멤버는 채팅을 사용할 수 없습니다.");
        }

        // APPROVED 상태가 아니면 접근 불가
        if (membership.getStatus() != JoinStatus.APPROVED) {
            throw CustomException.forbidden("승인된 멤버만 채팅을 사용할 수 있습니다.");
        }
    }

    /**
     * 메시지 유효성 검증
     */
    private void validateMessage(String message) {
        if (message == null || message.trim().isEmpty()) {
            throw CustomException.badRequest("메시지를 입력해주세요.");
        }

        if (message.length() > 2000) {
            throw CustomException.badRequest("메시지는 2000자 이내로 작성해주세요.");
        }
    }

    /**
     * Entity → DTO 변환 (모임 채팅)
     *
     * [Builder 패턴 사용]
     * ModelMapper 대신 Builder 패턴을 사용하여 명시적으로 변환합니다.
     * 순환 참조 방지 및 타입 안전성 확보
     */
    private ChatMessageDTO entityToDTO(ChatMessage entity) {
        return ChatMessageDTO.builder()
                .id(entity.getId())
                .groupId(entity.getGroup().getId())
                .sender(entity.getSender() != null
                        ? MemberSummaryDTO.builder()
                            .id(entity.getSender().getId())
                            .nickname(entity.getSender().getNickname())
                            .profileImage(entity.getSender().getProfileImage())
                            .build()
                        : null)  // SYSTEM 메시지는 sender가 null
                .message(entity.getMessage())
                .messageType(entity.getMessageType())
                .imageUrl(entity.getImageUrl())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    /**
     * ID로 정모 조회
     */
    private Meeting getMeetingById(Long meetingId) {
        return meetingRepository.findById(meetingId)
                .orElseThrow(() -> CustomException.notFound("정모를 찾을 수 없습니다."));
    }

    /**
     * 정모 참석자 권한 검증
     *
     * [검증 내용]
     * 정모에 ATTENDING 상태로 참석 등록되어 있는지 확인합니다.
     */
    private void validateMeetingAttendee(Long meetingId, Long memberId) {
        boolean isAttending = meetingAttendeeRepository.isAttending(meetingId, memberId);

        if (!isAttending) {
            throw CustomException.forbidden("정모 참석자만 채팅에 참여할 수 있습니다.");
        }
    }

    /**
     * Entity → DTO 변환 (정모 채팅)
     *
     * [차이점]
     * 모임 채팅과 달리 groupId 대신 meetingId를 설정합니다.
     */
    private ChatMessageDTO meetingEntityToDTO(ChatMessage entity) {
        return ChatMessageDTO.builder()
                .id(entity.getId())
                .meetingId(entity.getMeeting() != null ? entity.getMeeting().getId() : null)
                .sender(entity.getSender() != null
                        ? MemberSummaryDTO.builder()
                            .id(entity.getSender().getId())
                            .nickname(entity.getSender().getNickname())
                            .profileImage(entity.getSender().getProfileImage())
                            .build()
                        : null)
                .message(entity.getMessage())
                .messageType(entity.getMessageType())
                .imageUrl(entity.getImageUrl())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
