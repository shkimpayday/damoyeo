package com.damoyeo.api.domain.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 대시보드 통계 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsDTO {

    /** 전체 회원 수 */
    private long totalMembers;

    /** 전체 모임 수 */
    private long totalGroups;

    /** 전체 정모 수 */
    private long totalMeetings;

    /** 오늘 신규 가입자 수 */
    private long todayNewMembers;

    /** 활성 모임 수 */
    private long activeGroups;

    /** 예정된 정모 수 */
    private long upcomingMeetings;
}
