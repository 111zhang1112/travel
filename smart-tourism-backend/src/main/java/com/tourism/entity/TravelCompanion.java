package com.tourism.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 结伴实体
 */
@Data
@TableName("travel_companion")
public class TravelCompanion {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long userId;
    
    private String title;
    
    private String content;
    
    private String destination;
    
    private LocalDate startDate;
    
    private LocalDate endDate;
    
    private Integer peopleNeeded;
    
    private Integer currentPeople;
    
    /**
     * 预算范围：LOW-经济 MEDIUM-中等 HIGH-豪华
     */
    private String budgetRange;
    
    /**
     * 旅行风格：LEISURE-休闲 ADVENTURE-冒险 CULTURE-文化 FOOD-美食
     */
    private String travelStyle;
    
    /**
     * 联系方式：WECHAT-微信 PHONE-电话 QQ-QQ
     */
    private String contactMethod;
    
    private String contactInfo;
    
    private String requirements;
    
    private String images;
    
    private Integer viewCount;
    
    private Integer likeCount;
    
    private Integer commentCount;
    
    /**
     * 状态：0-待审核 1-招募中 2-已满员 3-已结束 4-已拒绝 5-已删除
     */
    private Integer status;
    
    private String rejectReason;
    
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
    private Boolean hasApplied;
}
