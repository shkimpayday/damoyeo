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
 * 채팅 서비스 구현체.
 * 모임/정모 채팅의 메시지 저장·조회, 읽음 상태 관리를 담당한다.
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

    /** {@inheritDoc} */
    @Override
    public ChatMessageDTO sendMessage(Long groupId, String email, String message) {
        log.info("[채팅] 메시지 전송 시도 - groupId: {}, email: {}", groupId, email);

        Member sender = getMemberByEmail(email);

        Group group = getGroupById(groupId);

        validateGroupMember(group, sender);

        validateMessage(message);

        ChatMessage chatMessage = ChatMessage.builder()
                .group(group)
                .sender(sender)
                .message(message.trim())
                .messageType(MessageType.TEXT)
                .build();

        ChatMessage saved = chatMessageRepository.save(chatMessage);

        log.info("[채팅] 메시지 저장 완료 - id: {}, groupId: {}", saved.getId(), groupId);

        return entityToDTO(saved);
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public PageResponseDTO<ChatMessageDTO> getMessages(Long groupId, String email, PageRequestDTO pageRequest) {
        log.info("[채팅] 메시지 히스토리 조회 - groupId: {}, page: {}", groupId, pageRequest.getPage());

        Member member = getMemberByEmail(email);

        Group group = getGroupById(groupId);

        validateGroupMember(group, member);

        Pageable pageable = PageRequest.of(
                pageRequest.getPage() - 1,  // Spring Data JPA는 0부터 시작
                pageRequest.getSize()
        );

        Page<ChatMessage> messagePage = chatMessageRepository
                .findByGroupIdOrderByCreatedAtDesc(groupId, pageable);

        List<ChatMessageDTO> dtoList = messagePage.getContent().stream()
                .map(this::entityToDTO)
                .collect(Collectors.toList());

        log.info("[채팅] 메시지 조회 완료 - total: {}, current: {}",
                messagePage.getTotalElements(), dtoList.size());

        return PageResponseDTO.<ChatMessageDTO>builder()
                .dtoList(dtoList)
                .totalCount((int) messagePage.getTotalElements())
                .pageRequestDTO(pageRequest)
                .build();
    }

    // 읽음 상태 관리

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public int getUnreadCount(Long groupId, String email) {
        Member member = getMemberByEmail(email);

        ChatRead chatRead = chatReadRepository
                .findByGroupIdAndMemberId(groupId, member.getId())
                .orElse(null);

        // 읽음 상태가 없으면 모든 메시지가 읽지 않은 상태
        if (chatRead == null) {
            return (int) chatMessageRepository.countByGroupId(groupId);
        }

        return chatMessageRepository.countByGroupIdAndIdGreaterThan(
                groupId,
                chatRead.getLastReadMessageId()
        );
    }

    /** {@inheritDoc} */
    @Override
    public void markAsRead(Long groupId, String email, Long messageId) {
        log.info("[채팅] 읽음 처리 - groupId: {}, email: {}, messageId: {}",
                groupId, email, messageId);

        Member member = getMemberByEmail(email);

        Group group = getGroupById(groupId);

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

        chatRead.updateLastRead(messageId);

        log.info("[채팅] 읽음 처리 완료 - lastReadMessageId: {}", messageId);
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public List<ChatRoomDTO> getMyChatRooms(String email) {
        log.info("[채팅] 채팅방 목록 조회 - email: {}", email);

        Member member = getMemberByEmail(email);

        List<GroupMember> myMemberships = groupMemberRepository
                .findMyGroups(member.getId());

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

    /** {@inheritDoc} */
    @Override
    public ChatMessageDTO sendMeetingMessage(Long meetingId, String email, String message) {
        log.info("[정모 채팅] 메시지 전송 시도 - meetingId: {}, email: {}", meetingId, email);

        Member sender = getMemberByEmail(email);

        Meeting meeting = getMeetingById(meetingId);

        validateMeetingAttendee(meetingId, sender.getId());

        validateMessage(message);

        ChatMessage chatMessage = ChatMessage.builder()
                .meeting(meeting)
                .sender(sender)
                .message(message.trim())
                .messageType(MessageType.TEXT)
                .build();

        ChatMessage saved = chatMessageRepository.save(chatMessage);

        log.info("[정모 채팅] 메시지 저장 완료 - id: {}, meetingId: {}", saved.getId(), meetingId);

        return meetingEntityToDTO(saved);
    }

    /**
     * 정모 메시지 히스토리 조회 (페이지네이션)
     */
    @Override
    @Transactional(readOnly = true)
    public PageResponseDTO<ChatMessageDTO> getMeetingMessages(Long meetingId, String email, PageRequestDTO pageRequest) {
        log.info("[정모 채팅] 메시지 히스토리 조회 - meetingId: {}, page: {}", meetingId, pageRequest.getPage());

        Member member = getMemberByEmail(email);

        validateMeetingAttendee(meetingId, member.getId());

        Pageable pageable = PageRequest.of(
                pageRequest.getPage() - 1,
                pageRequest.getSize()
        );

        Page<ChatMessage> messagePage = chatMessageRepository
                .findByMeetingIdOrderByCreatedAtDesc(meetingId, pageable);

        List<ChatMessageDTO> dtoList = messagePage.getContent().stream()
                .map(this::meetingEntityToDTO)
                .collect(Collectors.toList());

        log.info("[정모 채팅] 메시지 조회 완료 - total: {}, current: {}",
                messagePage.getTotalElements(), dtoList.size());

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
        Member member = getMemberByEmail(email);

        ChatRead chatRead = chatReadRepository
                .findByMeetingIdAndMemberId(meetingId, member.getId())
                .orElse(null);

        // 읽음 상태가 없으면 모든 메시지가 읽지 않은 상태
        if (chatRead == null) {
            return (int) chatMessageRepository.countByMeetingId(meetingId);
        }

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

        Member member = getMemberByEmail(email);

        Meeting meeting = getMeetingById(meetingId);

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

    /** 모임 멤버십 확인. 강퇴(BANNED) 또는 미승인 상태면 예외를 던진다. */
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

    /** ChatMessage → ChatMessageDTO (모임 채팅). SYSTEM 메시지는 sender가 null일 수 있다. */
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

    /** 정모 채팅 접근 권한 검증. ATTENDING 상태가 아니면 예외를 던진다. */
    private void validateMeetingAttendee(Long meetingId, Long memberId) {
        boolean isAttending = meetingAttendeeRepository.isAttending(meetingId, memberId);

        if (!isAttending) {
            throw CustomException.forbidden("정모 참석자만 채팅에 참여할 수 있습니다.");
        }
    }

    /** ChatMessage → ChatMessageDTO (정모 채팅). groupId 대신 meetingId를 설정한다. */
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
