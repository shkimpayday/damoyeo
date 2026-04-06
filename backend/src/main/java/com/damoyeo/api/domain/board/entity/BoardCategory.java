package com.damoyeo.api.domain.board.entity;

/**
 * 모임 게시판 카테고리
 *
 * <ul>
 *   <li>GREETING: 가입인사 - 신규 멤버의 자기소개</li>
 *   <li>REVIEW: 모임후기 - 정모 후기, 활동 소감</li>
 *   <li>FREE: 자유게시판 - 자유 주제의 게시글</li>
 *   <li>NOTICE: 공지사항 - 운영진(OWNER/MANAGER)만 작성 가능</li>
 * </ul>
 */
public enum BoardCategory {
    GREETING,
    REVIEW,
    FREE,
    NOTICE
}
