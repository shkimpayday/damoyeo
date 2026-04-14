package com.damoyeo.api.domain.group.service;

import com.damoyeo.api.domain.group.dto.*;
import com.damoyeo.api.global.common.dto.PageRequestDTO;
import com.damoyeo.api.global.common.dto.PageResponseDTO;

import java.util.List;

/**
 * 모임 서비스 인터페이스
 *
 * 모임 관련 비즈니스 로직의 계약(contract)을 정의합니다.
 *
 * [왜 인터페이스를 분리하는가?]
 * 1. 구현과 계약 분리: 나중에 다른 구현체로 교체 가능
 * 2. 테스트 용이성: Mock 객체로 쉽게 대체 가능
 * 3. 의존성 역전: Controller는 인터페이스에만 의존
 *
 * [기능 분류]
 * - CRUD: 모임 생성/조회/수정/삭제
 * - 목록 조회: 리스트, 검색, 내 모임, 근처 모임
 * - 멤버 관리: 가입, 탈퇴, 강퇴, 역할 변경
 *
 * - GroupController에서 주입받아 사용
 * - GroupServiceImpl에서 구현
 */
public interface GroupService {

    // CRUD 기본 기능

    /**
     * 모임 생성
     *
     * [처리 흐름]
     * 1. 요청한 사용자를 모임장(owner)으로 설정
     * 2. 카테고리 확인
     * 3. 모임 엔티티 생성 및 저장
     * 4. 모임장을 자동으로 멤버(OWNER, APPROVED)로 등록
     *
     * @param email 모임장이 될 사용자의 이메일
     * @param request 모임 생성 정보
     * @return 생성된 모임 정보
     *
     * Controller: POST /api/groups
     */
    GroupDTO create(String email, GroupCreateRequest request);

    /**
     * 모임 상세 조회
     *
     * [특징]
     * - 삭제된 모임(DELETED)은 조회 불가
     * - email이 주어지면 현재 사용자와 모임의 관계(myRole, myStatus) 포함
     *
     * @param id 모임 ID
     * @param email 조회하는 사용자의 이메일 (null 가능)
     * @return 모임 상세 정보
     *
     * Controller: GET /api/groups/{id}
     */
    GroupDTO getById(Long id, String email);

    /**
     * 모임 정보 수정
     *
     * [권한]
     * 모임장(OWNER) 또는 운영진(MANAGER)만 가능
     *
     * [특징]
     * request의 null 필드는 기존 값 유지
     *
     * @param id 모임 ID
     * @param email 요청자 이메일 (권한 확인용)
     * @param request 수정할 정보
     * @return 수정된 모임 정보
     *
     * Controller: PUT /api/groups/{id}
     */
    GroupDTO modify(Long id, String email, GroupModifyRequest request);

    /**
     * 모임 삭제 (소프트 삭제)
     *
     * [권한]
     * 모임장(OWNER)만 가능
     *
     * [동작]
     * 실제로 DB에서 삭제하지 않고 status를 DELETED로 변경합니다.
     *
     * @param id 모임 ID
     * @param email 요청자 이메일 (권한 확인용)
     *
     * Controller: DELETE /api/groups/{id}
     */
    void delete(Long id, String email);

    // 목록 조회

    /**
     * 모임 목록 조회 (페이지네이션 + 검색 + 정렬)
     *
     * [조건]
     * - ACTIVE 상태인 모임만 조회
     * - categoryId가 주어지면 해당 카테고리 필터링
     * - keyword가 주어지면 모임 이름 검색
     *
     * @param pageRequestDTO 페이지 정보 (page, size)
     * @param categoryId 카테고리 ID (null이면 전체)
     * @param keyword 검색 키워드 (null이면 검색 안 함)
     * @param sort 정렬 기준 (latest, popular)
     * @return 페이지네이션된 모임 목록
     *
     * Controller: GET /api/groups?categoryId=1&keyword=러닝&sort=popular&page=1&size=10
     */
    PageResponseDTO<GroupDTO> getList(PageRequestDTO pageRequestDTO, Long categoryId, String keyword, String sort);

    /**
     * 모임 검색
     *
     * [검색 대상]
     * 모임 이름에 키워드가 포함된 모임
     *
     * @param keyword 검색 키워드
     * @param pageRequestDTO 페이지 정보
     * @return 검색 결과 (페이지네이션)
     *
     * Controller: GET /api/groups/search?keyword=러닝&page=1
     */
    PageResponseDTO<GroupDTO> search(String keyword, PageRequestDTO pageRequestDTO);

    /**
     * 내가 가입한 모임 목록 조회
     *
     * @param email 사용자 이메일
     * @return 내 모임 목록
     *
     * Controller: GET /api/groups/my
     */
    List<GroupDTO> getMyGroups(String email);

    /**
     * 근처 모임 검색 (위치 기반)
     *
     * [알고리즘]
     * Haversine 공식으로 거리 계산
     *
     * @param lat 사용자 위도
     * @param lng 사용자 경도
     * @param radiusKm 검색 반경 (km)
     * @return 반경 내 모임 목록 (거리순)
     *
     * Controller: GET /api/groups/nearby?lat=37.5&lng=127.0&radius=5
     */
    List<GroupDTO> getNearbyGroups(double lat, double lng, double radiusKm);

    /**
     * 추천 모임 조회
     *
     * [동작]
     * 인기 모임 또는 최신 모임을 추천
     *
     * @return 추천 모임 목록
     *
     * Controller: GET /api/groups/recommended
     */
    List<GroupDTO> getRecommendedGroups();

    // 멤버 관리

    /**
     * 모임 가입
     *
     * [처리 흐름]
     * 1. 중복 가입 확인 (이미 멤버인 경우 에러)
     * 2. 정원 확인 (가득 찼으면 에러)
     * 3. GroupMember 생성 (즉시 APPROVED)
     *
     * @param groupId 모임 ID
     * @param email 가입자 이메일
     *
     * Controller: POST /api/groups/{id}/join
     */
    void join(Long groupId, String email);

    /**
     * 모임 탈퇴
     *
     * [제한]
     * 모임장(OWNER)은 탈퇴 불가 (먼저 모임장을 위임해야 함)
     *
     * @param groupId 모임 ID
     * @param email 탈퇴할 사용자 이메일
     *
     * Controller: POST /api/groups/{id}/leave
     */
    void leave(Long groupId, String email);

    /**
     * 멤버 강퇴
     *
     * [권한]
     * 모임장(OWNER) 또는 운영진(MANAGER)
     *
     * [제한]
     * 모임장(OWNER)은 강퇴 불가
     *
     * @param groupId 모임 ID
     * @param memberId 강퇴할 회원 ID
     * @param ownerEmail 요청자 이메일 (권한 확인)
     *
     * Controller: DELETE /api/groups/{id}/members/{memberId}
     */
    void kickMember(Long groupId, Long memberId, String ownerEmail);

    /**
     * 멤버 역할 변경
     *
     * [권한]
     * 모임장(OWNER)만 가능
     *
     * [용도]
     * 일반 멤버 → 운영진 승격 등
     *
     * @param groupId 모임 ID
     * @param memberId 대상 회원 ID
     * @param role 새 역할 (MANAGER, MEMBER)
     * @param ownerEmail 요청자 이메일 (모임장 확인)
     *
     * Controller: PUT /api/groups/{id}/members/{memberId}/role
     */
    void changeRole(Long groupId, Long memberId, String role, String ownerEmail);

    /**
     * 멤버 목록 조회
     *
     * @param groupId 모임 ID
     * @param status 조회할 상태 (null이면 APPROVED)
     * @return 멤버 목록
     *
     * Controller: GET /api/groups/{id}/members
     */
    List<GroupMemberDTO> getMembers(Long groupId, String status);


    //// 정모 목록용 DTO (간소화)
    //export interface MeetingListDTO {
    //  id: number;
    //  groupId: number;
    //  groupName: string;
    //  title: string;
    //  address: string;
    //  meetingDate: string;
    //  maxAttendees: number;
    //  currentAttendees: number;
    //  status: MeetingStatus;
    //}



}
