package com.tourism.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.tourism.common.Result;
import com.tourism.dto.InteractionRequest;
import com.tourism.entity.ScenicSpot;
import com.tourism.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 推荐控制器
 */
@RestController
@RequestMapping("/api/recommend")
@RequiredArgsConstructor
public class RecommendationController {

    private final RecommendationService recommendationService;

    /**
     * 获取推荐景点
     */
    @GetMapping("/scenic")
    public Result<List<ScenicSpot>> getRecommendedScenic(
            @RequestParam(defaultValue = "10") Integer limit
    ) {
        Long userId = null;
        if (StpUtil.isLogin()) {
            userId = StpUtil.getLoginIdAsLong();
        }
        
        List<ScenicSpot> recommendations = recommendationService.getRecommendations(userId, limit);
        return Result.success(recommendations);
    }

    /**
     * 记录用户行为
     */
    @PostMapping("/interaction")
    public Result<Void> recordInteraction(@RequestBody InteractionRequest request) {
        Long userId = StpUtil.getLoginIdAsLong();
        recommendationService.recordInteraction(
                userId,
                request.getTargetType(),
                request.getTargetId(),
                request.getActionType()
        );
        return Result.success();
    }
}
