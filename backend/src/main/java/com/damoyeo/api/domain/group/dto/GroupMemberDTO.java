package com.damoyeo.api.domain.group.dto;

import com.damoyeo.api.domain.member.dto.MemberSummaryDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * ============================================================================
 * 모임 멤버 응답 DTO
 * ============================================================================
 *
 * [역할]
 * 모임 멤버 정보를 프론트엔드에 전달하기 위한 데이터 전송 객체입니다.
 *
 * [GroupMember → GroupMemberDTO 변환 이유]
 * GroupMember 엔티티는 group, member 객체를 포함하지만,
 * 프론트엔드에서 멤버 목록을 표시할 때는 member 정보만 필요합니다.
 * 회원 정보는 중첩 객체로 반환합니다.
 *
 * [사용 위치]
 * - GroupController.getMembers() - 멤버 목록 조회 응답
 *
 * [프론트엔드 응답 예시]
 * GET /api/groups/1/members
 * [
 *   {
 *     "id": 1,
 *     "member": { "id": 5, "nickname": "홍길동", "profileImage": "..." },
 *     "role": "OWNER",
 *     "joinedAt": "2024-01-15T10:30:00"
 *   }
 * ]
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GroupMemberDTO {

    /**
     * GroupMember ID (PK)
     *
     * 멤버십의 고유 식별자입니다.
     * 강퇴, 역할 변경 등의 작업 시 이 ID를 사용합니다.
     */
    private Long id;

    /**
     * 회원 정보 (중첩 객체)
     */
    private MemberSummaryDTO member;

    /**
     * 모임 내 역할
     *
     * - "OWNER": 모임장 (👑 아이콘 표시)
     * - "MANAGER": 운영진 (⭐ 아이콘 표시)
     * - "MEMBER": 일반 멤버
     *
     * 프론트엔드에서 역할에 따라 다른 UI를 표시합니다.
     */
    private String role;

    /**
     * 가입 일시
     *
     * GroupMember의 createdAt 값입니다.
     * 멤버 목록을 가입일순으로 정렬할 때 사용합니다.
     */
    private LocalDateTime joinedAt;
}
