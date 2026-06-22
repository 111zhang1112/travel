package com.tourism.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 支付订单实体
 */
@Data
@TableName("payment_order")
public class PaymentOrder {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String orderNo;
    private String tradeNo;
    private Long userId;
    private String productType;
    private Long productId;
    private String productName;
    private BigDecimal amount;
    private String paymentMethod;
    private String status;
    private LocalDateTime payTime;
    private LocalDateTime notifyTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
