package com.tourism.task;

import com.tourism.dto.HotSearchDTO;
import com.tourism.service.SearchHistoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 热门搜索缓存更新定时任务
 */
@Slf4j
@Component
public class HotSearchCacheTask {
    
    @Autowired
    private SearchHistoryService searchHistoryService;
    
    // 简单的内存缓存
    private static final ConcurrentHashMap<String, List<HotSearchDTO>> cache = new ConcurrentHashMap<>();
    private static final String CACHE_KEY = "hot_searches";
    
    /**
     * 更新热门搜索缓存
     * 每小时执行一次
     */
    @Scheduled(cron = "0 0 * * * ?")
    public void updateHotSearchCache() {
        log.info("开始更新热门搜索缓存...");
        try {
            List<HotSearchDTO> hotSearches = searchHistoryService.getHotSearches(10);
            cache.put(CACHE_KEY, hotSearches);
            log.info("热门搜索缓存更新完成，共{}条记录", hotSearches.size());
        } catch (Exception e) {
            log.error("更新热门搜索缓存失败", e);
        }
    }
    
    /**
     * 获取缓存的热门搜索
     * 
     * @return 热门搜索列表
     */
    public static List<HotSearchDTO> getCachedHotSearches() {
        return cache.get(CACHE_KEY);
    }
    
    /**
     * 应用启动时初始化缓存
     */
    @Scheduled(initialDelay = 5000, fixedDelay = Long.MAX_VALUE)
    public void initCache() {
        log.info("初始化热门搜索缓存...");
        updateHotSearchCache();
    }
}
