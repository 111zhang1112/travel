package com.tourism.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.tourism.common.Result;
import com.tourism.dto.HotSearchDTO;
import com.tourism.dto.SearchSuggestionDTO;
import com.tourism.dto.SearchStatisticsDTO;
import com.tourism.entity.SearchHistory;
import com.tourism.service.SearchHistoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 搜索历史控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/search")
public class SearchHistoryController {

    @Autowired
    private SearchHistoryService searchHistoryService;

    /**
     * 保存搜索记录
     */
    @PostMapping("/history")
    public Result<Void> saveSearchHistory(@RequestBody Map<String, String> request) {
        try {
            String keyword = request.get("keyword");
            String searchType = request.get("searchType");

            if (keyword == null || keyword.trim().isEmpty()) {
                return Result.error(400, "关键词不能为空");
            }
            if (keyword.length() > 100) {
                return Result.error(400, "关键词长度不能超过100个字符");
            }

            Long userId = null;
            try {
                if (StpUtil.isLogin()) {
                    userId = StpUtil.getLoginIdAsLong();
                }
            } catch (Exception ignored) {}

            searchHistoryService.saveSearchHistory(userId, keyword.trim(), searchType);
            return Result.success();
        } catch (Exception e) {
            log.error("保存搜索记录失败", e);
            return Result.error("保存搜索记录失败：" + e.getMessage());
        }
    }

    /**
     * 获取用户搜索历史
     */
    @GetMapping("/history")
    public Result<List<SearchHistory>> getUserSearchHistory(
            @RequestParam(required = false, defaultValue = "10") Integer limit) {
        try {
            if (!StpUtil.isLogin()) {
                return Result.success(List.of());
            }
            Long userId = StpUtil.getLoginIdAsLong();
            return Result.success(searchHistoryService.getUserSearchHistory(userId, limit));
        } catch (Exception e) {
            log.error("获取搜索历史失败", e);
            return Result.error("获取搜索历史失败：" + e.getMessage());
        }
    }

    /**
     * 删除单条搜索历史
     */
    @DeleteMapping("/history/{id}")
    public Result<Void> deleteSearchHistory(@PathVariable Long id) {
        try {
            if (!StpUtil.isLogin()) {
                return Result.error(401, "请先登录");
            }
            Long userId = StpUtil.getLoginIdAsLong();
            searchHistoryService.deleteSearchHistory(id, userId);
            return Result.success();
        } catch (IllegalArgumentException e) {
            return Result.error(400, e.getMessage());
        } catch (Exception e) {
            log.error("删除搜索历史失败", e);
            return Result.error("删除失败：" + e.getMessage());
        }
    }

    /**
     * 清空用户搜索历史
     */
    @DeleteMapping("/history/clear")
    public Result<Void> clearUserSearchHistory() {
        try {
            if (!StpUtil.isLogin()) {
                return Result.error(401, "请先登录");
            }
            Long userId = StpUtil.getLoginIdAsLong();
            searchHistoryService.clearUserSearchHistory(userId);
            return Result.success();
        } catch (Exception e) {
            log.error("清空搜索历史失败", e);
            return Result.error("清空失败：" + e.getMessage());
        }
    }

    /**
     * 获取热门搜索
     */
    @GetMapping("/hot")
    public Result<List<HotSearchDTO>> getHotSearches(
            @RequestParam(required = false, defaultValue = "10") Integer limit) {
        try {
            return Result.success(searchHistoryService.getHotSearches(limit));
        } catch (Exception e) {
            log.error("获取热门搜索失败", e);
            return Result.error("获取热门搜索失败：" + e.getMessage());
        }
    }

    /**
     * 获取搜索建议
     */
    @GetMapping("/suggestions")
    public Result<List<SearchSuggestionDTO>> getSearchSuggestions(
            @RequestParam String keyword,
            @RequestParam(required = false, defaultValue = "8") Integer limit) {
        try {
            if (keyword == null || keyword.trim().isEmpty()) {
                return Result.success(List.of());
            }
            return Result.success(searchHistoryService.getSearchSuggestions(keyword.trim(), limit));
        } catch (Exception e) {
            log.error("获取搜索建议失败", e);
            return Result.error("获取搜索建议失败：" + e.getMessage());
        }
    }

    /**
     * 获取搜索统计（管理员）
     */
    @GetMapping("/statistics")
    public Result<SearchStatisticsDTO> getSearchStatistics(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            if (!StpUtil.isLogin()) {
                return Result.error(401, "请先登录");
            }
            return Result.success(searchHistoryService.getSearchStatistics(startDate, endDate));
        } catch (Exception e) {
            log.error("获取搜索统计失败", e);
            return Result.error("获取搜索统计失败：" + e.getMessage());
        }
    }
}
