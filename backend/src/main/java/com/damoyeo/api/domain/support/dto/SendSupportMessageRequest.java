package com.damoyeo.api.domain.support.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 상담 메시지 전송 요청 DTO
 *
 * [용도]
 * WebSocket 또는 REST API로 상담 메시지를 전송할 때 사용합니다.
 *
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SendSupportMessageRequest {

    /**
     * 메시지 내용
     *
     * 최소 1자, 최대 2000자
     */
    @NotBlank(message = "메시지 내용을 입력해주세요.")
    @Size(max = 2000, message = "메시지는 2000자 이내로 입력해주세요.")
    private String message;
}
