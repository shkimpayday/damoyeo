package com.damoyeo.api.domain.meeting.dto;

import com.damoyeo.api.domain.member.dto.MemberSummaryDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * ============================================================================
 * 정모 참석자 응답 DTO
 * ============================================================================
 *
 * [역할]
 * 정모 참석자 정보를 프론트엔드에 전달하는 데이터 전송 객체입니다.
 * MeetingAttendee 엔티티의 정보와 회원 정보를 담습니다.
 *
 * [포함 정보]
 * - 참석 등록 ID
 * - 회원 정보 (member - 중첩 객체)
 * - 참석 상태 (ATTENDING, MAYBE, NOT_ATTENDING)
 * - 등록 일시
 *
 * [사용 위치]
 * - MeetingController.getAttendees()
 * - MeetingServiceImpl.attendeeToDTO()에서 생성
 *
 * [프론트엔드 UI]
 * 정모 상세 페이지의 "참석자" 탭에서 목록 표시
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MeetingAttendeeDTO {

    /**
     * 참석 정보 ID
     *
     * MeetingAttendee 엔티티의 PK
     */
    private Long id;

    /**
     * 회원 정보 (중첩 객체)
     */
    private MemberSummaryDTO member;

    /**
     * 참석 상태
     *
     * - ATTENDING: 참석 예정
     * - MAYBE: 미정
     * - NOT_ATTENDING: 불참
     *
     * AttendStatus enum의 name() 값
     *
     * [프론트엔드 활용]
     * 상태별로 그룹핑하여 표시 가능
     * "참석 예정 15명", "미정 3명", "불참 2명"
     */
    private String status;

    /**
     * 참석 등록 일시
     *
     * MeetingAttendee의 createdAt
     * 언제 참석 신청을 했는지 표시
     */
    private LocalDateTime registeredAt;
}
