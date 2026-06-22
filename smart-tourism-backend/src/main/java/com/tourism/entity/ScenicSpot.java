package com.tourism.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 景点实体
 */
@Data
@TableName("scenic_spot")
public class ScenicSpot {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long merchantId;  // 商家ID
    
    private Long categoryId;  // 分类ID
    
    private String name;
    
    private String description;
    
    private String images;  // JSON 数组
    
    private BigDecimal latitude;
    
    private BigDecimal longitude;
    
    private String address;  // 详细地址
    
    private String trafficGuide;  // 交通指南
    
    private String travelTips;  // 游玩建议
    
    private String recommendedDuration;  // 推荐游玩时长
    
    private String bestSeason;  // 最佳游玩季节
    
    private BigDecimal ticketPrice;
    
    private String openingHours;
    
    private String tags;  // JSON 数组
    
    private BigDecimal rating;
    
    private Integer viewCount;
    
    private Integer status;  // 0-下架 1-上架
    
    // 兼容旧数据库字段
    private String location;  // 位置描述
    private String category;  // 分类（旧数据）
    private String region;    // 地区（旧数据）
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    @TableField(exist = false)
    private String categoryName;  // 非数据库字段，用于展示分类名称
}
