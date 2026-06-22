package com.tourism.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 分享记录实体
 */
@Data
@TableName("share_record")
public class ShareRecord {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long userId;           // 分享者ID
    
    private String contentType;    // 内容类型: scenic/hotel/guide/route
    
    private Long contentId;        // 内容ID
    
    private String platform;       // 分享平台: wechat/weibo/qq/link
    
    private String shareUrl;       // 分享链接
    
    private Integer viewCount;     // 访问次数
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
