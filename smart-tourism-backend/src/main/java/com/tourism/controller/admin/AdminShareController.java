package com.tourism.controller.admin;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.tourism.common.Result;
import com.tourism.service.ShareStatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 管理员分享统计控制器
 */
@RestController
@RequestMapping("/api/admin/share")
@RequiredArgsConstructor
@SaCheckRole("ADMIN")
public class AdminShareController {

    private final ShareStatisticsService shareStatisticsService;

    /**
     * 获取分享统计数据
     */
    @GetMapping("/statistics")
    public Result<Map<String, Object>> getStatistics(
            @RequestParam(required = false, defaultValue = "7") Integer days) {
        LocalDateTime startTime = LocalDateTime.now().minusDays(days);
        Map<String, Object> statistics = shareStatisticsService.getOverallStatistics(startTime);
        return Result.success(statistics);
    }

    /**
     * 获取分享排行榜
     */
    @GetMapping("/ranking")
    public Result<Map<String, Object>> getRanking(
            @RequestParam(required = false, defaultValue = "scenic") String contentType,
            @RequestParam(required = false, defaultValue = "7") Integer days,
            @RequestParam(required = false, defaultValue = "10") Integer limit) {
        LocalDateTime startTime = LocalDateTime.now().minusDays(days);
        List<Map<String, Object>> ranking = shareStatisticsService.getShareRanking(contentType, startTime, limit);
        
        Map<String, Object> result = Map.of(
            "contentType", contentType,
            "days", days,
            "ranking", ranking
        );
        
        return Result.success(result);
    }

    /**
     * 获取平台分享统计
     */
    @GetMapping("/platform-statistics")
    public Result<List<Map<String, Object>>> getPlatformStatistics(
            @RequestParam(required = false, defaultValue = "7") Integer days) {
        LocalDateTime startTime = LocalDateTime.now().minusDays(days);
        List<Map<String, Object>> statistics = shareStatisticsService.getPlatformStatistics(startTime);
        return Result.success(statistics);
    }
}
