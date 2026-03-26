package com.damoyeo.api.domain.member.entity;

import com.damoyeo.api.global.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * ============================================================================
 * 회원 엔티티 (Member)
 * ============================================================================
 *
 * [역할]
 * 회원 정보를 저장하는 JPA 엔티티입니다.
 * DB의 member 테이블과 매핑됩니다.
 *
 * [테이블 구조]
 * member 테이블:
 * - id: 기본키 (자동 증가)
 * - email: 이메일 (로그인 ID, 유니크)
 * - password: 비밀번호 (BCrypt 암호화)
 * - nickname: 닉네임
 * - profile_image: 프로필 이미지 URL
 * - introduction: 자기소개
 * - social: 소셜 로그인 여부
 * - created_at, modified_at: BaseEntity에서 상속
 *
 * [관련 테이블]
 * member_role 테이블:
 * - member_id: 회원 ID (FK)
 * - member_role_list: 권한 (USER, ADMIN)
 *
 * ▶ extends BaseEntity: createdAt, modifiedAt 상속
 */
@Entity
@Table(name = "member")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString(exclude = "memberRoleList")  // 순환 참조 방지
public class Member extends BaseEntity {

    /**
     * 회원 고유 ID (기본키)
     *
     * @GeneratedValue(IDENTITY): AUTO_INCREMENT
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 이메일 (로그인 ID)
     *
     * unique: 중복 불가
     * nullable = false: 필수
     *
     * [사용] CustomUserDetailsService.loadUserByUsername()
     */
    @Column(unique = true, nullable = false)
    private String email;

    /**
     * 비밀번호 (BCrypt 암호화)
     *
     * DB에는 암호화된 문자열 저장
     */
    @Column(nullable = false)
    private String password;

    /**
     * 닉네임
     */
    @Column(nullable = false)
    private String nickname;

    /**
     * 프로필 이미지 URL
     */
    private String profileImage;

    /**
     * 자기소개 (최대 200자)
     */
    private String introduction;

    /**
     * 소셜 로그인 여부
     */
    @Column(nullable = false)
    private boolean social;

    /**
     * 사용자 위치 - 위도
     */
    private Double lat;

    /**
     * 사용자 위치 - 경도
     */
    private Double lng;

    /**
     * 사용자 주소
     */
    private String address;

    /**
     * 활동 중인 모임 공개 여부 (프리미엄 회원 전용 설정)
     *
     * true: 공개 (기본값)
     * false: 비공개
     */
    @Builder.Default
    private boolean showJoinedGroups = true;

    /**
     * 권한 목록
     *
     * @ElementCollection: 별도 테이블(member_role)에 저장
     * LAZY 로딩: 접근 시 쿼리 실행
     *
     * [권한] USER: 일반, ADMIN: 관리자
     */
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "member_role", joinColumns = @JoinColumn(name = "member_id"))
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private List<MemberRole> memberRoleList = new ArrayList<>();

    /** 권한 추가 */
    public void addRole(MemberRole role) {
        memberRoleList.add(role);
    }

    /** 모든 권한 삭제 */
    public void clearRoles() {
        memberRoleList.clear();
    }

    /** 닉네임 변경 (JPA 더티 체킹) */
    public void changeNickname(String nickname) {
        this.nickname = nickname;
    }

    /** 비밀번호 변경 (암호화된 값 전달) */
    public void changePassword(String password) {
        this.password = password;
    }

    /** 프로필 이미지 변경 */
    public void changeProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    /** 자기소개 변경 */
    public void changeIntroduction(String introduction) {
        this.introduction = introduction;
    }

    /** 위치 변경 */
    public void changeLocation(Double lat, Double lng, String address) {
        this.lat = lat;
        this.lng = lng;
        this.address = address;
    }

    /** 활동 모임 공개 여부 변경 (프리미엄 전용) */
    public void changeShowJoinedGroups(boolean showJoinedGroups) {
        this.showJoinedGroups = showJoinedGroups;
    }
}
