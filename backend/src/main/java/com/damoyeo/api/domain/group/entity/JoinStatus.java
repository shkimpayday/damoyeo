package com.damoyeo.api.domain.group.entity;

/**
 * 모임 가입 상태 Enum
 *
 * 회원의 모임 가입 상태를 나타냅니다.
 *
 * [가입 흐름]
 * 모임 가입 시 즉시 APPROVED 상태로 등록됩니다.
 *
 * - GroupMember 엔티티의 status 필드
 * - GroupService.join(): APPROVED로 생성 (즉시 가입)
 *
 * [DB 저장]
 * @Enumerated(EnumType.STRING)으로 문자열 저장
 */
public enum JoinStatus {

    /**
     * 승인됨 (가입 완료)
     *
     * 모임에 가입이 완료된 상태입니다.
     * 가입 시 즉시 이 상태로 등록됩니다.
     *
     * [특징]
     * - 정식 멤버로 활동 가능
     * - 모임 멤버 목록에 표시됨
     * - 채팅, 정모 참석 등 모든 기능 이용 가능
     */
    APPROVED,

    /**
     * 강퇴됨
     *
     * 모임장/운영진에 의해 강퇴된 상태입니다.
     *
     * [특징]
     * - 모임 기능 이용 불가
     * - 멤버 목록에서 제외됨
     */
    BANNED
}
