package com.damoyeo.api.domain.category.dto;

import com.damoyeo.api.domain.category.entity.Category;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 카테고리 응답 DTO
 *
 * 모임, 정모 등에서 카테고리 정보를 중첩 객체로 반환할 때 사용합니다.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CategoryDTO {

    private Long id;
    private String name;
    private String icon;
    private int displayOrder;

    /**
     * Entity -> DTO 변환
     */
    public static CategoryDTO from(Category category) {
        if (category == null) return null;
        return CategoryDTO.builder()
                .id(category.getId())
                .name(category.getName())
                .icon(category.getIcon())
                .displayOrder(category.getDisplayOrder())
                .build();
    }
}
