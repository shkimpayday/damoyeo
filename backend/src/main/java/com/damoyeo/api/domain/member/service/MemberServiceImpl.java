package com.damoyeo.api.domain.member.service;

import com.damoyeo.api.domain.member.dto.MemberDTO;
import com.damoyeo.api.domain.member.dto.MemberModifyRequest;
import com.damoyeo.api.domain.member.dto.MemberSignupRequest;
import com.damoyeo.api.domain.member.entity.Member;
import com.damoyeo.api.domain.member.entity.MemberRole;
import com.damoyeo.api.domain.member.repository.MemberRepository;
import com.damoyeo.api.domain.notification.entity.NotificationType;
import com.damoyeo.api.domain.notification.service.NotificationService;
import com.damoyeo.api.global.exception.CustomException;
import com.damoyeo.api.global.util.FileUploadUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.stream.Collectors;

/**
 * ============================================================================
 * 회원 서비스 구현체
 * ============================================================================
 *
 * [역할]
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

    /** 비밀번호 암호화 (BCrypt) */
    private final PasswordEncoder passwordEncoder;

    /** 파일 업로드 유틸리티 */
    private final FileUploadUtil fileUploadUtil;

    /** 알림 서비스 */
    private final NotificationService notificationService;

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
                .build();
    }
}
