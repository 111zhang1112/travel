package com.tourism.task;

import com.tourism.service.SearchHistoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 搜索历史清理定时任务
 */
@Slf4j
@Component
public class SearchHistoryCleanupTask {
    
    @Autowired
    private SearchHistoryService searchHistoryService;
    
    /**
     * 清理过期的搜索记录
     * 每天凌晨2点执行
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void cleanExpiredRecords() {
        log.info("开始清理过期搜索记录...");
        try {
            searchHistoryService.cleanExpiredRecords();
            log.info("过期搜索记录清理完成");
        } catch (Exception e) {
            log.error("清理过期搜索记录失败", e);
        }
    }
}
