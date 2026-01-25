package com.damoyeo.api.domain.group.repository;

import com.damoyeo.api.domain.group.entity.GroupMember;
import com.damoyeo.api.domain.group.entity.JoinStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * ============================================================================
 * 모임-회원 관계(GroupMember) Repository
 * ============================================================================
 *
 * [역할]
 * 모임과 회원 간의 관계(멤버십)를 관리하는 데이터 접근 계층입니다.
 *
 * [주요 기능]
 * - 특정 모임의 멤버 목록 조회
 * - 특정 회원이 가입한 모임 목록 조회
 * - 가입 신청 상태 확인
 * - 멤버 수 집계
 *
 * [사용 위치]
 * - GroupServiceImpl: 가입/탈퇴/승인/거절 처리
 * - MeetingServiceImpl: 정모 참석 자격 확인
 */
public interface GroupMemberRepository extends JpaRepository<GroupMember, Long> {

    /**
     * 모임의 멤버 목록 조회 (상태별)
     *
     * [용도]
     * 특정 상태의 멤버를 조회합니다.
     *
     * [사용 예시]
     * - 승인된 멤버 목록: status = APPROVED
     * - 가입 대기 목록: status = PENDING
     *
     * [특징]
     * fetch join으로 회원 정보를 함께 조회하여 N+1 문제를 방지합니다.
     *
     * @param groupId 모임 ID
     * @param status 조회할 상태 (PENDING, APPROVED, REJECTED)
     * @return 해당 상태의 멤버 목록
     *
     * 호출 위치:
     * - GroupServiceImpl.getMembers() - 승인된 멤버
     * - GroupServiceImpl.getPendingMembers() - 가입 대기 목록
     */
    @Query("select gm from GroupMember gm " +
            "left join fetch gm.member " +
            "where gm.group.id = :groupId and gm.status = :status")
    List<GroupMember> findByGroupIdAndStatus(@Param("groupId") Long groupId,
                                             @Param("status") JoinStatus status);

    /**
     * 특정 모임에서 특정 회원의 멤버십 조회
     *
     * [용도]
     * 회원이 특정 모임에 가입되어 있는지, 어떤 상태인지 확인합니다.
     *
     * [사용 예시]
     * - 가입 신청 전: 이미 신청했는지 확인
     * - 가입 승인/거절: 해당 멤버십 찾아서 상태 변경
     * - 권한 검사: 멤버인지, 역할이 무엇인지 확인
     *
     * @param groupId 모임 ID
     * @param memberId 회원 ID
     * @return 멤버십 정보 (없으면 Optional.empty())
     *
     * 호출 위치:
     * - GroupServiceImpl.join() - 중복 가입 방지
     * - GroupServiceImpl.approve/reject() - 멤버십 상태 변경
     */
    @Query("select gm from GroupMember gm " +
            "where gm.group.id = :groupId and gm.member.id = :memberId")
    Optional<GroupMember> findByGroupIdAndMemberId(@Param("groupId") Long groupId,
                                                   @Param("memberId") Long memberId);

    /**
     * 내가 가입한 모임 목록 조회
     *
     * [용도]
     * 특정 회원이 정식 멤버로 가입한 모임 목록을 조회합니다.
     * 마이페이지의 "내 모임" 탭에서 사용됩니다.
     *
     * [조건]
     * - status = APPROVED (승인된 멤버만)
     * - PENDING(대기중), REJECTED(거절됨)는 제외
     *
     * [특징]
     * 모임 정보와 카테고리를 함께 조회하여
     * 프론트엔드에서 모임 목록을 표시할 때 필요한 정보를 한 번에 가져옵니다.
     *
     * @param memberId 회원 ID
     * @return 가입한 모임 목록 (GroupMember에서 group 추출하여 사용)
     *
     * 호출 위치: GroupServiceImpl.getMyGroups()
     */
    @Query("select gm from GroupMember gm " +
            "left join fetch gm.group g " +
            "left join fetch g.category " +
            "where gm.member.id = :memberId and gm.status = 'APPROVED'")
    List<GroupMember> findMyGroups(@Param("memberId") Long memberId);

    /**
     * 승인된 멤버 수 집계
     *
     * [용도]
     * 모임의 현재 멤버 수를 계산합니다.
     *
     * [사용 예시]
     * - 가입 신청 시: 정원(maxMembers) 초과 여부 확인
     * - 모임 목록 표시: "멤버 15명" 형태로 표시
     *
     * [주의]
     * PENDING(대기중), REJECTED(거절됨)는 카운트에서 제외합니다.
     *
     * @param groupId 모임 ID
     * @return 승인된 멤버 수
     *
     * 호출 위치:
     * - GroupServiceImpl.join() - 정원 확인
     * - GroupDTO 변환 시 - memberCount 필드 설정
     */
    @Query("select count(gm) from GroupMember gm " +
            "where gm.group.id = :groupId and gm.status = 'APPROVED'")
    int countApprovedMembers(@Param("groupId") Long groupId);

    /**
     * 멤버십 존재 여부 확인
     *
     * [용도]
     * 회원이 특정 모임에 어떤 형태로든 관계가 있는지 빠르게 확인합니다.
     *
     * [특징]
     * - 상태(PENDING/APPROVED/REJECTED)와 관계없이 존재 여부만 확인
     * - COUNT 대신 EXISTS를 사용하여 성능 최적화
     * - Spring Data JPA가 자동으로 쿼리 생성
     *
     * [사용 예시]
     * - 가입 신청 버튼 활성화/비활성화 결정
     * - 이미 가입 신청한 사용자에게 "대기중" 표시
     *
     * @param groupId 모임 ID
     * @param memberId 회원 ID
     * @return 존재 여부 (true: 관계 있음, false: 관계 없음)
     *
     * 호출 위치: GroupServiceImpl에서 중복 체크 시
     */
    boolean existsByGroupIdAndMemberId(Long groupId, Long memberId);
}
