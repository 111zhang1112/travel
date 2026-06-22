package com.tourism.service;

import com.tourism.dto.HotSearchDTO;
import com.tourism.dto.SearchSuggestionDTO;
import com.tourism.dto.SearchStatisticsDTO;
import com.tourism.entity.SearchHistory;

import java.time.LocalDate;
import java.util.List;

/**
 * 搜索历史服务接口
 */
public interface SearchHistoryService {
    
    /**
     * 保存搜索记录
     * 如果用户已搜索过该关键词，则更新时间戳
     * 
     * @param userId 用户ID（未登录为null）
     * @param keyword 搜索关键词
     * @param searchType 搜索类型
     */
    void saveSearchHistory(Long userId, String keyword, String searchType);
    
    /**
     * 获取用户搜索历史
     * 
     * @param userId 用户ID
     * @param limit 限制数量（默认10）
     * @return 搜索历史列表
     */
    List<SearchHistory> getUserSearchHistory(Long userId, Integer limit);
    
    /**
     * 删除搜索历史记录
     * 
     * @param id 记录ID
     * @param userId 用户ID（用于权限验证）
     */
    void deleteSearchHistory(Long id, Long userId);
    
    /**
     * 清空用户搜索历史
     * 
     * @param userId 用户ID
     */
    void clearUserSearchHistory(Long userId);
    
    /**
     * 获取热门搜索
     * 
     * @param limit 限制数量（默认10）
     * @return 热门搜索列表
     */
    List<HotSearchDTO> getHotSearches(Integer limit);
    
    /**
     * 获取搜索建议
     * 
     * @param keyword 关键词
     * @param limit 限制数量（默认8）
     * @return 搜索建议列表
     */
    List<SearchSuggestionDTO> getSearchSuggestions(String keyword, Integer limit);
    
    /**
     * 获取搜索统计
     * 
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 搜索统计数据
     */
    SearchStatisticsDTO getSearchStatistics(LocalDate startDate, LocalDate endDate);
    
    /**
     * 清理过期记录（定时任务调用）
     * 清理30天前的搜索记录
     */
    void cleanExpiredRecords();
}
