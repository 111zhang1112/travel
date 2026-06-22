package com.tourism.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 搜索统计DTO
 */
@Data
public class SearchStatisticsDTO {
    
    /**
     * 总搜索次数
     */
    private Long totalSearches;
    
    /**
     * 热门关键词列表
     */
    private List<KeywordStatDTO> topKeywords;
    
    /**
     * 按日期统计的搜索次数
     */
    private Map<String, Long> searchesByDate;
    
    /**
     * 关键词统计DTO
     */
    @Data
    public static class KeywordStatDTO {
        private String keyword;
        private Long count;
        private Double percentage;
    }
}
