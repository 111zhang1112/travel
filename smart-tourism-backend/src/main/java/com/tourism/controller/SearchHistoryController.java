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

import java.time.LocalDate;
import java.util.HashMap;
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
    public ResponseEntity<Map<String, Object>> saveSearchHistory(
            @RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String keyword = request.get("keyword");
            String searchType = request.get("searchType");
            
            if (keyword == null || keyword.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "关键词不能为空");
                return ResponseEntity.badRequest().body(response);
            }
            
            if (keyword.length() > 100) {
                response.put("success", false);
                response.put("message", "关键词长度不能超过100个字符");
                return ResponseEntity.badRequest().body(response);
            }
            
            // 获取当前登录用户ID（如果已登录）
            Long userId = null;
            try {
                if (StpUtil.isLogin()) {
                    userId = StpUtil.getLoginIdAsLong();
                }
            } catch (Exception e) {
                // 未登录，userId保持为null
            }
            
            searchHistoryService.saveSearchHistory(userId, keyword.trim(), searchType);
            
            response.put("success", true);
            response.put("message", "搜索记录已保存");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("保存搜索记录失败", e);
            response.put("success", false);
            response.put("message", "保存搜索记录失败：" + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * 获取用户搜索历史
     */
    @GetMapping("/history")
    public ResponseEntity<Map<String, Object>> getUserSearchHistory(
            @RequestParam(required = false, defaultValue = "10") Integer limit) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 必须登录才能查看历史
            if (!StpUtil.isLogin()) {
                response.put("success", false);
                response.put("message", "请先登录");
                response.put("data", List.of());
                return ResponseEntity.ok(response);
            }
            
            Long userId = StpUtil.getLoginIdAsLong();
            List<SearchHistory> history = searchHistoryService.getUserSearchHistory(userId, limit);
            
            response.put("success", true);
            response.put("data", history);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("获取搜索历史失败", e);
            response.put("success", false);
            response.put("message", "获取搜索历史失败：" + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * 删除单条搜索历史
     */
    @DeleteMapping("/history/{id}")
    public ResponseEntity<Map<String, Object>> deleteSearchHistory(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (!StpUtil.isLogin()) {
                response.put("success", false);
                response.put("message", "请先登录");
                return ResponseEntity.status(401).body(response);
            }
            
            Long userId = StpUtil.getLoginIdAsLong();
            searchHistoryService.deleteSearchHistory(id, userId);
            
            response.put("success", true);
            response.put("message", "删除成功");
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            log.error("删除搜索历史失败", e);
            response.put("success", false);
            response.put("message", "删除失败：" + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * 清空用户搜索历史
     */
    @DeleteMapping("/history/clear")
    public ResponseEntity<Map<String, Object>> clearUserSearchHistory() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (!StpUtil.isLogin()) {
                response.put("success", false);
                response.put("message", "请先登录");
                return ResponseEntity.status(401).body(response);
            }
            
            Long userId = StpUtil.getLoginIdAsLong();
            searchHistoryService.clearUserSearchHistory(userId);
            
            response.put("success", true);
            response.put("message", "清空成功");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("清空搜索历史失败", e);
            response.put("success", false);
            response.put("message", "清空失败：" + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * 获取热门搜索
     */
    @GetMapping("/hot")
    public Result<List<HotSearchDTO>> getHotSearches(
            @RequestParam(required = false, defaultValue = "10") Integer limit) {
        try {
            List<HotSearchDTO> hotSearches = searchHistoryService.getHotSearches(limit);
            return Result.success(hotSearches);
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
            List<SearchSuggestionDTO> suggestions =
                searchHistoryService.getSearchSuggestions(keyword.trim(), limit);
            return Result.success(suggestions);
        } catch (Exception e) {
            log.error("获取搜索建议失败", e);
            return Result.error("获取搜索建议失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取搜索统计（管理员）
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getSearchStatistics(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 验证管理员权限
            if (!StpUtil.isLogin()) {
                response.put("success", false);
                response.put("message", "请先登录");
                return ResponseEntity.status(401).body(response);
            }
            
            // 这里可以添加管理员权限检查
            // StpUtil.checkRole("admin");
            
            SearchStatisticsDTO statistics = 
                searchHistoryService.getSearchStatistics(startDate, endDate);
            
            response.put("success", true);
            response.put("data", statistics);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("获取搜索统计失败", e);
            response.put("success", false);
            response.put("message", "获取搜索统计失败：" + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
}
