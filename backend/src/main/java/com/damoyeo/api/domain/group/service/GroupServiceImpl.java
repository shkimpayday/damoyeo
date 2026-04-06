package com.damoyeo.api.domain.group.service;

import com.damoyeo.api.domain.category.dto.CategoryDTO;
import com.damoyeo.api.domain.category.entity.Category;
import com.damoyeo.api.domain.category.repository.CategoryRepository;
import com.damoyeo.api.domain.group.dto.*;
import com.damoyeo.api.domain.group.entity.*;
import com.damoyeo.api.domain.group.repository.GroupMemberRepository;
import com.damoyeo.api.domain.group.repository.GroupRepository;
import com.damoyeo.api.domain.member.dto.MemberSummaryDTO;
import com.damoyeo.api.domain.member.entity.Member;
import com.damoyeo.api.domain.member.entity.MemberRole;
import com.damoyeo.api.domain.member.repository.MemberRepository;
import com.damoyeo.api.domain.notification.entity.NotificationType;
import com.damoyeo.api.domain.notification.repository.NotificationRepository;
import com.damoyeo.api.domain.notification.service.NotificationService;
import com.damoyeo.api.global.common.dto.PageRequestDTO;
import com.damoyeo.api.global.common.dto.PageResponseDTO;
import com.damoyeo.api.global.exception.CustomException;
import com.damoyeo.api.global.util.FileUploadUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * ============================================================================
 * 모임 서비스 구현체
 * ============================================================================
 *
 * [역할]
 * 모임 관련 모든 비즈니스 로직을 구현합니다.
 *
 * [어노테이션 설명]
 * @Service: Spring 빈으로 등록 (서비스 계층)
 * @RequiredArgsConstructor: final 필드에 대한 생성자 자동 생성 (의존성 주입)
 * @Transactional: 모든 public 메서드에 트랜잭션 적용
 * @Slf4j: 로깅을 위한 log 객체 자동 생성
 *
 * [트랜잭션 전략]
 * - 기본: @Transactional (쓰기 작업)
 * - 조회만: @Transactional(readOnly = true) (성능 최적화)
 *
 * [사용하는 Repository]
 * - GroupRepository: 모임 CRUD
 * - GroupMemberRepository: 멤버십 관리
 * - MemberRepository: 회원 조회
 * - CategoryRepository: 카테고리 조회
 */
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class GroupServiceImpl implements GroupService {

    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final MemberRepository memberRepository;
    private final CategoryRepository categoryRepository;
    private final FileUploadUtil fileUploadUtil;
    private final NotificationService notificationService;

    // ========================================================================
    // 프리미엄 회원 제한 상수
    // ========================================================================

    /** 일반 회원 모임 생성 제한 (2개) */
    private static final int NORMAL_GROUP_LIMIT = 2;

    /** 일반 회원 모임 인원 제한 (30명) */
    private static final int NORMAL_MEMBER_LIMIT = 30;

    // ========================================================================
    // CRUD 기본 기능
    // ========================================================================

    /**
     * 모임 생성
     *
     * [처리 흐름]
     * 1. 요청한 사용자(email)를 DB에서 조회 → 모임장이 됨
     * 2. 카테고리 존재 확인
     * 3. Group 엔티티 생성 및 저장
     * 4. 모임장을 GroupMember로 자동 등록 (OWNER, APPROVED)
     *
     * [왜 모임장을 GroupMember로도 등록하는가?]
     * - 멤버 목록 조회 시 모임장도 함께 표시하기 위함
     * - 멤버 수 집계에 모임장도 포함하기 위함
     * - 일관된 구조 유지 (모든 멤버가 GroupMember 테이블에 존재)
     */
    @Override
    public GroupDTO create(String email, GroupCreateRequest request) {
        // 1. 모임장이 될 회원 조회 (권한 정보 포함)
        Member owner = memberRepository.getWithRoles(email)
                .orElseThrow(() -> CustomException.notFound("회원을 찾을 수 없습니다."));

        // 2. 프리미엄 회원 여부 확인
        boolean isPremium = owner.getMemberRoleList().contains(MemberRole.PREMIUM);

        // 3. 일반 회원 모임 생성 제한 확인 (2개)
        if (!isPremium) {
            int ownedGroupCount = groupRepository.countOwnedGroups(owner.getId());
            if (ownedGroupCount >= NORMAL_GROUP_LIMIT) {
                throw new CustomException(
                        "일반 회원은 최대 " + NORMAL_GROUP_LIMIT + "개의 모임만 생성할 수 있습니다. " +
                                "프리미엄 회원으로 업그레이드하면 무제한으로 모임을 생성할 수 있습니다.",
                        HttpStatus.FORBIDDEN
                );
            }
        }

        // 4. 일반 회원 모임 인원 제한 확인 (30명)
        int maxMembers = request.getMaxMembers();
        if (!isPremium && maxMembers > NORMAL_MEMBER_LIMIT) {
            throw new CustomException(
                    "일반 회원은 최대 " + NORMAL_MEMBER_LIMIT + "명까지만 모임 인원을 설정할 수 있습니다. " +
                            "프리미엄 회원으로 업그레이드하면 인원 제한 없이 모임을 운영할 수 있습니다.",
                    HttpStatus.FORBIDDEN
            );
        }

        // 5. 카테고리 확인
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> CustomException.notFound("카테고리를 찾을 수 없습니다."));

        // 6. 모임 엔티티 생성
        Group group = Group.builder()
                .name(request.getName())
                .description(request.getDescription())
                .category(category)
                .coverImage(request.getCoverImage())
                .location(request.getLocation())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .maxMembers(maxMembers)
                .isPublic(request.isPublic())
                .owner(owner)
                .build();

        Group saved = groupRepository.save(group);

        // 4. 모임장을 멤버로 자동 등록
        // - role: OWNER (모임장)
        // - status: APPROVED (즉시 승인)
        GroupMember ownerMember = GroupMember.builder()
                .group(saved)
                .member(owner)
                .role(GroupRole.OWNER)
                .status(JoinStatus.APPROVED)
                .build();
        groupMemberRepository.save(ownerMember);

        log.info("Group created: {} by {}", saved.getName(), email);
        return entityToDTO(saved, email);
    }

    /**
     * 모임 상세 조회
     *
     * @Transactional(readOnly = true)
     * - 읽기 전용 트랜잭션으로 성능 최적화
     * - Hibernate 플러시 모드가 NEVER로 설정되어 더티 체킹 비용 절감
     */
    @Override
    @Transactional(readOnly = true)
    public GroupDTO getById(Long id, String email) {
        Group group = groupRepository.findByIdWithDetails(id)
                .orElseThrow(() -> CustomException.notFound("모임을 찾을 수 없습니다."));
        return entityToDTO(group, email);
    }

    /**
     * 모임 정보 수정
     *
     * [특징]
     * - null인 필드는 무시하고 기존 값 유지
     * - 권한 검사: 모임장 또는 운영진만 가능
     *
     * [JPA 더티 체킹]
     * group.changeName() 등의 메서드로 값을 변경하면
     * 트랜잭션 종료 시 JPA가 자동으로 UPDATE 쿼리를 실행합니다.
     * 별도의 save() 호출이 필요 없습니다.
     */
    @Override
    public GroupDTO modify(Long id, String email, GroupModifyRequest request) {
        Group group = groupRepository.findByIdWithDetails(id)
                .orElseThrow(() -> CustomException.notFound("모임을 찾을 수 없습니다."));

        // 권한 검사: 모임장 또는 운영진만 수정 가능
        checkOwnerOrManager(group, email);

        // null이 아닌 필드만 업데이트 (선택적 수정)
        if (request.getName() != null) {
            group.changeName(request.getName());
        }
        if (request.getDescription() != null) {
            group.changeDescription(request.getDescription());
        }
        if(request.getIsPublic() != null) {
            group.changeIsPublic(request.getIsPublic());
        }
        // 커버 이미지 파일 업로드 처리
        if (request.getCoverImageFile() != null && !request.getCoverImageFile().isEmpty()) {
            String imageUrl = fileUploadUtil.uploadGroupImage(request.getCoverImageFile());
            group.changeCoverImage(imageUrl);
        } else if (request.getCoverImage() != null) {
            group.changeCoverImage(request.getCoverImage());
        }
        if (request.getLocation() != null) {
            // lat/lng가 null이면 기존 값 유지
            Double lat = request.getLatitude() != null ? request.getLatitude() : group.getLatitude();
            Double lng = request.getLongitude() != null ? request.getLongitude() : group.getLongitude();
            group.changeLocation(request.getLocation(), lat, lng);
        }
        if (request.getMaxMembers() != null) {
            // 일반 회원 모임 인원 제한 확인 (30명)
            Member owner = memberRepository.getWithRoles(group.getOwner().getEmail())
                    .orElseThrow(() -> CustomException.notFound("모임장 정보를 찾을 수 없습니다."));
            boolean isPremium = owner.getMemberRoleList().contains(MemberRole.PREMIUM);

            if (!isPremium && request.getMaxMembers() > NORMAL_MEMBER_LIMIT) {
                throw new CustomException(
                        "일반 회원은 최대 " + NORMAL_MEMBER_LIMIT + "명까지만 모임 인원을 설정할 수 있습니다. " +
                                "프리미엄 회원으로 업그레이드하면 인원 제한 없이 모임을 운영할 수 있습니다.",
                        HttpStatus.FORBIDDEN
                );
            }
            group.changeMaxMembers(request.getMaxMembers());
        }
        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> CustomException.notFound("카테고리를 찾을 수 없습니다."));
            group.changeCategory(category);
        }

        // JPA 더티 체킹으로 자동 UPDATE (save 불필요)
        return entityToDTO(group, email);
    }

    /**
     * 모임 삭제 (소프트 삭제)
     *
     * [소프트 삭제란?]
     * 실제로 DB에서 삭제하지 않고 status를 DELETED로 변경합니다.
     *
     * [장점]
     * 1. 실수로 삭제한 경우 복구 가능
     * 2. 정모, 채팅 등 관련 데이터의 참조 무결성 유지
     * 3. 통계/분석용 데이터 보존
     *
     * [권한]
     * 모임장만 삭제 가능
     */
    @Override
    public void delete(Long id, String email) {
        Group group = groupRepository.findByIdWithDetails(id)
                .orElseThrow(() -> CustomException.notFound("모임을 찾을 수 없습니다."));

        // 권한 검사: 모임장만 삭제 가능
        checkOwner(group, email);

        // 모임 해체 전에 모든 멤버에게 알림 발송 (모임장 제외)
        List<GroupMember> members = groupMemberRepository.findByGroupIdAndStatus(id, JoinStatus.APPROVED);
        String groupName = group.getName();

        for (GroupMember gm : members) {
            // 모임장은 알림 제외 (본인이 해체한 것이므로)
            if (gm.getRole() != GroupRole.OWNER) {
                notificationService.send(
                        gm.getMember(),
                        NotificationType.GROUP_DISBANDED,
                        "모임이 해체되었습니다",
                        "'" + groupName + "'이(가) 해체되었습니다.",
                        id
                );
            }
        }

        // 소프트 삭제: 상태만 변경
        group.changeStatus(GroupStatus.DELETED);
        log.info("Group deleted: {} by {}", group.getName(), email);
    }

    // ========================================================================
    // 목록 조회
    // ========================================================================

    /**
     * 모임 목록 조회 (페이지네이션 + 검색 + 정렬)
     *
     * [조건]
     * - categoryId가 있으면 해당 카테고리만 필터링
     * - keyword가 있으면 모임 이름 검색
     * - sort: latest(최신순), popular(인기순)
     *
     * [정렬 기준]
     * - latest: 생성일 기준 내림차순 (기본값)
     * - popular: 멤버 수 기준 내림차순
     */
    @Override
    @Transactional(readOnly = true)
    public PageResponseDTO<GroupDTO> getList(PageRequestDTO pageRequestDTO, Long categoryId, String keyword, String sort) {
        Page<Group> result;
        boolean isPopular = "popular".equalsIgnoreCase(sort);
        boolean hasKeyword = keyword != null && !keyword.trim().isEmpty();
        boolean hasCategory = categoryId != null;

        if (isPopular) {
            // 인기순 정렬 (멤버 수 기준) - Native Query가 SQL 내부에서 ORDER BY 처리
            // getUnsortedPageable()을 사용하지 않으면 Spring이 Pageable의 Sort를
            // SQL에 추가하려 해서 native query와 충돌하여 500 오류 발생
            Pageable unsortedPageable = pageRequestDTO.getUnsortedPageable();
            if (hasKeyword && hasCategory) {
                result = groupRepository.searchByKeywordAndCategoryOrderByMemberCount(
                        keyword.trim(), categoryId, unsortedPageable);
            } else if (hasKeyword) {
                result = groupRepository.searchByKeywordOrderByMemberCount(
                        keyword.trim(), unsortedPageable);
            } else if (hasCategory) {
                result = groupRepository.findByCategoryIdOrderByMemberCount(
                        categoryId, unsortedPageable);
            } else {
                result = groupRepository.findAllOrderByMemberCount(unsortedPageable);
            }
        } else {
            // 최신순 정렬 (기본값): id 기준 내림차순
            if (hasKeyword && hasCategory) {
                result = groupRepository.searchByKeywordAndCategory(
                        keyword.trim(), categoryId, pageRequestDTO.getPageable("id"));
            } else if (hasKeyword) {
                result = groupRepository.searchByKeyword(keyword.trim(), pageRequestDTO.getPageable("id"));
            } else if (hasCategory) {
                result = groupRepository.findByCategoryId(categoryId, pageRequestDTO.getPageable("id"));
            } else {
                result = groupRepository.findAllByStatus(GroupStatus.ACTIVE, pageRequestDTO.getPageable("id"));
            }
        }

        // Entity → DTO 변환
        List<GroupDTO> dtoList = result.getContent().stream()
                .map(g -> entityToDTO(g, null))  // 비로그인 상태로 변환
                .collect(Collectors.toList());

        // PageResponseDTO 생성 (Builder 패턴)
        return PageResponseDTO.<GroupDTO>builder()
                .pageRequestDTO(pageRequestDTO)
                .dtoList(dtoList)
                .totalCount((int) result.getTotalElements())
                .build();
    }

    /**
     * 모임 검색
     *
     * [검색 방식]
     * 모임 이름에 키워드가 포함된 모임 검색 (LIKE '%keyword%')
     */
    @Override
    @Transactional(readOnly = true)
    public PageResponseDTO<GroupDTO> search(String keyword, PageRequestDTO pageRequestDTO) {
        Page<Group> result = groupRepository.searchByKeyword(keyword, pageRequestDTO.getPageable("id"));

        List<GroupDTO> dtoList = result.getContent().stream()
                .map(g -> entityToDTO(g, null))
                .collect(Collectors.toList());

        return PageResponseDTO.<GroupDTO>builder()
                .pageRequestDTO(pageRequestDTO)
                .dtoList(dtoList)
                .totalCount((int) result.getTotalElements())
                .build();
    }

    /**
     * 내가 가입한 모임 목록 조회
     *
     * [조건]
     * 가입된 멤버(APPROVED)인 모임만 조회합니다.
     */
    @Override
    @Transactional(readOnly = true)
    public List<GroupDTO> getMyGroups(String email) {
        Member member = getMemberByEmail(email);

        // GroupMember에서 모임 정보를 꺼내서 DTO로 변환
        return groupMemberRepository.findMyGroups(member.getId()).stream()
                .map(gm -> entityToDTO(gm.getGroup(), email))
                .collect(Collectors.toList());
    }

    /**
     * 근처 모임 검색 (위치 기반)
     *
     * [알고리즘]
     * GroupRepository.findNearbyGroups()에서 Haversine 공식을 사용하여
     * 지정된 반경 내의 모임을 거리순으로 조회합니다.
     */
    @Override
    @Transactional(readOnly = true)
    public List<GroupDTO> getNearbyGroups(double lat, double lng, double radiusKm) {
        return groupRepository.findNearbyGroups(lat, lng, radiusKm).stream()
                .map(g -> entityToDTO(g, null))
                .collect(Collectors.toList());
    }

    /**
     * 추천 모임 조회
     *
     * [추천 기준]
     * 활성 상태(ACTIVE)인 모임 중 최신순으로 10개를 추천합니다.
     * 추후 인기도, 사용자 관심사 기반 추천으로 확장 가능합니다.
     */
    @Override
    @Transactional(readOnly = true)
    public List<GroupDTO> getRecommendedGroups() {
        return groupRepository.findTop10ByStatusOrderByCreatedAtDesc(GroupStatus.ACTIVE).stream()
                .map(g -> entityToDTO(g, null))
                .collect(Collectors.toList());
    }

    // ========================================================================
    // 멤버 관리
    // ========================================================================

    /**
     * 모임 가입
     *
     * [처리 흐름]
     * 1. 모임과 회원 존재 확인
     * 2. 중복 가입 확인 (이미 멤버인 경우)
     * 3. 정원 확인 (가득 찼으면 에러)
     * 4. GroupMember 생성 (즉시 APPROVED)
     */
    @Override
    public void join(Long groupId, String email) {
        Group group = groupRepository.findByIdWithDetails(groupId)
                .orElseThrow(() -> CustomException.notFound("모임을 찾을 수 없습니다."));
        Member member = getMemberByEmail(email);

        // 1. 중복 가입 확인
        if (groupMemberRepository.existsByGroupIdAndMemberId(groupId, member.getId())) {
            throw new CustomException("이미 멤버입니다.", HttpStatus.BAD_REQUEST);
        }

        // 2. 정원 확인
        int currentCount = groupMemberRepository.countApprovedMembers(groupId);
        if (currentCount >= group.getMaxMembers()) {
            throw new CustomException("모임 정원이 가득 찼습니다.", HttpStatus.BAD_REQUEST);
        }

        // 3. GroupMember 생성 (즉시 가입)
        GroupMember groupMember = GroupMember.builder()
                .group(group)
                .member(member)
                .role(GroupRole.MEMBER)  // 기본 역할: 일반 멤버
                .status(JoinStatus.APPROVED)  // 즉시 승인
                .build();

        groupMemberRepository.save(groupMember);
        log.info("Member {} joined group {}", email, group.getName());

        notificationService.send(
                group.getOwner(),
                NotificationType.NEW_MEMBER,
                "새 맴버 가입",
                member.getNickname() + "님이 '" + group.getName() + "'에 가입했습니다.",  // 내용
                groupId
        );


    }

    /**
     * 모임 탈퇴
     *
     * [제한]
     * 모임장은 탈퇴할 수 없습니다.
     * 모임장이 탈퇴하려면 먼저 다른 멤버에게 모임장을 위임해야 합니다.
     */
    @Override
    public void leave(Long groupId, String email) {
        Member member = getMemberByEmail(email);

        Group group = groupRepository.findByIdWithDetails(groupId)
                .orElseThrow(() -> CustomException.notFound("모임을 찾을 수 없습니다."));

        GroupMember groupMember = groupMemberRepository.findByGroupIdAndMemberId(groupId, member.getId())
                .orElseThrow(() -> CustomException.notFound("가입 정보를 찾을 수 없습니다."));

        // 모임장은 탈퇴 불가
        if (groupMember.getRole() == GroupRole.OWNER) {
            throw new CustomException("모임장은 탈퇴할 수 없습니다. 모임장을 위임하세요.", HttpStatus.BAD_REQUEST);
        }

        // DB에서 삭제
        groupMemberRepository.delete(groupMember);
        log.info("Member {} left group {}", email, groupId);

        notificationService.send(
            group.getOwner(),
            NotificationType.MEMBER_LEFT,
            "맴버가 탈퇴하였습니다.",
            member.getNickname() + "님이 '" + group.getName() + "'에서 탈퇴했습니다.",
            groupId
        );

    }

    /**
     * 멤버 강퇴
     *
     * [권한] 모임장 또는 운영진
     * [제한] 모임장은 강퇴 불가
     */
    @Override
    public void kickMember(Long groupId, Long memberId, String ownerEmail) {
        Group group = groupRepository.findByIdWithDetails(groupId)
                .orElseThrow(() -> CustomException.notFound("모임을 찾을 수 없습니다."));

        checkOwnerOrManager(group, ownerEmail);

        GroupMember groupMember = groupMemberRepository.findByGroupIdAndMemberId(groupId, memberId)
                .orElseThrow(() -> CustomException.notFound("멤버를 찾을 수 없습니다."));

        // 모임장은 강퇴 불가
        if (groupMember.getRole() == GroupRole.OWNER) {
            throw new CustomException("모임장은 강퇴할 수 없습니다.", HttpStatus.BAD_REQUEST);
        }

        // 강퇴될 멤버 정보 저장 (삭제 전에 참조)
        Member kickedMember = groupMember.getMember();

        groupMemberRepository.delete(groupMember);
        log.info("Member {} kicked from group {}", memberId, groupId);

        // 강퇴된 멤버에게 알림 발송
        notificationService.send(
                kickedMember,
                NotificationType.MEMBER_KICKED,
                "모임에서 탈퇴 처리되었습니다",
                "'" + group.getName() + "'에서 탈퇴 처리되었습니다.",
                groupId
        );
    }

    /**
     * 멤버 역할 변경
     *
     * [권한] 모임장만 가능
     * [용도] 일반 멤버 → 운영진 승격 등
     */
    @Override
    public void changeRole(Long groupId, Long memberId, String role, String ownerEmail) {
        Group group = groupRepository.findByIdWithDetails(groupId)
                .orElseThrow(() -> CustomException.notFound("모임을 찾을 수 없습니다."));

        // 모임장만 역할 변경 가능
        checkOwner(group, ownerEmail);

        GroupMember groupMember = groupMemberRepository.findByGroupIdAndMemberId(groupId, memberId)
                .orElseThrow(() -> CustomException.notFound("멤버를 찾을 수 없습니다."));

        // 문자열 → Enum 변환
        GroupRole newRole = GroupRole.valueOf(role);

        notificationService.send(
                groupMember.getMember(),
                NotificationType.ROLE_CHANGED,
                "권한이 변경되었습니다.",
                "'"+group.getName() + "'모임에서 권한이 "+groupMember.getRole()+"에서 "+newRole+"로 변경되었습니다.",
                groupId
        );

        groupMember.changeRole(newRole);
        log.info("Member {} role changed to {} in group {}", memberId, role, groupId);

    }

    /**
     * 멤버 목록 조회
     *
     * @param status null이면 APPROVED (기본값)
     */
    @Override
    @Transactional(readOnly = true)
    public List<GroupMemberDTO> getMembers(Long groupId, String status) {
        JoinStatus joinStatus = status != null ? JoinStatus.valueOf(status) : JoinStatus.APPROVED;
        return groupMemberRepository.findByGroupIdAndStatus(groupId, joinStatus).stream()
                .map(this::memberToDTO)
                .collect(Collectors.toList());
    }

    // ========================================================================
    // 헬퍼 메서드 (private)
    // ========================================================================

    /**
     * 이메일로 회원 조회
     *
     * 여러 메서드에서 반복적으로 사용되는 로직을 추출했습니다.
     */
    private Member getMemberByEmail(String email) {
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> CustomException.notFound("회원을 찾을 수 없습니다."));
    }

    /**
     * 모임장 권한 검사
     *
     * 요청자가 해당 모임의 모임장인지 확인합니다.
     * 모임장이 아니면 403 Forbidden 에러를 발생시킵니다.
     */
    private void checkOwner(Group group, String email) {
        if (!group.getOwner().getEmail().equals(email)) {
            throw CustomException.forbidden("모임장만 수행할 수 있는 작업입니다.");
        }
    }

    /**
     * 모임장 또는 운영진 권한 검사
     *
     * 요청자가 모임장이거나 운영진(MANAGER)인지 확인합니다.
     * 둘 다 아니면 403 Forbidden 에러를 발생시킵니다.
     */
    private void checkOwnerOrManager(Group group, String email) {
        Member member = getMemberByEmail(email);

        // 모임장인 경우 바로 통과
        if (group.getOwner().getEmail().equals(email)) {
            return;
        }

        // 모임장이 아니면 GroupMember에서 역할 확인
        GroupMember gm = groupMemberRepository.findByGroupIdAndMemberId(group.getId(), member.getId())
                .orElseThrow(() -> CustomException.forbidden("권한이 없습니다."));

        // MANAGER도 아니면 권한 없음
        if (gm.getRole() != GroupRole.MANAGER && gm.getRole() != GroupRole.OWNER) {
            throw CustomException.forbidden("권한이 없습니다.");
        }
    }

    /**
     * Group Entity → GroupDTO 변환
     *
     * [변환 내용]
     * 1. 기본 정보 복사
     * 2. 카테고리 정보를 중첩 객체로 반환 (CategoryDTO)
     * 3. 모임장 정보를 중첩 객체로 반환 (MemberSummaryDTO)
     * 4. 위치 정보를 중첩 객체로 반환 (LocationDTO)
     * 5. 멤버 수 계산 (countApprovedMembers)
     * 6. 현재 사용자의 관계 설정 (email이 주어진 경우)
     *
     * @param group 변환할 엔티티
     * @param email 현재 사용자 이메일 (null 가능)
     */
    private GroupDTO entityToDTO(Group group, String email) {
        // 승인된 멤버 수 조회
        int memberCount = groupMemberRepository.countApprovedMembers(group.getId());

        // Builder 패턴으로 DTO 생성
        GroupDTO.GroupDTOBuilder builder = GroupDTO.builder()
                .id(group.getId())
                .name(group.getName())
                .description(group.getDescription())
                .coverImage(group.getCoverImage())
                .thumbnailImage(group.getCoverImage())  // 썸네일은 커버 이미지 사용
                .address(group.getLocation())
                .maxMembers(group.getMaxMembers())
                .memberCount(memberCount)
                .isPublic(group.isPublic())
                .status(group.getStatus().name())
                .createdAt(group.getCreatedAt())
                .updatedAt(group.getModifiedAt());

        // 위치 정보를 중첩 객체로 변환
        if (group.getLatitude() != null && group.getLongitude() != null) {
            builder.location(GroupDTO.LocationDTO.builder()
                    .lat(group.getLatitude())
                    .lng(group.getLongitude())
                    .build());
        }

        // 카테고리 정보를 중첩 객체로 변환
        if (group.getCategory() != null) {
            builder.category(CategoryDTO.from(group.getCategory()));
        }

        // 모임장 정보를 중첩 객체로 변환
        if (group.getOwner() != null) {
            builder.owner(MemberSummaryDTO.from(group.getOwner()));
        }

        // 현재 사용자와 모임의 관계 설정
        // 프론트엔드에서 "가입하기", "탈퇴" 등 버튼 표시에 사용
        if (email != null) {
            Member member = memberRepository.findByEmail(email).orElse(null);
            if (member != null) {
                groupMemberRepository.findByGroupIdAndMemberId(group.getId(), member.getId())
                        .ifPresent(gm -> {
                            builder.myRole(gm.getRole().name());    // OWNER, MANAGER, MEMBER
                            builder.myStatus(gm.getStatus().name()); // APPROVED, BANNED
                        });
            }
        }

        return builder.build();
    }

    /**
     * GroupMember Entity → GroupMemberDTO 변환
     *
     * 멤버 목록 조회에서 사용됩니다.
     */
    private GroupMemberDTO memberToDTO(GroupMember gm) {
        boolean isNew = gm.getCreatedAt() != null &&
                gm.getCreatedAt().isAfter(LocalDateTime.now().minusDays(7));
        return GroupMemberDTO.builder()
                .id(gm.getId())
                .member(MemberSummaryDTO.from(gm.getMember()))
                .role(gm.getRole().name())
                .joinedAt(gm.getCreatedAt())  // BaseEntity의 createdAt
                .isNewMember(isNew)
                .build();
    }
}
