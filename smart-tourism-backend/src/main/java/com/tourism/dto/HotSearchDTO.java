package com.tourism.dto;

import lombok.Data;

/**
 * 热门搜索DTO
 */
@Data
public class HotSearchDTO {
    
    /**
     * 搜索关键词
     */
    private String keyword;
    
    /**
     * 搜索次数
     */
    private Long searchCount;
    
    /**
     * 排名
     */
    private Integer ranking;
}
