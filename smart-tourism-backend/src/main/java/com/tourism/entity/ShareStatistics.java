package com.tourism.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 分享统计实体
 */
@Data
@TableName("share_statistics")
public class ShareStatistics {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String contentType;    // 内容类型
    
    private Long contentId;        // 内容ID
    
    private Integer shareCount;    // 分享次数
    
    private Integer viewCount;     // 访问次数
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
