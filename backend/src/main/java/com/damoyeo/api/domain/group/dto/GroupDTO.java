package com.damoyeo.api.domain.group.dto;

import com.damoyeo.api.domain.category.dto.CategoryDTO;
import com.damoyeo.api.domain.member.dto.MemberSummaryDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * ============================================================================
 * 모임 응답 DTO
 * ============================================================================
 *
 * [역할]
 * 모임 정보를 프론트엔드에 전달하기 위한 데이터 전송 객체입니다.
 *
 * [Entity → DTO 변환 이유]
 * 1. Entity는 DB 구조를 그대로 반영하지만, 프론트엔드에는 다른 형태가 필요할 수 있음
 * 2. 순환 참조 방지 (Entity의 양방향 관계 → JSON 직렬화 시 무한 루프)
 * 3. 필요한 정보만 선택적으로 노출 (보안)
 * 4. 연관 엔티티를 중첩 객체로 반환 (RESTful)
 *
 * [사용 위치]
 * - GroupController의 모든 응답
 * - GroupServiceImpl에서 Entity를 DTO로 변환
 *
 * [프론트엔드 응답 예시]
 * {
 *   "id": 1,
 *   "name": "강남 러닝 크루",
 *   "category": { "id": 1, "name": "운동/스포츠", "icon": "🏃" },
 *   "owner": { "id": 5, "nickname": "홍길동", "profileImage": "..." },
 *   "memberCount": 25,
 *   "myRole": "MEMBER",
 *   ...
 * }
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GroupDTO {

    // ========================================================================
    // 기본 정보
    // ========================================================================

    /** 모임 ID (PK) */
    private Long id;

    /** 모임 이름 */
    private String name;

    /** 모임 소개 */
    private String description;

    // ========================================================================
    // 카테고리 정보 (중첩 객체)
    // ========================================================================

    /** 카테고리 정보 */
    private CategoryDTO category;

    // ========================================================================
    // 위치 정보
    // ========================================================================

    /** 모임 대표 이미지 URL */
    private String coverImage;

    /** 썸네일 이미지 URL */
    private String thumbnailImage;

    /** 모임 주소 (사람이 읽을 수 있는 형태) */
    private String address;

    /** 위치 정보 (중첩 객체) */
    private LocationDTO location;

    // ========================================================================
    // 멤버 관련
    // ========================================================================

    /** 최대 멤버 수 (정원) */
    private int maxMembers;

    /**
     * 현재 멤버 수
     *
     * GroupMemberRepository.countApprovedMembers()로 계산됩니다.
     * 승인된 멤버(APPROVED)만 카운트합니다.
     */
    private int memberCount;

    // ========================================================================
    // 상태 정보
    // ========================================================================

    /** 공개 여부 (true: 공개, false: 비공개) */
    private boolean isPublic;

    /** 모임 상태 (ACTIVE, INACTIVE, DELETED) */
    private String status;

    // ========================================================================
    // 모임장(Owner) 정보 (중첩 객체)
    // ========================================================================

    /** 모임장 정보 */
    private MemberSummaryDTO owner;

    // ========================================================================
    // 현재 로그인 사용자와의 관계
    // ========================================================================
    // 로그인한 사용자가 이 모임과 어떤 관계인지 표시합니다.
    // 프론트엔드에서 버튼 표시 등에 활용됩니다.

    /**
     * 현재 사용자의 모임 내 역할
     *
     * - "OWNER": 모임장 → "모임 관리" 버튼 표시
     * - "MANAGER": 운영진 → "멤버 관리" 버튼 표시
     * - "MEMBER": 일반 멤버 → "탈퇴" 버튼 표시
     * - null: 가입 안 함 → "가입하기" 버튼 표시
     */
    private String myRole;

    /**
     * 현재 사용자의 가입 상태
     *
     * - "APPROVED": 정식 멤버
     * - "PENDING": 가입 대기 중 → "승인 대기 중" 메시지 표시
     * - null: 관계 없음
     *
     * myRole과 함께 사용하여 UI를 결정합니다.
     */
    private String myStatus;

    /** 모임 생성일시 */
    private LocalDateTime createdAt;

    /** 모임 수정일시 */
    private LocalDateTime updatedAt;

    // ========================================================================
    // 위치 정보 내부 클래스
    // ========================================================================

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class LocationDTO {
        private Double lat;
        private Double lng;
    }
}
