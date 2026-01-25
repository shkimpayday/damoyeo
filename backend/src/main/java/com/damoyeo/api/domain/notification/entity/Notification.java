package com.damoyeo.api.domain.notification.entity;

import com.damoyeo.api.domain.member.entity.Member;
import com.damoyeo.api.global.common.entity.BaseEntity;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;

/**
 * ============================================================================
 * 알림(Notification) 엔티티
 * ============================================================================
 *
 * [역할]
 * 사용자에게 전송되는 각종 알림을 저장합니다.
 * 모임 가입 승인, 정모 알림, 역할 변경 등 다양한 이벤트를 사용자에게 알려줍니다.
 *
 * [알림 종류] (NotificationType)
 * - JOIN_REQUEST: 가입 신청이 왔을 때 (모임장에게)
 * - JOIN_APPROVED: 가입이 승인되었을 때 (신청자에게)
 * - JOIN_REJECTED: 가입이 거절되었을 때 (신청자에게)
 * - NEW_MEETING: 새 정모가 등록되었을 때 (모임 멤버에게)
 * - MEETING_REMINDER: 정모 시작 전 알림 (참석자에게)
 * - MEETING_CANCELLED: 정모가 취소되었을 때 (참석자에게)
 * - NEW_MEMBER: 새 멤버가 가입했을 때 (모임장에게)
 * - GROUP_UPDATE: 모임 정보가 변경되었을 때 (멤버에게)
 * - ROLE_CHANGED: 역할이 변경되었을 때 (해당 멤버에게)
 *
 * [사용 위치]
 * - NotificationService.send(): 알림 생성
 * - NotificationController: 알림 목록 조회, 읽음 처리
 *
 * [프론트엔드 UI]
 * ┌─────────────────────────────────────┐
 * │  🔔 알림                     [모두 읽음]│
 * ├─────────────────────────────────────┤
 * │  ● 강남 러닝 크루 가입이 승인되었습니다  │
 * │    2시간 전                          │
 * │  ○ 5월 첫째 주 러닝 정모가 등록되었습니다│
 * │    어제                              │
 * └─────────────────────────────────────┘
 * (●: 안 읽음, ○: 읽음)
 *
 * [DB 테이블]
 * notification 테이블
 */
@Entity
@Table(name = "notification")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString(exclude = "member")
public class Notification extends BaseEntity {

    /**
     * 알림 고유 ID (PK)
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 알림을 받는 회원
     *
     * 이 알림이 누구에게 전송되는지를 나타냅니다.
     *
     * [fetch = LAZY]
     * 알림 조회 시 회원 정보가 항상 필요하지 않으므로
     * 지연 로딩으로 설정하여 성능을 최적화합니다.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    /**
     * 알림 유형
     *
     * NotificationType enum 값을 문자열로 저장합니다.
     *
     * [프론트엔드 활용]
     * 알림 유형에 따라 아이콘이나 스타일을 다르게 표시할 수 있습니다.
     * - JOIN_APPROVED: 승인 아이콘 (초록색)
     * - JOIN_REJECTED: 거절 아이콘 (빨간색)
     * - NEW_MEETING: 캘린더 아이콘
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;

    /**
     * 알림 제목
     *
     * 알림의 제목 또는 요약
     * 예: "가입이 승인되었습니다", "새 정모가 등록되었습니다"
     */
    @Column(nullable = false)
    private String title;

    /**
     * 알림 내용
     *
     * 알림의 상세 내용 (최대 500자)
     * 예: "강남 러닝 크루에 가입되었습니다. 다음 정모를 확인해보세요!"
     */
    @Column(length = 500)
    private String message;

    /**
     * 관련 엔티티 ID
     *
     * 알림과 연관된 리소스의 ID입니다.
     * 알림 유형에 따라 다른 엔티티를 가리킵니다.
     *
     * [유형별 relatedId]
     * - JOIN_*: groupId (모임 ID)
     * - NEW_MEETING, MEETING_*: meetingId (정모 ID)
     * - NEW_MEMBER: groupId (모임 ID)
     * - GROUP_UPDATE: groupId (모임 ID)
     * - ROLE_CHANGED: groupId (모임 ID)
     *
     * [프론트엔드 활용]
     * 알림 클릭 시 해당 리소스 페이지로 이동
     * 예: JOIN_APPROVED 알림 클릭 → /groups/{relatedId}로 이동
     */
    private Long relatedId;  // 관련 엔티티 ID (group, meeting 등)

    /**
     * 읽음 여부
     *
     * false: 읽지 않음 (기본값)
     * true: 읽음
     *
     * [프론트엔드 활용]
     * - 읽지 않은 알림은 굵게 또는 점으로 표시
     * - 헤더의 알림 벨에 읽지 않은 개수 배지 표시
     */
    @Column(nullable = false)
    @Builder.Default
    private boolean isRead = false;

    // ========================================================================
    // 변경 메서드
    // ========================================================================

    /**
     * 알림 읽음 처리
     *
     * 사용자가 알림을 클릭하거나 확인했을 때 호출합니다.
     *
     * 호출 위치: NotificationServiceImpl.markAsRead()
     */
    public void markAsRead() {
        this.isRead = true;
    }
}
