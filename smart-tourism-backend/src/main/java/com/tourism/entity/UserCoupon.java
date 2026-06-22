package com.tourism.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户优惠券实体
 */
@Data
@TableName("user_coupon")
public class UserCoupon {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long userId;
    
    private Long couponId;
    
    private String status;  // UNUSED-未使用 USED-已使用 EXPIRED-已过期
    
    private Long orderId;
    
    private LocalDateTime useTime;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    // 非数据库字段 - 优惠券详情
    @TableField(exist = false)
    private Coupon coupon;
}
