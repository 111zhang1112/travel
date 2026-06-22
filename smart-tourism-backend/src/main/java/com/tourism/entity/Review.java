package com.tourism.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 用户评价实体
 */
@Data
@TableName("review")
public class Review {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long userId;
    
    private String targetType;  // SCENIC / HOTEL
    
    private Long targetId;
    
    private BigDecimal rating;  // 1.0 - 5.0
    
    private String content;
    
    private String images;  // JSON数组
    
    private Integer status;  // 0-待审核 1-已通过 2-已拒绝
    
    private Integer likeCount;  // 点赞数
    
    private String reply;  // 商家回复
    
    private LocalDateTime replyTime;  // 回复时间
    
    // 兼容旧数据库字段
    private Long spotId;      // 景点ID（旧数据）
    private String userName;  // 用户名（旧数据）
    private String spotName;  // 景点名称（旧数据）
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    // 非数据库字段 - 用于前端展示
    @TableField(exist = false)
    private String username;  // 用户昵称
    
    @TableField(exist = false)
    private String userAvatar;  // 用户头像
    
    @TableField(exist = false)
    private String targetName;  // 目标名称（景点/酒店名）
    
    @TableField(exist = false)
    private Boolean liked;  // 当前用户是否已点赞
    
    // 手动添加 setter 方法以解决 Lombok 编译问题
    public void setUsername(String username) {
        this.username = username;
    }
    
    public void setUserAvatar(String userAvatar) {
        this.userAvatar = userAvatar;
    }
    
    public void setTargetName(String targetName) {
        this.targetName = targetName;
    }
}
