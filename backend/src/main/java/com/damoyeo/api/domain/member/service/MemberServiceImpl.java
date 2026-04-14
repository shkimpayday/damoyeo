package com.damoyeo.api.domain.member.service;

import com.damoyeo.api.domain.group.entity.GroupMember;
import com.damoyeo.api.domain.group.entity.GroupStatus;
import com.damoyeo.api.domain.group.entity.JoinStatus;
import com.damoyeo.api.domain.group.repository.GroupMemberRepository;
import com.damoyeo.api.domain.member.dto.MemberDTO;
import com.damoyeo.api.domain.member.dto.MemberModifyRequest;
import com.damoyeo.api.domain.member.dto.MemberSignupRequest;
import com.damoyeo.api.domain.member.dto.PublicProfileDTO;
import com.damoyeo.api.domain.member.entity.Member;
import com.damoyeo.api.domain.member.entity.MemberRole;
import com.damoyeo.api.domain.member.repository.MemberRepository;
import com.damoyeo.api.domain.notification.entity.NotificationType;
import com.damoyeo.api.domain.notification.service.NotificationService;
import com.damoyeo.api.global.exception.CustomException;
import com.damoyeo.api.global.util.FileUploadUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 회원 서비스 구현체
 *
 * MemberService 인터페이스의 실제 구현입니다.
 * 회원가입, 프로필 조회/수정 등 회원 관련 비즈니스 로직을 처리합니다.
 *
 * ▶ @Service: Spring 서비스 Bean으로 등록
 * ▶ @Transactional: 모든 메서드에 트랜잭션 적용
 * ▶ @RequiredArgsConstructor: final 필드 생성자 주입
 */
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class MemberServiceImpl implements MemberService {

    /** 회원 DB 접근 */
    private final MemberRepository memberRepository;

    /** 모임 멤버 DB 접근 */
    private final GroupMemberRepository groupMemberRepository;

    /** 비밀번호 암호화 (BCrypt) */
    private final PasswordEncoder passwordEncoder;

    /** 파일 업로드 유틸리티 */
    private final FileUploadUtil fileUploadUtil;

    /** 알림 서비스 */
    private final NotificationService notificationService;

    /** 외부 API 호출 (카카오 OAuth) */
    private final RestTemplate restTemplate;

    /** 카카오 OAuth REST API 키 */
    @Value("${kakao.oauth.client-id:}")
    private String kakaoClientId;

    /** 카카오 OAuth Client Secret (미설정 시 빈 문자열) */
    @Value("${kakao.oauth.client-secret:}")
    private String kakaoClientSecret;

    /**
     * 회원가입
     *
     * [처리 흐름]
     * 1. 이메일/닉네임 중복 확인
     * 2. 비밀번호 BCrypt 암호화
     * 3. Member 엔티티 생성 (USER 권한 부여)
     * 4. DB 저장
     *
     * @param request 회원가입 요청 데이터
     * @return 생성된 회원 정보 (비밀번호 제외)
     */
    @Override
    public MemberDTO signup(MemberSignupRequest request) {
        // ========== 1. 중복 체크 ==========
        if (memberRepository.existsByEmail(request.getEmail())) {
            throw new CustomException("이미 사용 중인 이메일입니다.", HttpStatus.BAD_REQUEST);
        }
        if (memberRepository.existsByNickname(request.getNickname())) {
            throw new CustomException("이미 사용 중인 닉네임입니다.", HttpStatus.BAD_REQUEST);
        }

        // ========== 2. Member 엔티티 생성 ==========
        Member member = Member.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))  // BCrypt 암호화
                .nickname(request.getNickname())
                .social(false)  // 일반 회원가입 (소셜 X)
                .build();

        // ========== 3. 기본 권한 부여 ==========
        member.addRole(MemberRole.USER);

        // ========== 4. DB 저장 ==========
        Member saved = memberRepository.save(member);
        log.info("New member registered: {}", saved.getEmail());

        // ========== 5. 환영 알림 발송 ==========
        notificationService.send(
                saved,
                NotificationType.WELCOME,
                "다모여에 오신 것을 환영합니다! 🎉",
                "첫 가입 기념으로 프리미엄 30일 무료 쿠폰을 드립니다. 다양한 모임에서 새로운 인연을 만나보세요!",
                null  // 관련 리소스 없음
        );

        return entityToDTO(saved);
    }

    /**
     * 이메일로 회원 조회
     *
     * @Transactional(readOnly = true): 읽기 전용 (성능 최적화)
     */
    @Override
    @Transactional(readOnly = true)
    public MemberDTO getByEmail(String email) {
        Member member = memberRepository.getWithRoles(email)
                .orElseThrow(() -> CustomException.notFound("회원을 찾을 수 없습니다."));
        return entityToDTO(member);
    }

    /**
     * 회원 정보 수정
     *
     * [특징]
     * - null이 아닌 필드만 수정
     * - JPA 더티 체킹: 트랜잭션 커밋 시 변경 사항 자동 반영
     *
     * @param email 수정할 회원의 이메일
     * @param request 수정할 데이터 (null인 필드는 수정 안 함)
     * @return 수정된 회원 정보
     */
    @Override
    public MemberDTO modify(String email, MemberModifyRequest request) {
        Member member = memberRepository.getWithRoles(email)
                .orElseThrow(() -> CustomException.notFound("회원을 찾을 수 없습니다."));

        // 닉네임 변경 (중복 확인)
        if (request.getNickname() != null && !request.getNickname().equals(member.getNickname())) {
            if (memberRepository.existsByNickname(request.getNickname())) {
                throw new CustomException("이미 사용 중인 닉네임입니다.", HttpStatus.BAD_REQUEST);
            }
            member.changeNickname(request.getNickname());
        }

        // 비밀번호 변경 (암호화)
        if (request.getPassword() != null) {
            member.changePassword(passwordEncoder.encode(request.getPassword()));
        }

        // 프로필 이미지 변경
        if (request.getProfileImage() != null) {
            member.changeProfileImage(request.getProfileImage());
        }

        // 자기소개 변경
        if (request.getIntroduction() != null) {
            member.changeIntroduction(request.getIntroduction());
        }

        // 활동 모임 공개 여부 변경 (프리미엄 회원만 가능)
        if (request.getShowJoinedGroups() != null) {
            boolean isPremium = member.getMemberRoleList().contains(MemberRole.PREMIUM);
            if (isPremium) {
                member.changeShowJoinedGroups(request.getShowJoinedGroups());
            }
            // 프리미엄이 아닌 경우 무시 (에러 없이)
        }

        // JPA 더티 체킹으로 자동 UPDATE (save 호출 불필요)
        return entityToDTO(member);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return memberRepository.existsByEmail(email);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByNickname(String nickname) {
        return memberRepository.existsByNickname(nickname);
    }

    /**
     * 프로필 이미지 업로드
     *
     * [처리 흐름]
     * 1. 회원 조회
     * 2. 기존 이미지 삭제 (있는 경우)
     * 3. 새 이미지 업로드
     * 4. 회원 프로필 이미지 URL 업데이트
     *
     * @param email 회원 이메일
     * @param file 업로드할 이미지 파일
     * @return 저장된 이미지 URL
     */
    @Override
    public String uploadProfileImage(String email, MultipartFile file) {
        Member member = memberRepository.getWithRoles(email)
                .orElseThrow(() -> CustomException.notFound("회원을 찾을 수 없습니다."));

        // 기존 이미지 삭제
        String oldImageUrl = member.getProfileImage();
        if (oldImageUrl != null && !oldImageUrl.isEmpty()) {
            fileUploadUtil.deleteFile(oldImageUrl);
        }

        // 새 이미지 업로드
        String imageUrl = fileUploadUtil.uploadProfileImage(file);

        // 프로필 이미지 URL 업데이트
        member.changeProfileImage(imageUrl);

        log.info("Profile image updated for member: {}", email);
        return imageUrl;
    }

    /**
     * 위치 정보 업데이트
     *
     * @param email 회원 이메일
     * @param lat 위도
     * @param lng 경도
     * @param address 주소
     */
    @Override
    public void updateLocation(String email, Double lat, Double lng, String address) {
        Member member = memberRepository.getWithRoles(email)
                .orElseThrow(() -> CustomException.notFound("회원을 찾을 수 없습니다."));

        member.changeLocation(lat, lng, address);
        log.info("Location updated for member: {}", email);
    }

    /**
     * 공개 프로필 조회
     *
     * [반환 정보]
     * - 닉네임, 프로필 이미지, 자기소개, 지역
     * - 가입일, 가입한 모임 수
     * - 가입한 공개 모임 목록
     */
    @Override
    @Transactional(readOnly = true)
    public PublicProfileDTO getPublicProfile(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> CustomException.notFound("회원을 찾을 수 없습니다."));

        List<PublicProfileDTO.JoinedGroupDTO> joinedGroups;

        // 회원이 활동 모임 공개를 설정한 경우에만 조회
        if (member.isShowJoinedGroups()) {
            List<GroupMember> groupMembers = groupMemberRepository.findByMemberIdAndStatus(
                    memberId, JoinStatus.APPROVED);

            joinedGroups = groupMembers.stream()
                    // 공개 모임 + 활성 상태 모임만 표시 (INACTIVE, DELETED 제외)
                    .filter(gm -> gm.getGroup().isPublic()
                            && gm.getGroup().getStatus() == GroupStatus.ACTIVE)
                    .map(gm -> PublicProfileDTO.JoinedGroupDTO.builder()
                            .id(gm.getGroup().getId())
                            .name(gm.getGroup().getName())
                            .thumbnailImage(gm.getGroup().getCoverImage())
                            .categoryName(gm.getGroup().getCategory() != null
                                    ? gm.getGroup().getCategory().getName()
                                    : "미분류")
                            .build())
                    .collect(Collectors.toList());
        } else {
            // 비공개 설정 시 빈 목록 반환
            joinedGroups = List.of();
        }

        return PublicProfileDTO.builder()
                .id(member.getId())
                .nickname(member.getNickname())
                .profileImage(member.getProfileImage())
                .introduction(member.getIntroduction())
                .address(member.getAddress())
                .createdAt(member.getCreatedAt())
                .groupCount(joinedGroups.size())
                .joinedGroups(joinedGroups)
                .showJoinedGroups(member.isShowJoinedGroups())
                .build();
    }

    /**
     * 카카오 OAuth 로그인
     *
     * <p>처리 흐름:</p>
     * <ol>
     *   <li>카카오 인가 코드 → 카카오 액세스 토큰 교환</li>
     *   <li>카카오 사용자 정보 조회 (이메일, 닉네임, 프로필 이미지)</li>
     *   <li>이메일로 기존 회원 조회, 없으면 자동 가입</li>
     * </ol>
     *
     * @param code        카카오 인가 코드
     * @param redirectUri 카카오 로그인 시 사용한 Redirect URI
     * @return 회원 DTO
     */
    @Override
    public MemberDTO kakaoLogin(String code, String redirectUri) {
        // ========== 1. 카카오 액세스 토큰 발급 ==========
        String kakaoAccessToken = getKakaoAccessToken(code, redirectUri);

        // ========== 2. 카카오 사용자 정보 조회 ==========
        Map<String, Object> userInfo = getKakaoUserInfo(kakaoAccessToken);

        // ========== 3. 회원 조회 또는 신규 가입 ==========
        Member member = findOrCreateKakaoMember(userInfo);

        return entityToDTO(member);
    }

    /**
     * 카카오 인가 코드를 액세스 토큰으로 교환
     *
     * <p>POST https://kauth.kakao.com/oauth/token</p>
     *
     * @param code        카카오 인가 코드
     * @param redirectUri 프론트엔드에서 사용한 Redirect URI (카카오에 등록된 URI와 일치해야 함)
     * @return 카카오 액세스 토큰
     * @throws CustomException 토큰 발급 실패 시
     */
    @SuppressWarnings("unchecked")
    private String getKakaoAccessToken(String code, String redirectUri) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", kakaoClientId);
        params.add("redirect_uri", redirectUri);
        params.add("code", code);

        // Client Secret이 설정된 경우에만 포함
        if (kakaoClientSecret != null && !kakaoClientSecret.isBlank()) {
            params.add("client_secret", kakaoClientSecret);
        }

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    "https://kauth.kakao.com/oauth/token",
                    request,
                    Map.class
            );

            Map<String, Object> body = response.getBody();
            if (body == null || !body.containsKey("access_token")) {
                throw new CustomException("카카오 액세스 토큰 발급 실패", HttpStatus.BAD_GATEWAY);
            }

            return (String) body.get("access_token");

        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.error("카카오 토큰 발급 실패: {}", e.getMessage());
            throw new CustomException("카카오 로그인 처리 중 오류가 발생했습니다.", HttpStatus.BAD_GATEWAY);
        }
    }

    /**
     * 카카오 액세스 토큰으로 사용자 정보 조회
     *
     * <p>GET https://kapi.kakao.com/v2/user/me</p>
     *
     * @param kakaoAccessToken 카카오 액세스 토큰
     * @return 카카오 사용자 정보 (id, kakao_account.email, kakao_account.profile 등)
     * @throws CustomException 사용자 정보 조회 실패 시
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> getKakaoUserInfo(String kakaoAccessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + kakaoAccessToken);

        HttpEntity<Void> request = new HttpEntity<>(headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    "https://kapi.kakao.com/v2/user/me",
                    HttpMethod.GET,
                    request,
                    Map.class
            );

            Map<String, Object> body = response.getBody();
            if (body == null) {
                throw new CustomException("카카오 사용자 정보 조회 실패", HttpStatus.BAD_GATEWAY);
            }

            return body;

        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.error("카카오 사용자 정보 조회 실패: {}", e.getMessage());
            throw new CustomException("카카오 사용자 정보를 가져올 수 없습니다.", HttpStatus.BAD_GATEWAY);
        }
    }

    /**
     * 카카오 사용자 정보로 기존 회원 조회 또는 신규 회원 생성
     *
     * <p>신규 가입 처리:</p>
     * <ul>
     *   <li>이메일이 없는 경우 kakao_{id}@kakao.damoyeo.com 형태로 생성</li>
     *   <li>닉네임 중복 시 숫자 접미사 추가</li>
     *   <li>비밀번호는 랜덤 UUID로 설정 (소셜 로그인이므로 사용 불가)</li>
     *   <li>신규 가입 시 환영 알림 발송</li>
     * </ul>
     *
     * @param userInfo 카카오 사용자 정보 Map
     * @return 조회 또는 생성된 Member 엔티티
     */
    @SuppressWarnings("unchecked")
    private Member findOrCreateKakaoMember(Map<String, Object> userInfo) {
        Long kakaoId = ((Number) userInfo.get("id")).longValue();

        // kakao_account에서 이메일, 닉네임, 프로필 이미지 추출
        Map<String, Object> kakaoAccount = (Map<String, Object>) userInfo.get("kakao_account");
        String email = null;
        String nickname = "카카오사용자";
        String profileImage = null;

        if (kakaoAccount != null) {
            // 이메일 (카카오 동의항목에서 이메일 제공 동의한 경우에만 존재)
            email = (String) kakaoAccount.get("email");

            Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");
            if (profile != null) {
                String kakaoNickname = (String) profile.get("nickname");
                if (kakaoNickname != null && !kakaoNickname.isBlank()) {
                    nickname = kakaoNickname;
                }
                profileImage = (String) profile.get("profile_image_url");
            }
        }

        // 이메일이 없으면 kakao id 기반으로 생성
        if (email == null || email.isBlank()) {
            email = "kakao_" + kakaoId + "@kakao.damoyeo.com";
        }

        // 기존 회원이면 반환
        Optional<Member> existing = memberRepository.getWithRoles(email);
        if (existing.isPresent()) {
            log.info("카카오 기존 회원 로그인: {}", email);
            return existing.get();
        }

        // 닉네임 중복 처리
        String finalNickname = nickname;
        if (memberRepository.existsByNickname(finalNickname)) {
            finalNickname = finalNickname + "_" + (kakaoId % 10000);
        }

        // 신규 회원 생성
        Member newMember = Member.builder()
                .email(email)
                .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                .nickname(finalNickname)
                .profileImage(profileImage)
                .social(true)
                .build();
        newMember.addRole(MemberRole.USER);

        Member saved = memberRepository.save(newMember);
        log.info("카카오 신규 회원 가입: {}", email);

        // 환영 알림 발송
        notificationService.send(
                saved,
                NotificationType.WELCOME,
                "다모여에 오신 것을 환영합니다! 🎉",
                "카카오 계정으로 가입하셨습니다. 다양한 모임을 만나보세요!",
                null
        );

        return saved;
    }

    /**
     * Entity → DTO 변환 헬퍼 메서드
     *
     * [주의] password는 포함하지 않음 (보안)
     */
    private MemberDTO entityToDTO(Member member) {
        return MemberDTO.builder()
                .id(member.getId())
                .email(member.getEmail())
                .nickname(member.getNickname())
                .profileImage(member.getProfileImage())
                .introduction(member.getIntroduction())
                .social(member.isSocial())
                .roleNames(member.getMemberRoleList().stream()
                        .map(Enum::name)  // MemberRole.USER → "USER"
                        .collect(Collectors.toList()))
                .showJoinedGroups(member.isShowJoinedGroups())
                .build();
    }
}
