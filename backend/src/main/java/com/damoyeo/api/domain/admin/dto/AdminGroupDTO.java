package com.damoyeo.api.domain.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 관리자용 모임 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminGroupDTO {

    /** 모임 ID */
    private Long id;

    /** 모임 이름 */
    private String name;

    /** 카테고리 이름 */
    private String categoryName;

    /** 모임장 닉네임 */
    private String ownerNickname;

    /** 모임장 이메일 */
    private String ownerEmail;

    /** 현재 멤버 수 */
    private int memberCount;

    /** 최대 멤버 수 */
    private int maxMembers;

    /** 모임 상태 (ACTIVE, INACTIVE, DELETED) */
    private String status;

    /** 공개 여부 */
    private boolean isPublic;

    /** 생성일 */
    private LocalDateTime createdAt;
}
