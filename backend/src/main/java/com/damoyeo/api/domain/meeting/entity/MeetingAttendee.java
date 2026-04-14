package com.damoyeo.api.domain.meeting.entity;

import com.damoyeo.api.domain.member.entity.Member;
import com.damoyeo.api.global.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * 정모 참석자 엔티티 (중간 테이블)
 *
 * 정모(Meeting)와 회원(Member) 간의 다대다(N:M) 관계를 관리합니다.
 * 단순한 관계뿐 아니라 참석 상태 정보도 포함합니다.
 *
 * [관계 다이어그램]
 * Meeting (1) ──── (N) MeetingAttendee (N) ──── (1) Member
 *
 * [GroupMember와의 차이점]
 * - GroupMember: 모임 가입 (지속적인 멤버십)
 * - MeetingAttendee: 특정 정모 참석 (일회성)
 *
 * - MeetingService.attend(): 참석 등록
 * - MeetingController.getAttendees(): 참석자 목록 조회
 *
 * [DB 테이블]
 * meeting_attendee 테이블
 * - unique constraint: (meeting_id, member_id) - 같은 정모에 중복 참석 등록 방지
 */
@Entity
@Table(name = "meeting_attendee",
        uniqueConstraints = @UniqueConstraint(columnNames = {"meeting_id", "member_id"}))
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString(exclude = {"meeting", "member"})
public class MeetingAttendee extends BaseEntity {

    /**
     * 고유 ID (PK)
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 참석할 정모
     *
     * 이 참석 정보가 어느 정모에 대한 것인지를 나타냅니다.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meeting_id", nullable = false)
    private Meeting meeting;

    /**
     * 참석자 정보
     *
     * 정모에 참석하는 회원입니다.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    /**
     * 참석 상태
     *
     * - ATTENDING: 참석 예정
     * - MAYBE: 미정 (아직 확정 안 됨)
     * - NOT_ATTENDING: 불참
     *
     * 기본값: ATTENDING (참석 신청 시 참석으로 시작)
     *
     * [사용 예]
     * 처음에는 ATTENDING으로 등록했다가,
     * 나중에 일정이 안 맞으면 NOT_ATTENDING으로 변경할 수 있습니다.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private AttendStatus status = AttendStatus.ATTENDING;

    /**
     * 참석 상태 변경
     *
     * 사용자가 참석/미정/불참 상태를 변경할 때 사용합니다.
     *
     * @param status 새로운 참석 상태
     *
     * 호출 위치: MeetingServiceImpl.updateAttendStatus()
     */
    public void changeStatus(AttendStatus status) {
        this.status = status;
    }
}
