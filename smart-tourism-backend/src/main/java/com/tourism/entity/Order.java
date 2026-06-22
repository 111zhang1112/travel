package com.tourism.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单实体
 */
@Data
@TableName("orders")
public class Order {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String orderNo;
    
    private Long userId;
    
    private String productType;  // SCENIC, HOTEL
    
    private Long productId;
    
    private String productName;
    
    private BigDecimal amount;
    
    private Integer quantity;
    
    // 酒店预订相关字段
    private Long roomId;  // 房型ID
    private String checkInDate;  // 入住日期 YYYY-MM-DD
    private String checkOutDate;  // 退房日期 YYYY-MM-DD
    private Integer nights;  // 入住天数
    
    private String status;  // PENDING, PAID, COMPLETED, CANCELLED
    
    private LocalDateTime payTime;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
