package com.damoyeo.api.global.common.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * ============================================================================
 * 페이지네이션 응답 DTO (서버 → 클라이언트)
 * ============================================================================
 *
 * [사용 목적]
 * 페이지네이션된 데이터와 페이지 정보를 프론트엔드에 전달하는 객체입니다.
 * 모임 목록, 정모 목록 등 리스트 조회 API의 응답으로 사용됩니다.
 *
 * [프론트엔드로 전달되는 JSON 예시]
 * {
 *   "dtoList": [...],      // 실제 데이터 목록
 *   "pageNumList": [1,2,3,4,5,6,7,8,9,10],  // 화면에 표시할 페이지 번호들
 *   "prev": false,         // 이전 페이지 그룹 존재 여부
 *   "next": true,          // 다음 페이지 그룹 존재 여부
 *   "totalCount": 150,     // 전체 데이터 개수
 *   "current": 1           // 현재 페이지 번호
 * }
 *
 * [Service에서 사용 예시]
 * return PageResponseDTO.<GroupDTO>builder()
 *     .pageRequestDTO(pageRequestDTO)
 *     .dtoList(groupDTOList)
 *     .totalCount(100)
 *     .build();
 *
 * @param <E> 리스트에 담길 DTO 타입 (GroupDTO, MeetingDTO 등)
 */
@Data
public class PageResponseDTO<E> {

    /**
     * 실제 데이터 목록
     * 예: 10개의 GroupDTO가 담긴 리스트
     */
    private List<E> dtoList;

    /**
     * 화면에 표시할 페이지 번호 목록
     * 예: [1, 2, 3, 4, 5, 6, 7, 8, 9, 10]
     *
     * 한 번에 10개씩 페이지 번호를 보여줍니다.
     * 1~10페이지 그룹, 11~20페이지 그룹 등...
     */
    private List<Integer> pageNumList;

    /**
     * 요청 정보 (어떤 페이지를 요청했는지)
     */
    private PageRequestDTO pageRequestDTO;

    /**
     * 이전 페이지 그룹 존재 여부
     * 예: 현재 11~20페이지 그룹이면 prev=true (1~10페이지가 있으므로)
     */
    private boolean prev;

    /**
     * 다음 페이지 그룹 존재 여부
     * 예: 전체 25페이지인데 현재 1~10 그룹이면 next=true
     */
    private boolean next;

    /**
     * 전체 데이터 개수
     */
    private int totalCount;

    /**
     * 전체 페이지 수
     */
    private int totalPage;

    /**
     * 현재 페이지 번호
     */
    private int current;

    /**
     * PageResponseDTO 생성자 (빌더 패턴으로 호출)
     *
     * PageResponseDTO.builder().pageRequestDTO(...).dtoList(...).totalCount(...).build()
     *
     * [페이지 번호 계산 로직 설명]
     *
     * 예시: 전체 95개 데이터, 한 페이지 10개, 현재 3페이지 요청
     *
     * 1. end 계산: Math.ceil(3 / 10.0) * 10 = 10
     *    → 현재 페이지가 속한 그룹의 마지막 번호 (1~10 그룹이므로 10)
     *
     * 2. start 계산: 10 - 9 = 1
     *    → 현재 그룹의 첫 번째 페이지 번호
     *
     * 3. last 계산: Math.ceil(95 / 10.0) = 10
     *    → 실제로 존재하는 마지막 페이지 번호
     *
     * 4. end 보정: Math.min(10, 10) = 10
     *    → end가 실제 마지막 페이지보다 크면 안 되므로 보정
     *
     * 5. prev: start > 1 → 1 > 1 = false
     *    → 1페이지 그룹이면 이전 그룹 없음
     *
     * 6. next: 95 > 10 * 10 = 100 → false
     *    → 다음 페이지 그룹이 있는지 확인
     */
    @Builder
    public PageResponseDTO(PageRequestDTO pageRequestDTO, List<E> dtoList, int totalCount) {
        // 데이터가 없으면 아무것도 설정하지 않음
        if (totalCount <= 0) {
            this.dtoList = List.of();
            return;
        }

        this.pageRequestDTO = pageRequestDTO;
        this.dtoList = dtoList;
        this.totalCount = totalCount;

        // 현재 페이지가 속한 페이지 그룹의 끝 번호 계산
        // 예: 3페이지 → 10, 15페이지 → 20
        int end = (int) (Math.ceil(pageRequestDTO.getPage() / 10.0)) * 10;

        // 현재 페이지 그룹의 시작 번호
        // 예: end가 10이면 start는 1, end가 20이면 start는 11
        int start = end - 9;

        // 실제 마지막 페이지 번호 계산
        // 예: 전체 95개, 페이지당 10개 → 10페이지
        int last = (int) (Math.ceil(totalCount / (double) pageRequestDTO.getSize()));

        // end가 실제 마지막 페이지보다 크면 보정
        // 예: 계산된 end가 10인데 실제로는 7페이지까지만 있으면 end=7
        end = Math.min(end, last);

        // 이전/다음 페이지 그룹 존재 여부
        this.prev = start > 1;
        this.next = totalCount > end * pageRequestDTO.getSize();

        // 페이지 번호 목록 생성 [start ~ end]
        // 예: start=1, end=10 → [1,2,3,4,5,6,7,8,9,10]
        this.pageNumList = IntStream.rangeClosed(start, end).boxed().collect(Collectors.toList());

        // 전체 페이지 수와 현재 페이지
        this.totalPage = last;
        this.current = pageRequestDTO.getPage();
    }
}
