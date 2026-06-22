package com.tourism.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 酒店实体
 */
@Data
@TableName("hotel")
public class Hotel {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long merchantId;  // 商家ID
    
    private String name;
    
    private String description;
    
    private String images;  // JSON 数组
    
    private BigDecimal latitude;
    
    private BigDecimal longitude;
    
    private BigDecimal priceMin;
    
    private BigDecimal priceMax;
    
    private Integer starRating;  // 1-5 星
    
    private BigDecimal rating;  // 用户评分 1.0-5.0
    
    private String amenities;  // JSON 数组，设施标签
    
    private String address;
    
    // 兼容旧数据库字段
    private String location;   // 位置描述
    private String facilities; // 设施列表（逗号分隔）
    private String tag;        // 标签
    private String region;     // 地区
    
    private Integer status;  // 0-下架 1-上架
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
