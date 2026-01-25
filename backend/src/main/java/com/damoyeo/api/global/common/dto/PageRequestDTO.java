package com.damoyeo.api.global.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/**
 * ============================================================================
 * 페이지네이션 요청 DTO (클라이언트 → 서버)
 * ============================================================================
 *
 * [사용 목적]
 * 프론트엔드에서 "몇 페이지, 한 페이지에 몇 개" 정보를 받아오는 객체입니다.
 * 모임 목록, 정모 목록 등 리스트 조회 API에서 사용됩니다.
 *
 * [프론트엔드 요청 예시]
 * GET /api/groups?page=1&size=10
 * → page=1 (첫 번째 페이지), size=10 (한 페이지에 10개)
 *
 * [Controller에서 사용 예시]
 * @GetMapping("/api/groups")
 * public PageResponseDTO<GroupDTO> list(PageRequestDTO pageRequestDTO) {
 *     // pageRequestDTO.getPage() → 1
 *     // pageRequestDTO.getSize() → 10
 * }
 *
 * ▶ Lombok 어노테이션 설명
 *   @Data: getter, setter, toString, equals, hashCode 자동 생성
 *   @Builder: 빌더 패턴 사용 가능 (PageRequestDTO.builder().page(1).build())
 *   @AllArgsConstructor: 모든 필드를 받는 생성자
 *   @NoArgsConstructor: 기본 생성자 (Spring이 객체 생성할 때 필요)
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PageRequestDTO {

    /**
     * 요청 페이지 번호 (1부터 시작)
     *
     * 프론트엔드는 보통 1페이지부터 시작하지만,
     * Spring Data JPA는 0페이지부터 시작합니다.
     * 그래서 getPageable()에서 page - 1을 해줍니다.
     *
     * @Builder.Default: 빌더 패턴 사용 시 기본값 1 적용
     */
    @Builder.Default
    private int page = 1;

    /**
     * 한 페이지에 보여줄 데이터 개수
     *
     * 기본값 10개 (한 페이지에 10개씩 보여줌)
     */
    @Builder.Default
    private int size = 10;

    /**
     * Spring Data JPA에서 사용하는 Pageable 객체로 변환
     *
     * [왜 필요한가?]
     * Repository에서 페이지네이션 쿼리를 실행하려면 Pageable 객체가 필요합니다.
     * 이 메서드가 PageRequestDTO를 Pageable로 변환해줍니다.
     *
     * [사용 예시]
     * // Service에서
     * Page<Group> result = groupRepository.findAll(pageRequestDTO.getPageable("id"));
     *
     * @param props 정렬 기준 컬럼명 (예: "id", "createdAt")
     * @return Pageable 객체
     *
     * [내부 동작 설명]
     * - this.page - 1: 프론트(1부터) → JPA(0부터) 변환
     * - Sort.by(props).descending(): 지정된 컬럼 기준 내림차순 정렬
     */
    public Pageable getPageable(String... props) {
        return PageRequest.of(this.page - 1, this.size, Sort.by(props).descending());
    }
}
