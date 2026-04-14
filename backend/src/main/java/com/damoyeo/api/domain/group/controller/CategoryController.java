package com.damoyeo.api.domain.group.controller;

import com.damoyeo.api.domain.category.entity.Category;
import com.damoyeo.api.domain.category.repository.CategoryRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 카테고리 API Controller
 *
 * 모임 카테고리 목록을 조회하는 API를 제공합니다.
 *
 * [카테고리 데이터]
 * DataInitializer에서 서버 시작 시 18개의 카테고리가 자동 생성됩니다:
 * - 운동/스포츠, 사교/인맥, 아웃도어/여행, 문화/공연, 음악/악기
 * - 외국어, 독서, 스터디, 게임/오락, 사진/영상
 * - 요리, 공예, 자기계발, 봉사활동, 반려동물
 * - IT/개발, 금융/재테크, 기타
 *
 * [엔드포인트]
 * GET /api/categories - 카테고리 목록 조회 (인증 불필요)
 *
 * [프론트엔드 사용 위치]
 * - 모임 생성 폼: 카테고리 선택 드롭다운
 * - 모임 목록 페이지: 카테고리 필터
 * - 메인 페이지: 카테고리별 모임 탭
 *
 * [참고]
 * - 이 컨트롤러는 group 패키지 내에 있지만, 카테고리는 모임 외에도 사용될 수 있어
 *   별도의 /api/categories 경로를 사용합니다.
 * - 인증이 필요 없는 공개 API입니다.
 */
@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@Tag(name = "Category", description = "카테고리 API")
public class CategoryController {

    /**
     * CategoryRepository 직접 사용
     *
     * [왜 Service 없이 Repository를 직접 사용하는가?]
     * 단순한 목록 조회라서 별도의 비즈니스 로직이 없습니다.
     * 복잡한 로직이 추가되면 CategoryService를 만드는 것이 좋습니다.
     */
    private final CategoryRepository categoryRepository;

    /**
     * 카테고리 목록 조회
     *
     * [프론트엔드 요청]
     * GET /api/categories
     *
     * [응답 예시]
     * [
     *   { "id": 1, "name": "운동/스포츠", "icon": "🏃", "displayOrder": 1 },
     *   { "id": 2, "name": "사교/인맥", "icon": "🤝", "displayOrder": 2 },
     *   ...
     * ]
     *
     * [정렬]
     * displayOrder 오름차순 (DataInitializer에서 지정한 순서)
     *
     * [캐싱 고려]
     * 카테고리는 자주 변경되지 않으므로,
     * 프론트엔드에서 캐싱하거나 Spring Cache를 적용할 수 있습니다.
     */
    @GetMapping
    @Operation(summary = "카테고리 목록 조회")
    public ResponseEntity<List<Category>> getCategories() {
        return ResponseEntity.ok(categoryRepository.findAllByOrderByDisplayOrderAsc());
    }
}
