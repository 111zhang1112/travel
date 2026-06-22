package com.tourism.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.stp.StpUtil;
import com.tourism.common.Result;
import com.tourism.service.MerchantStatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 商家数据统计控制器
 */
@RestController
@RequestMapping("/api/merchant/stats")
@RequiredArgsConstructor
@SaCheckRole("MERCHANT")
public class MerchantStatsController {

    private final MerchantStatsService merchantStatsService;

    /**
     * 获取经营概况
     */
    @GetMapping("/overview")
    public Result<Map<String, Object>> getOverview() {
        Long merchantId = StpUtil.getLoginIdAsLong();
        Map<String, Object> overview = merchantStatsService.getMerchantOverview(merchantId);
        return Result.success(overview);
    }

    /**
     * 获取订单趋势（最近12个月）
     */
    @GetMapping("/order-trend")
    public Result<List<Map<String, Object>>> getOrderTrend() {
        Long merchantId = StpUtil.getLoginIdAsLong();
        List<Map<String, Object>> trend = merchantStatsService.getMerchantOrderTrend(merchantId);
        return Result.success(trend);
    }

    /**
     * 获取收入趋势
     * @param period 时间周期：day(最近7天), week(最近12周), month(最近12个月)
     */
    @GetMapping("/revenue-trend")
    public Result<List<Map<String, Object>>> getRevenueTrend(
            @RequestParam(defaultValue = "month") String period
    ) {
        Long merchantId = StpUtil.getLoginIdAsLong();
        List<Map<String, Object>> trend = merchantStatsService.getMerchantRevenueTrend(merchantId, period);
        return Result.success(trend);
    }

    /**
     * 获取产品销量排名
     * @param limit 返回数量，默认10
     */
    @GetMapping("/product-ranking")
    public Result<List<Map<String, Object>>> getProductRanking(
            @RequestParam(required = false) Integer limit
    ) {
        Long merchantId = StpUtil.getLoginIdAsLong();
        List<Map<String, Object>> ranking = merchantStatsService.getMerchantProductRanking(merchantId, limit);
        return Result.success(ranking);
    }
}
