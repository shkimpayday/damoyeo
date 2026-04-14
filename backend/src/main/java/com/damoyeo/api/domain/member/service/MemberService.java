package com.damoyeo.api.domain.member.service;

import com.damoyeo.api.domain.member.dto.MemberDTO;
import com.damoyeo.api.domain.member.dto.MemberModifyRequest;
import com.damoyeo.api.domain.member.dto.MemberSignupRequest;
import com.damoyeo.api.domain.member.dto.PublicProfileDTO;

/**
 * 회원 서비스 인터페이스
 *
 * 회원 관련 비즈니스 로직의 계약(Contract)을 정의합니다.
 *
 * [왜 인터페이스를 사용하는가?]
 * 1. 구현체 교체 용이: 테스트 시 Mock 구현체 사용 가능
 * 2. 의존성 역전: Controller는 인터페이스에만 의존
 * 3. 명세 역할: 제공해야 할 기능을 명확히 정의
 *
 * [구현체]
 * MemberServiceImpl에서 실제 로직을 구현합니다.
 */
public interface MemberService {

    /**
     * 회원가입
     *
     * 새로운 회원을 등록합니다.
     * 이메일/닉네임 중복 확인 후 비밀번호를 암호화하여 저장합니다.
     *
     * @param request 회원가입 요청 데이터 (email, password, nickname)
     * @return 생성된 회원 정보
     * @throws CustomException 이메일/닉네임 중복 시
     */
    MemberDTO signup(MemberSignupRequest request);

    /**
     * 이메일로 회원 조회
     *
     * @param email 이메일
     * @return 회원 정보
     * @throws CustomException 회원이 없을 때 (404)
     */
    MemberDTO getByEmail(String email);

    /**
     * 회원 정보 수정
     *
     * null이 아닌 필드만 수정됩니다.
     *
     * @param email 수정할 회원의 이메일
     * @param request 수정할 데이터
     * @return 수정된 회원 정보
     */
    MemberDTO modify(String email, MemberModifyRequest request);

    /**
     * 이메일 존재 여부 확인
     *
     * 회원가입 전 이메일 중복 확인에 사용합니다.
     *
     * @param email 확인할 이메일
     * @return 존재하면 true
     */
    boolean existsByEmail(String email);

    /**
     * 닉네임 존재 여부 확인
     *
     * 회원가입/닉네임 변경 전 중복 확인에 사용합니다.
     *
     * @param nickname 확인할 닉네임
     * @return 존재하면 true
     */
    boolean existsByNickname(String nickname);

    /**
     * 프로필 이미지 업로드
     *
     * 이미지 파일을 업로드하고 회원의 프로필 이미지를 업데이트합니다.
     * 기존 이미지가 있으면 삭제합니다.
     *
     * @param email 회원 이메일
     * @param file 업로드할 이미지 파일
     * @return 저장된 이미지 URL
     */
    String uploadProfileImage(String email, org.springframework.web.multipart.MultipartFile file);

    /**
     * 위치 정보 업데이트
     *
     * 회원의 위치 정보를 업데이트합니다.
     *
     * @param email 회원 이메일
     * @param lat 위도
     * @param lng 경도
     * @param address 주소
     */
    void updateLocation(String email, Double lat, Double lng, String address);

    /**
     * 공개 프로필 조회
     *
     * 다른 회원이 볼 수 있는 공개 프로필 정보를 조회합니다.
     * 이메일, 비밀번호 등 민감한 정보는 포함되지 않습니다.
     *
     * @param memberId 조회할 회원 ID
     * @return 공개 프로필 정보
     */
    PublicProfileDTO getPublicProfile(Long memberId);

    /**
     * 카카오 OAuth 로그인
     *
     * <p>카카오 인가 코드를 이용한 소셜 로그인 처리:</p>
     * <ol>
     *   <li>카카오 인가 코드 → 카카오 액세스 토큰 교환</li>
     *   <li>카카오 액세스 토큰으로 사용자 정보 조회</li>
     *   <li>이메일 기반으로 기존 회원 조회 또는 신규 회원 생성</li>
     *   <li>회원 정보 반환 (JWT 토큰은 Controller에서 발급)</li>
     * </ol>
     *
     * @param code        카카오 인가 코드 (프론트엔드에서 전달)
     * @param redirectUri 카카오 로그인 시 사용한 Redirect URI
     * @return 로그인된 회원 정보 DTO
     * @throws com.damoyeo.api.global.exception.CustomException 카카오 API 호출 실패 시
     */
    MemberDTO kakaoLogin(String code, String redirectUri);
}
