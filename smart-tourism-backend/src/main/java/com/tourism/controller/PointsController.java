package com.tourism.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tourism.common.Result;
import com.tourism.entity.PointsRecord;
import com.tourism.service.PointsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 积分控制器
 */
@RestController
@RequestMapping("/api/points")
@RequiredArgsConstructor
public class PointsController {
    
    private final PointsService pointsService;
    
    /**
     * 获取我的积分余额
     */
    @GetMapping("/balance")
    public Result<Integer> getBalance() {
        Long userId = StpUtil.getLoginIdAsLong();
        Integer points = pointsService.getUserPoints(userId);
        return Result.success(points);
    }
    
    /**
     * 获取积分记录
     */
    @GetMapping("/records")
    public Result<Page<PointsRecord>> getRecords(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = StpUtil.getLoginIdAsLong();
        Page<PointsRecord> result = pointsService.getPointsRecords(userId, page, size);
        return Result.success(result);
    }
}
