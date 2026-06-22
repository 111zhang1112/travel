package com.tourism.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 创建订单请求
 */
@Data
public class CreateOrderRequest {
    
    @NotBlank(message = "商品类型不能为空")
    private String productType;  // SCENIC, HOTEL, ROUTE
    
    @NotNull(message = "商品ID不能为空")
    private Long productId;
    
    private String productName;
    
    @NotNull(message = "金额不能为空")
    private BigDecimal amount;
    
    private Integer quantity;
    
    // 酒店预订相关字段
    private Long roomId;  // 房型ID
    private String checkInDate;  // 入住日期 YYYY-MM-DD
    private String checkOutDate;  // 退房日期 YYYY-MM-DD
    private Integer nights;  // 入住天数
}
