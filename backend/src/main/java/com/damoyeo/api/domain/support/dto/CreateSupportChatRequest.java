package com.damoyeo.api.domain.support.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ============================================================================
 * 상담 생성 요청 DTO
 * ============================================================================
 *
 * @author damoyeo
 * @since 2025-03-16
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateSupportChatRequest {

    /**
     * 상담 제목
     */
    @NotBlank(message = "상담 제목을 입력해주세요.")
    @Size(max = 200, message = "제목은 200자 이내로 입력해주세요.")
    private String title;

    /**
     * 첫 메시지 내용
     */
    @NotBlank(message = "문의 내용을 입력해주세요.")
    @Size(max = 2000, message = "내용은 2000자 이내로 입력해주세요.")
    private String message;
}
