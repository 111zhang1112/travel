package com.tourism.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户行为实体（用于推荐系统）
 */
@Data
@TableName("interactions")
public class Interaction {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long userId;
    
    private String targetType;  // SCENIC, HOTEL, GUIDE
    
    private Long targetId;
    
    private String actionType;  // VIEW, FAVORITE, LIKE
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
