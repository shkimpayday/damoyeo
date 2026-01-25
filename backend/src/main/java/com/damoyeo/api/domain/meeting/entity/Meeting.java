package com.damoyeo.api.domain.meeting.entity;

import com.damoyeo.api.domain.group.entity.Group;
import com.damoyeo.api.domain.member.entity.Member;
import com.damoyeo.api.global.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * ============================================================================
 * 정모(Meeting) 엔티티
 * ============================================================================
 *
 * [역할]
 * 모임 내에서 진행되는 정기/비정기 오프라인 모임을 표현합니다.
 * 소모임(somoim.co.kr)의 "정모"와 유사한 개념입니다.
 *
 * [용어 설명]
 * - 모임(Group): 사람들이 모인 그룹 (예: "강남 러닝 크루")
 * - 정모(Meeting): 그 모임에서 진행하는 실제 만남 (예: "5월 첫째 주 러닝")
 *
 * [관계]
 * - group (N:1) → Group: 이 정모가 속한 모임
 * - creator (N:1) → Member: 정모를 만든 사람
 * - attendees (1:N) → MeetingAttendee: 참석자 목록
 *
 * [사용 위치]
 * - MeetingController: 정모 CRUD API
 * - MeetingService: 정모 비즈니스 로직
 *
 * [DB 테이블]
 * meeting 테이블
 */
@Entity
@Table(name = "meeting")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString(exclude = {"group", "creator", "attendees"})
public class Meeting extends BaseEntity {

    /**
     * 정모 고유 ID (PK)
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 소속 모임
     *
     * 이 정모가 어느 모임에서 진행되는지를 나타냅니다.
     * 정모는 반드시 특정 모임에 속해야 합니다.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private Group group;

    /**
     * 정모 제목
     *
     * 예: "5월 첫째 주 러닝", "독서 토론회 - 데미안"
     */
    @Column(nullable = false)
    private String title;

    /**
     * 정모 설명
     *
     * 정모에 대한 상세 설명 (최대 2000자)
     * 예: "이번 주는 한강 반포대교에서 5km 러닝합니다..."
     */
    @Column(length = 2000)
    private String description;

    // ========================================================================
    // 위치 정보
    // ========================================================================

    /**
     * 정모 장소 (주소)
     *
     * 사람이 읽을 수 있는 주소 형태
     * 예: "서울특별시 서초구 반포대로 11길"
     */
    private String location;

    /**
     * 위도 (Latitude)
     *
     * 프론트엔드에서 지도 표시에 사용됩니다.
     */
    private Double latitude;

    /**
     * 경도 (Longitude)
     */
    private Double longitude;

    /**
     * 정모 일시
     *
     * 정모가 진행되는 날짜와 시간
     * 예: 2024-05-04 10:00
     */
    @Column(nullable = false)
    private LocalDateTime meetingDate;

    /**
     * 최대 참석 인원
     *
     * 이 숫자를 초과하면 참석 신청 불가
     * 예: 20명
     */
    @Column(nullable = false)
    private int maxAttendees;

    /**
     * 참가비
     *
     * 정모 참석에 필요한 비용 (원)
     * 기본값: 0 (무료)
     * 예: 10000 (만원)
     */
    @Column(nullable = false)
    @Builder.Default
    private int fee = 0;

    /**
     * 정모 상태
     *
     * - SCHEDULED: 예정됨 (기본값)
     * - ONGOING: 진행 중
     * - COMPLETED: 완료됨
     * - CANCELLED: 취소됨
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private MeetingStatus status = MeetingStatus.SCHEDULED;

    /**
     * 정모 생성자
     *
     * 정모를 만든 사람입니다.
     * 모임장(OWNER)이 아니어도 운영진(MANAGER)이면 정모를 만들 수 있습니다.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id", nullable = false)
    private Member creator;

    /**
     * 참석자 목록
     *
     * MeetingAttendee 엔티티를 통해 정모와 회원의 N:M 관계를 관리합니다.
     *
     * - cascade = ALL: 정모 삭제 시 참석자 관계도 함께 삭제
     * - orphanRemoval = true: 컬렉션에서 제거된 엔티티는 DB에서도 삭제
     */
    @OneToMany(mappedBy = "meeting", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<MeetingAttendee> attendees = new ArrayList<>();

    // ========================================================================
    // 변경 메서드
    // ========================================================================

    /** 정모 제목 변경 */
    public void changeTitle(String title) {
        this.title = title;
    }

    /** 정모 설명 변경 */
    public void changeDescription(String description) {
        this.description = description;
    }

    /** 위치 정보 변경 */
    public void changeLocation(String location, Double latitude, Double longitude) {
        this.location = location;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    /** 정모 일시 변경 */
    public void changeMeetingDate(LocalDateTime meetingDate) {
        this.meetingDate = meetingDate;
    }

    /** 최대 참석 인원 변경 */
    public void changeMaxAttendees(int maxAttendees) {
        this.maxAttendees = maxAttendees;
    }

    /** 참가비 변경 */
    public void changeFee(int fee) {
        this.fee = fee;
    }

    /** 정모 상태 변경 */
    public void changeStatus(MeetingStatus status) {
        this.status = status;
    }
}
