package com.tourism.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 积分记录实体
 */
@Data
@TableName("points_record")
public class PointsRecord {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long userId;
    
    private Integer points;  // 积分变动（正数增加，负数减少）
    
    private String type;  // ORDER-订单 REVIEW-评价 EXCHANGE-兑换 ADMIN-管理员调整
    
    private String description;
    
    private Long relatedId;  // 关联ID
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
