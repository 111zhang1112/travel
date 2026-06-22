package com.tourism.dto;

import lombok.Data;

/**
 * 搜索建议DTO
 */
@Data
public class SearchSuggestionDTO {
    
    /**
     * 关键词
     */
    private String keyword;
    
    /**
     * 类型：scenic/hotel/guide
     */
    private String type;
    
    /**
     * 标题
     */
    private String title;
    
    /**
     * 实体ID
     */
    private Long id;
}
