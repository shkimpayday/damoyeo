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
 * 단순한 관계뿐 아니라 추가 정보(역할, 가입일)도 포함합니다.
 *
 * [관계 다이어그램]
 * Group (1) ──── (N) GroupMember (N) ──── (1) Member
 *
 * [왜 중간 테이블을 별도 엔티티로 만들었는가?]
 * JPA의 @ManyToMany를 사용하면 단순한 관계만 표현할 수 있습니다.
 * 하지만 우리는 추가 정보가 필요합니다:
 * - role: 이 회원이 모임에서 어떤 역할인가? (모임장/운영진/멤버)
 * - status: 가입 상태 (APPROVED, BANNED)
 * - createdAt: 언제 가입했는가?
 *
 * 따라서 중간 테이블을 별도 엔티티로 승격시켜 관리합니다.
 *
 * [사용 위치]
 * - GroupService.join(): 가입 시 즉시 APPROVED 상태로 생성
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
     * - APPROVED: 가입 완료 (정식 멤버)
     * - BANNED: 강퇴됨
     *
     * 기본값: APPROVED (가입 시 즉시 승인)
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private JoinStatus status = JoinStatus.APPROVED;

    // ========================================================================
    // 상태 변경 메서드
    // ========================================================================

    /**
     * 역할 변경
     *
     * 멤버의 역할을 변경합니다.
     * 예: 일반 멤버 → 운영진 승격
     *
     * @param role 새로운 역할 (OWNER, MANAGER, MEMBER)
     *
     * 호출 위치: GroupServiceImpl.changeRole()
     */
    public void changeRole(GroupRole role) {
        this.role = role;
    }
}
