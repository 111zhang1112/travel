package com.tourism.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 社区动态实体
 */
@Data
@TableName("community_post")
public class CommunityPost {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long userId;
    
    private String title;
    
    private String content;
    
    private String images;
    
    private String location;
    
    private Integer viewCount;
    
    private Integer likeCount;
    
    private Integer commentCount;
    
    /**
     * 状态：0-待审核 1-已发布 2-已拒绝 3-已删除
     */
    private Integer status;
    
    private String rejectReason;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    // 非数据库字段 - 用户信息
    @TableField(exist = false)
    private String username;
    
    @TableField(exist = false)
    private String nickname;
    
    @TableField(exist = false)
    private String avatar;
    
    // 小程序兼容字段（getter方法会自动序列化为JSON字段）
    public String getUserName() {
        return nickname != null ? nickname : username;
    }
    
    public String getUserAvatar() {
        return avatar;
    }
}
