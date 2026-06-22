package com.tourism.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 评论实体
 */
@Data
@TableName("comment")
public class Comment {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long userId;
    
    /**
     * 目标类型：POST-动态 SCENIC-景点 HOTEL-酒店 GUIDE-攻略
     */
    private String targetType;
    
    private Long targetId;
    
    /**
     * 父评论ID（用于回复）
     */
    private Long parentId;
    
    private String content;
    
    private Integer likeCount;
    
    /**
     * 状态：0-已删除 1-正常 2-待审核
     */
    private Integer status;
    
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
    
    @TableField(exist = false)
    private String targetName;
}
