package com.tourism.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 创建支付请求
 */
@Data
public class CreatePaymentRequest {
    private String productType;
    private Long productId;
    private String productName;
    private BigDecimal amount;
    private String paymentMethod; // ALIPAY/WECHAT
    
    // 通用字段
    private Integer quantity;
    
    // 酒店预订特定字段
    private Long roomId;
    private String checkInDate;
    private String checkOutDate;
    private Integer nights;
}
