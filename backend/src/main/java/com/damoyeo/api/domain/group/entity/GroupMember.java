package com.damoyeo.api.domain.group.entity;

import com.damoyeo.api.domain.member.entity.Member;
import com.damoyeo.api.global.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * ============================================================================
 * 모임-회원 관계 엔티티 (중간 테이블)
 * ============================================================================
 *
 * [역할]
 * 모임(Group)과 회원(Member) 간의 다대다(N:M) 관계를 관리합니다.
 * 단순한 관계뿐 아니라 추가 정보(역할, 가입 상태)도 포함합니다.
 *
 * [관계 다이어그램]
 * Group (1) ──── (N) GroupMember (N) ──── (1) Member
 *
 * [왜 중간 테이블을 별도 엔티티로 만들었는가?]
 * JPA의 @ManyToMany를 사용하면 단순한 관계만 표현할 수 있습니다.
 * 하지만 우리는 추가 정보가 필요합니다:
 * - role: 이 회원이 모임에서 어떤 역할인가? (모임장/운영진/멤버)
 * - status: 가입 신청 상태는? (대기중/승인됨/거절됨)
 * - createdAt: 언제 가입했는가?
 *
 * 따라서 중간 테이블을 별도 엔티티로 승격시켜 관리합니다.
 *
 * [사용 위치]
 * - GroupService.join(): 가입 신청 시 PENDING 상태로 생성
 * - GroupService.approve(): 가입 승인 시 APPROVED로 변경
 * - GroupController.getMembers(): 멤버 목록 조회
 *
 * [DB 테이블]
 * group_member 테이블
 * - unique constraint: (group_id, member_id) - 같은 모임에 중복 가입 방지
 */
@Entity
@Table(name = "group_member",
        uniqueConstraints = @UniqueConstraint(columnNames = {"group_id", "member_id"}))
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString(exclude = {"group", "member"})
public class GroupMember extends BaseEntity {

    /**
     * 고유 ID (PK)
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 소속 모임
     *
     * 이 멤버십이 어느 모임에 속하는지를 나타냅니다.
     * LAZY 로딩으로 필요할 때만 조회합니다.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private Group group;

    /**
     * 회원 정보
     *
     * 이 멤버십의 주인인 회원입니다.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    /**
     * 모임 내 역할
     *
     * - OWNER: 모임장 (모임 생성자, 모든 권한)
     * - MANAGER: 운영진 (멤버 관리 권한)
     * - MEMBER: 일반 멤버
     *
     * 기본값: MEMBER (가입 시 일반 멤버로 시작)
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private GroupRole role = GroupRole.MEMBER;

    /**
     * 가입 상태
     *
     * - PENDING: 가입 신청 중 (승인 대기)
     * - APPROVED: 승인됨 (정식 멤버)
     * - REJECTED: 거절됨
     *
     * 기본값: PENDING (가입 신청 시 대기 상태로 시작)
     *
     * [가입 흐름]
     * 1. 사용자가 가입 신청 → PENDING 상태로 GroupMember 생성
     * 2. 모임장/운영진이 승인 → APPROVED로 변경
     * 3. 또는 거절 → REJECTED로 변경
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private JoinStatus status = JoinStatus.PENDING;

    // ========================================================================
    // 상태 변경 메서드
    // ========================================================================

    /**
     * 가입 승인
     *
     * 모임장/운영진이 가입 신청을 승인할 때 호출합니다.
     * PENDING → APPROVED
     *
     * 호출 위치: GroupServiceImpl.approveMember()
     */
    public void approve() {
        this.status = JoinStatus.APPROVED;
    }

    /**
     * 가입 거절
     *
     * 모임장/운영진이 가입 신청을 거절할 때 호출합니다.
     * PENDING → REJECTED
     *
     * 호출 위치: GroupServiceImpl.rejectMember()
     */
    public void reject() {
        this.status = JoinStatus.REJECTED;
    }

    /**
     * 역할 변경
     *
     * 멤버의 역할을 변경합니다.
     * 예: 일반 멤버 → 운영진 승격
     *
     * @param role 새로운 역할 (OWNER, MANAGER, MEMBER)
     *
     * 호출 위치: GroupServiceImpl.changeRole() (Phase 2에서 구현 예정)
     */
    public void changeRole(GroupRole role) {
        this.role = role;
    }
}
