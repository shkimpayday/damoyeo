package com.damoyeo.api.domain.payment.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 카카오페이 결제 승인 응답 DTO
 *
 * [용도]
 * 카카오페이 API의 /v1/payment/approve 응답을 매핑합니다.
 *
 * [주요 필드]
 * - aid: 요청 고유번호
 * - tid: 결제 고유번호
 * - payment_method_type: 결제 수단 (CARD, MONEY)
 * - amount: 결제 금액 정보
 * - approved_at: 결제 승인 시간
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KakaoPayApproveResponse {

    /**
     * 요청 고유번호
     */
    private String aid;

    /**
     * 결제 고유번호
     */
    private String tid;

    /**
     * 가맹점 코드
     */
    private String cid;

    /**
     * 가맹점 주문번호
     */
    @JsonProperty("partner_order_id")
    private String partnerOrderId;

    /**
     * 가맹점 회원 ID
     */
    @JsonProperty("partner_user_id")
    private String partnerUserId;

    /**
     * 결제 수단
     * - CARD: 카드
     * - MONEY: 카카오페이머니
     */
    @JsonProperty("payment_method_type")
    private String paymentMethodType;

    /**
     * 상품 이름
     */
    @JsonProperty("item_name")
    private String itemName;

    /**
     * 상품 코드
     */
    @JsonProperty("item_code")
    private String itemCode;

    /**
     * 상품 수량
     */
    private Integer quantity;

    /**
     * 결제 금액 정보
     */
    private Amount amount;

    /**
     * 결제 준비 요청 시간
     */
    @JsonProperty("created_at")
    private String createdAt;

    /**
     * 결제 승인 시간
     */
    @JsonProperty("approved_at")
    private String approvedAt;

    /**
     * 결제 금액 정보 내부 클래스
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Amount {
        /**
         * 총 결제 금액
         */
        private Integer total;

        /**
         * 비과세 금액
         */
        @JsonProperty("tax_free")
        private Integer taxFree;

        /**
         * 부가세
         */
        private Integer vat;

        /**
         * 할인 금액
         */
        private Integer discount;
    }
}
