package com.damoyeo.api.domain.group.entity;

import com.damoyeo.api.domain.category.entity.Category;
import com.damoyeo.api.domain.member.entity.Member;
import com.damoyeo.api.global.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * ============================================================================
 * 모임(Group) 엔티티
 * ============================================================================
 *
 * [역할]
 * "다모여" 플랫폼의 핵심 엔티티로, 사용자들이 만드는 모임을 표현합니다.
 * 소모임(somoim.co.kr)의 "소모임"과 유사한 개념입니다.
 *
 * [DB 테이블]
 * "club" 테이블에 매핑됩니다.
 * ※ "group"은 SQL 예약어이므로 테이블명을 "club"으로 지정했습니다.
 *
 * [관계]
 * - owner (N:1) → Member: 모임장 (모임을 만든 사람)
 * - category (N:1) → Category: 모임 카테고리 (운동, 독서 등)
 * - members (1:N) → GroupMember: 모임 멤버들
 *
 * [사용 위치]
 * - GroupController: 모임 CRUD API
 * - GroupService: 모임 비즈니스 로직
 * - MeetingService: 정모 생성 시 모임 정보 참조
 *
 * @Entity: JPA 엔티티임을 선언
 * @Table(name = "club"): 실제 테이블명 지정
 */
@Entity
@Table(name = "club")  // group은 예약어
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString(exclude = {"owner", "category", "members"})
public class Group extends BaseEntity {

    /**
     * 모임 고유 ID (PK)
     *
     * AUTO_INCREMENT로 자동 생성됩니다.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 모임 이름
     *
     * 예: "강남 러닝 크루", "판교 독서 모임"
     * null 불가
     */
    @Column(nullable = false)
    private String name;

    /**
     * 모임 소개
     *
     * 모임에 대한 상세 설명 (최대 2000자)
     * 예: "매주 토요일 아침 한강에서 러닝합니다..."
     */
    @Column(length = 2000)
    private String description;

    /**
     * 모임 카테고리
     *
     * DataInitializer에서 생성한 18개 카테고리 중 하나
     * 예: 운동/스포츠, 독서, 게임/오락 등
     *
     * LAZY: 모임 조회 시 카테고리를 항상 필요로 하진 않으므로 지연 로딩
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    /**
     * 모임 대표 이미지 URL
     *
     * 모임 목록이나 상세 페이지에서 표시됩니다.
     * null이면 프론트엔드에서 기본 이미지 표시
     */
    private String coverImage;

    /**
     * 모임 위치 (주소)
     *
     * 사람이 읽을 수 있는 주소 형태
     * 예: "서울특별시 강남구 역삼동"
     */
    private String location;

    /**
     * 위도 (Latitude)
     *
     * 위치 기반 검색(근처 모임 찾기)에 사용됩니다.
     * 예: 37.5665 (서울시청)
     */
    private Double latitude;

    /**
     * 경도 (Longitude)
     *
     * 위치 기반 검색에 사용됩니다.
     * 예: 126.9780 (서울시청)
     */
    private Double longitude;

    /**
     * 최대 멤버 수
     *
     * 이 숫자를 초과하면 가입 신청 불가
     * 예: 50명
     */
    @Column(nullable = false)
    private int maxMembers;

    /**
     * 공개 여부
     *
     * true: 누구나 검색/조회 가능
     * false: 초대받은 사람만 접근 가능 (Phase 2에서 구현 예정)
     */
    @Column(nullable = false)
    private boolean isPublic;

    /**
     * 모임 상태
     *
     * - ACTIVE: 활성 (정상 운영 중)
     * - INACTIVE: 비활성 (일시 중단)
     * - DELETED: 삭제됨 (소프트 삭제)
     *
     * 기본값: ACTIVE
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private GroupStatus status = GroupStatus.ACTIVE;

    /**
     * 모임장 (소유자)
     *
     * 모임을 생성한 사람이며, 모든 권한을 가집니다.
     * - 모임 정보 수정
     * - 멤버 관리 (강퇴, 역할 변경)
     * - 운영진 지정
     * - 모임 삭제
     *
     * LAZY: 모임 목록 조회 시 모임장 정보가 항상 필요하진 않음
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private Member owner;

    /**
     * 모임 멤버 목록
     *
     * GroupMember 엔티티를 통해 모임과 회원의 N:M 관계를 관리합니다.
     *
     * - cascade = ALL: 모임 삭제 시 멤버 관계도 함께 삭제
     * - orphanRemoval = true: 컬렉션에서 제거된 엔티티는 DB에서도 삭제
     */
    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<GroupMember> members = new ArrayList<>();

    // ========================================================================
    // 변경 메서드 (Setter 대신 명시적인 변경 메서드 사용)
    // ========================================================================
    // JPA 엔티티는 Setter를 직접 노출하지 않고,
    // 의미 있는 이름의 변경 메서드를 통해 상태를 변경하는 것이 좋습니다.
    // 이렇게 하면 어디서 어떤 이유로 값이 변경되는지 추적하기 쉽습니다.

    /** 모임 이름 변경 */
    public void changeName(String name) {
        this.name = name;
    }

    /** 모임 설명 변경 */
    public void changeDescription(String description) {
        this.description = description;
    }

    /** 대표 이미지 변경 */
    public void changeCoverImage(String coverImage) {
        this.coverImage = coverImage;
    }

    /**
     * 위치 정보 변경
     *
     * 주소, 위도, 경도를 한 번에 변경합니다.
     * 위치 정보는 서로 연관되어 있으므로 함께 변경하는 것이 안전합니다.
     */
    public void changeLocation(String location, Double latitude, Double longitude) {
        this.location = location;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    /** 최대 멤버 수 변경 */
    public void changeMaxMembers(int maxMembers) {
        this.maxMembers = maxMembers;
    }

    /** 카테고리 변경 */
    public void changeCategory(Category category) {
        this.category = category;
    }

    /** 공개여부 변경 */
    public void changeIsPublic(Boolean isPublic) {
        this.isPublic = isPublic;
    }

    /** 모임 상태 변경 (ACTIVE, INACTIVE, DELETED) */
    public void changeStatus(GroupStatus status) {
        this.status = status;
    }
}
