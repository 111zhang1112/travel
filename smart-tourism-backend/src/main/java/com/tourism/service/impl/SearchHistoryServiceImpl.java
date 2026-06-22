package com.tourism.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tourism.dto.HotSearchDTO;
import com.tourism.dto.SearchSuggestionDTO;
import com.tourism.dto.SearchStatisticsDTO;
import com.tourism.entity.Hotel;
import com.tourism.entity.ScenicSpot;
import com.tourism.entity.SearchHistory;
import com.tourism.entity.TravelGuide;
import com.tourism.mapper.HotelMapper;
import com.tourism.mapper.ScenicSpotMapper;
import com.tourism.mapper.SearchHistoryMapper;
import com.tourism.mapper.TravelGuideMapper;
import com.tourism.service.SearchHistoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 搜索历史服务实现
 */
@Slf4j
@Service
public class SearchHistoryServiceImpl implements SearchHistoryService {
    
    @Autowired
    private SearchHistoryMapper searchHistoryMapper;
    
    @Autowired
    private ScenicSpotMapper scenicSpotMapper;
    
    @Autowired
    private HotelMapper hotelMapper;
    
    @Autowired
    private TravelGuideMapper travelGuideMapper;
    
    @Override
    @Transactional
    public void saveSearchHistory(Long userId, String keyword, String searchType) {
        if (!StringUtils.hasText(keyword) || keyword.length() > 100) {
            log.warn("Invalid keyword: {}", keyword);
            return;
        }
        
        // 检查用户是否已搜索过该关键词
        if (userId != null) {
            SearchHistory existing = searchHistoryMapper.selectByUserIdAndKeyword(userId, keyword);
            if (existing != null) {
                // 更新时间戳
                existing.setSearchTime(LocalDateTime.now());
                existing.setUpdateTime(LocalDateTime.now());
                searchHistoryMapper.updateById(existing);
                return;
            }
        }
        
        // 创建新记录
        SearchHistory history = new SearchHistory();
        history.setUserId(userId);
        history.setKeyword(keyword);
        history.setSearchType(searchType != null ? searchType : "all");
        history.setSearchTime(LocalDateTime.now());
        history.setCreateTime(LocalDateTime.now());
        history.setUpdateTime(LocalDateTime.now());
        
        searchHistoryMapper.insert(history);
    }
    
    @Override
    public List<SearchHistory> getUserSearchHistory(Long userId, Integer limit) {
        if (userId == null) {
            return new ArrayList<>();
        }
        
        int queryLimit = (limit != null && limit > 0) ? Math.min(limit, 10) : 10;
        return searchHistoryMapper.selectUserSearchHistory(userId, queryLimit);
    }
    
    @Override
    @Transactional
    public void deleteSearchHistory(Long id, Long userId) {
        if (id == null || userId == null) {
            throw new IllegalArgumentException("ID and userId cannot be null");
        }
        
        // 验证记录属于该用户
        SearchHistory history = searchHistoryMapper.selectById(id);
        if (history == null) {
            throw new IllegalArgumentException("Search history not found");
        }
        
        if (!userId.equals(history.getUserId())) {
            throw new IllegalArgumentException("Unauthorized to delete this record");
        }
        
        searchHistoryMapper.deleteById(id);
    }
    
    @Override
    @Transactional
    public void clearUserSearchHistory(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("userId cannot be null");
        }
        
        LambdaQueryWrapper<SearchHistory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SearchHistory::getUserId, userId);
        searchHistoryMapper.delete(wrapper);
    }
    
    @Override
    public List<HotSearchDTO> getHotSearches(Integer limit) {
        int queryLimit = (limit != null && limit > 0) ? Math.min(limit, 10) : 10;
        
        // 查询最近7天的热门搜索
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        List<HotSearchDTO> hotSearches = searchHistoryMapper.selectHotSearches(sevenDaysAgo, queryLimit);
        
        // 添加排名
        for (int i = 0; i < hotSearches.size(); i++) {
            hotSearches.get(i).setRanking(i + 1);
        }
        
        return hotSearches;
    }
    
    @Override
    public List<SearchSuggestionDTO> getSearchSuggestions(String keyword, Integer limit) {
        if (!StringUtils.hasText(keyword)) {
            return new ArrayList<>();
        }
        
        int queryLimit = (limit != null && limit > 0) ? Math.min(limit, 8) : 8;
        List<SearchSuggestionDTO> suggestions = new ArrayList<>();
        
        String likeKeyword = "%" + keyword + "%";
        
        // 搜索景点
        LambdaQueryWrapper<ScenicSpot> scenicWrapper = new LambdaQueryWrapper<>();
        scenicWrapper.like(ScenicSpot::getName, keyword)
                    .or()
                    .like(ScenicSpot::getLocation, keyword)
                    .last("LIMIT " + (queryLimit / 2));
        
        List<ScenicSpot> scenicSpots = scenicSpotMapper.selectList(scenicWrapper);
        for (ScenicSpot spot : scenicSpots) {
            SearchSuggestionDTO dto = new SearchSuggestionDTO();
            dto.setKeyword(spot.getName());
            dto.setType("scenic");
            dto.setTitle(spot.getName());
            dto.setId(spot.getId());
            suggestions.add(dto);
        }
        
        // 搜索酒店
        if (suggestions.size() < queryLimit) {
            LambdaQueryWrapper<Hotel> hotelWrapper = new LambdaQueryWrapper<>();
            hotelWrapper.like(Hotel::getName, keyword)
                       .or()
                       .like(Hotel::getLocation, keyword)
                       .last("LIMIT " + (queryLimit - suggestions.size()));
            
            List<Hotel> hotels = hotelMapper.selectList(hotelWrapper);
            for (Hotel hotel : hotels) {
                SearchSuggestionDTO dto = new SearchSuggestionDTO();
                dto.setKeyword(hotel.getName());
                dto.setType("hotel");
                dto.setTitle(hotel.getName());
                dto.setId(hotel.getId());
                suggestions.add(dto);
            }
        }
        
        // 搜索攻略
        if (suggestions.size() < queryLimit) {
            LambdaQueryWrapper<TravelGuide> guideWrapper = new LambdaQueryWrapper<>();
            guideWrapper.like(TravelGuide::getTitle, keyword)
                       .last("LIMIT " + (queryLimit - suggestions.size()));
            
            List<TravelGuide> guides = travelGuideMapper.selectList(guideWrapper);
            for (TravelGuide guide : guides) {
                SearchSuggestionDTO dto = new SearchSuggestionDTO();
                dto.setKeyword(guide.getTitle());
                dto.setType("guide");
                dto.setTitle(guide.getTitle());
                dto.setId(guide.getId());
                suggestions.add(dto);
            }
        }
        
        return suggestions.stream().limit(queryLimit).collect(Collectors.toList());
    }
    
    @Override
    public SearchStatisticsDTO getSearchStatistics(LocalDate startDate, LocalDate endDate) {
        LocalDateTime startTime = startDate.atStartOfDay();
        LocalDateTime endTime = endDate.atTime(23, 59, 59);
        
        SearchStatisticsDTO statistics = new SearchStatisticsDTO();
        
        // 总搜索次数
        Long totalSearches = searchHistoryMapper.countSearches(startTime, endTime);
        statistics.setTotalSearches(totalSearches);
        
        // 热门关键词
        List<Map<String, Object>> topKeywordsData = searchHistoryMapper.selectTopKeywords(startTime, endTime, 20);
        List<SearchStatisticsDTO.KeywordStatDTO> topKeywords = new ArrayList<>();
        
        for (Map<String, Object> data : topKeywordsData) {
            SearchStatisticsDTO.KeywordStatDTO keywordStat = new SearchStatisticsDTO.KeywordStatDTO();
            keywordStat.setKeyword((String) data.get("keyword"));
            keywordStat.setCount(((Number) data.get("count")).longValue());
            keywordStat.setPercentage(totalSearches > 0 ? 
                (keywordStat.getCount() * 100.0 / totalSearches) : 0.0);
            topKeywords.add(keywordStat);
        }
        statistics.setTopKeywords(topKeywords);
        
        // 按日期统计
        List<Map<String, Object>> dateData = searchHistoryMapper.countSearchesByDate(startTime, endTime);
        Map<String, Long> searchesByDate = new LinkedHashMap<>();
        
        for (Map<String, Object> data : dateData) {
            String date = data.get("date").toString();
            Long count = ((Number) data.get("count")).longValue();
            searchesByDate.put(date, count);
        }
        statistics.setSearchesByDate(searchesByDate);
        
        return statistics;
    }
    
    @Override
    @Transactional
    public void cleanExpiredRecords() {
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        int deletedCount = searchHistoryMapper.deleteExpiredRecords(thirtyDaysAgo);
        log.info("Cleaned {} expired search records", deletedCount);
    }
}
