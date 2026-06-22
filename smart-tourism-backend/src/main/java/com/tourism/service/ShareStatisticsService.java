package com.tourism.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tourism.entity.ShareStatistics;
import com.tourism.mapper.ShareStatisticsMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 分享统计服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ShareStatisticsService extends ServiceImpl<ShareStatisticsMapper, ShareStatistics> {

    /**
     * 增加分享次数
     */
    @Async
    @Transactional
    public void incrementShareCount(String contentType, Long contentId) {
        try {
            // 确保统计记录存在
            ensureStatisticsExists(contentType, contentId);
            
            // 更新统计表
            baseMapper.incrementShareCount(contentType, contentId);
            
            // 更新内容表的分享计数
            updateContentShareCount(contentType, contentId);
        } catch (Exception e) {
            log.error("更新分享统计失败: contentType={}, contentId={}", contentType, contentId, e);
        }
    }

    /**
     * 增加访问次数
     */
    @Async
    @Transactional
    public void incrementViewCount(String contentType, Long contentId) {
        try {
            // 确保统计记录存在
            ensureStatisticsExists(contentType, contentId);
            
            baseMapper.incrementViewCount(contentType, contentId);
        } catch (Exception e) {
            log.error("更新访问统计失败: contentType={}, contentId={}", contentType, contentId, e);
        }
    }

    /**
     * 确保统计记录存在
     */
    private void ensureStatisticsExists(String contentType, Long contentId) {
        ShareStatistics existing = baseMapper.getByContent(contentType, contentId);
        if (existing == null) {
            ShareStatistics statistics = new ShareStatistics();
            statistics.setContentType(contentType);
            statistics.setContentId(contentId);
            statistics.setShareCount(0);
            statistics.setViewCount(0);
            save(statistics);
        }
    }

    /**
     * 更新内容表的分享计数
     */
    private void updateContentShareCount(String contentType, Long contentId) {
        String tableName = getTableName(contentType);
        if (tableName != null) {
            baseMapper.updateContentShareCount(tableName, contentId);
        }
    }

    /**
     * 获取内容类型对应的表名
     */
    private String getTableName(String contentType) {
        return switch (contentType) {
            case "scenic" -> "scenic_spot";
            case "hotel" -> "hotel";
            case "guide" -> "travel_guide";
            case "route" -> "travel_route";
            default -> null;
        };
    }

    /**
     * 获取统计数据
     */
    public ShareStatistics getStatistics(String contentType, Long contentId) {
        return baseMapper.getByContent(contentType, contentId);
    }

    /**
     * 获取分享排行榜
     */
    public List<Map<String, Object>> getShareRanking(String contentType, LocalDateTime startTime, int limit) {
        return baseMapper.getShareRanking(contentType, startTime, limit);
    }

    /**
     * 获取平台分享统计
     */
    public List<Map<String, Object>> getPlatformStatistics(LocalDateTime startTime) {
        return baseMapper.getPlatformStatistics(startTime);
    }

    /**
     * 获取总体统计数据
     */
    public Map<String, Object> getOverallStatistics(LocalDateTime startTime) {
        return baseMapper.getOverallStatistics(startTime);
    }
}
