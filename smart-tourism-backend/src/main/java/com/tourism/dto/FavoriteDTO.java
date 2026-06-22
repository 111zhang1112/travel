package com.tourism.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 收藏DTO
 */
@Data
public class FavoriteDTO {
    
    /**
     * 收藏ID
     */
    private Long id;
    
    /**
     * 目标类型：SCENIC/HOTEL
     */
    private String targetType;
    
    /**
     * 目标ID
     */
    private Long targetId;
    
    /**
     * 目标名称
     */
    private String name;
    
    /**
     * 目标图片
     */
    private String image;
    
    /**
     * 位置信息
     */
    private String location;
    
    /**
     * 评分
     */
    private Double rating;
    
    /**
     * 价格
     */
    private Double price;
    
    /**
     * 收藏时间
     */
    private LocalDateTime createTime;
}
