package com.tourism.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 优惠券实体
 */
@Data
@TableName("coupon")
public class Coupon {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String name;
    
    private String type;  // DISCOUNT-折扣券 AMOUNT-满减券
    
    private BigDecimal value;  // 优惠值
    
    private BigDecimal minAmount;  // 最低消费金额
    
    private Integer totalCount;  // 发放总量
    
    private Integer remainCount;  // 剩余数量
    
    private LocalDateTime startTime;
    
    private LocalDateTime endTime;
    
    private String description;
    
    private Integer status;  // 0-禁用 1-启用
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    // 非数据库字段
    @TableField(exist = false)
    private Boolean canReceive;  // 是否可领取
    
    @TableField(exist = false)
    private Boolean received;  // 是否已领取
}
