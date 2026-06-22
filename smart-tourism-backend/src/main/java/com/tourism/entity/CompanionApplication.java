package com.tourism.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 结伴申请实体
 */
@Data
@TableName("companion_application")
public class CompanionApplication {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long companionId;
    
    private Long userId;
    
    private String message;
    
    /**
     * 状态：0-待处理 1-已同意 2-已拒绝
     */
    private Integer status;
    
    private String reply;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    // 非数据库字段
    @TableField(exist = false)
    private String username;
    
    @TableField(exist = false)
    private String nickname;
    
    @TableField(exist = false)
    private String avatar;
}
