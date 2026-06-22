package com.tourism.controller;

import com.tourism.common.Result;
import com.tourism.entity.ShareRecord;
import com.tourism.entity.ShareStatistics;
import com.tourism.service.ShareService;
import com.tourism.service.ShareStatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * 分享控制器
 */
@RestController
@RequestMapping("/api/share")
@RequiredArgsConstructor
public class ShareController {

    private final ShareService shareService;
    private final ShareStatisticsService shareStatisticsService;

    /**
     * 创建分享
     */
    @PostMapping("/create")
    public Result<Map<String, Object>> createShare(@RequestBody Map<String, Object> params) {
        String contentType = (String) params.get("contentType");
        Long contentId = Long.valueOf(params.get("contentId").toString());
        String platform = (String) params.get("platform");

        ShareRecord record = shareService.createShare(contentType, contentId, platform);

        Map<String, Object> result = new HashMap<>();
        result.put("shareUrl", record.getShareUrl());
        result.put("shareId", record.getId());
        result.put("fullUrl", "http://localhost:5173" + record.getShareUrl());

        return Result.success(result);
    }

    /**
     * 获取分享链接详情(用于跳转)
     */
    @GetMapping("/url/{encoded}")
    public Result<Map<String, Object>> getShareUrl(@PathVariable String encoded) {
        try {
            // 解码分享链接
            String decoded = new String(Base64.getUrlDecoder().decode(encoded));
            String[] parts = decoded.split(":");
            String contentType = parts[0];
            Long contentId = Long.valueOf(parts[1]);

            // 查找分享记录
            String shareUrl = "/s/" + encoded;
            ShareRecord record = shareService.getByShareUrl(shareUrl);

            if (record != null) {
                // 记录访问
                shareService.recordView(record.getId());
            }

            // 返回跳转信息
            Map<String, Object> result = new HashMap<>();
            result.put("contentType", contentType);
            result.put("contentId", contentId);
            result.put("redirectUrl", getRedirectUrl(contentType, contentId));

            return Result.success(result);
        } catch (Exception e) {
            return Result.error("分享链接无效");
        }
    }

    /**
     * 获取内容分享统计
     */
    @GetMapping("/statistics/{contentType}/{contentId}")
    public Result<ShareStatistics> getStatistics(
            @PathVariable String contentType,
            @PathVariable Long contentId) {
        ShareStatistics statistics = shareStatisticsService.getStatistics(contentType, contentId);
        return Result.success(statistics);
    }

    /**
     * 记录分享链接访问
     */
    @PostMapping("/record-view/{shareId}")
    public Result<Void> recordView(@PathVariable Long shareId) {
        shareService.recordView(shareId);
        return Result.success();
    }

    /**
     * 获取跳转URL
     */
    private String getRedirectUrl(String contentType, Long contentId) {
        return switch (contentType) {
            case "scenic" -> "/scenic/" + contentId;
            case "hotel" -> "/hotel/" + contentId;
            case "guide" -> "/guide/" + contentId;
            case "route" -> "/route/" + contentId;
            default -> "/";
        };
    }
}
