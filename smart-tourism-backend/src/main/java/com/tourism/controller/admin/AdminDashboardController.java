package com.tourism.controller.admin;

import com.tourism.common.Result;
import com.tourism.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 数据看板控制器
 */
@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final ScenicSpotService scenicSpotService;
    private final HotelService hotelService;
    private final TravelGuideService guideService;
    private final OrderService orderService;
    private final UserService userService;
    private final ReviewService reviewService;

    /**
     * 获取统计数据
     */
    @GetMapping("/stats")
    public Result<Map<String, Object>> getStats() {
        Map<String, Object> stats = new HashMap<>();
        
        // 统计各模块数量
        stats.put("scenicCount", scenicSpotService.count());
        stats.put("hotelCount", hotelService.count());
        stats.put("guideCount", guideService.count());
        stats.put("orderCount", orderService.count());
        stats.put("userCount", userService.count());
        stats.put("reviewCount", reviewService.count());
        
        // 今日订单数
        stats.put("todayOrderCount", orderService.getTodayOrderCount());
        
        // 待处理订单数
        stats.put("pendingOrderCount", orderService.getPendingOrderCount());
        
        // 总收入（已支付和已完成的订单）
        stats.put("totalRevenue", orderService.getTotalRevenue());
        
        // 今日收入
        stats.put("todayRevenue", orderService.getTodayRevenue());
        
        // 订单状态统计
        stats.put("paidOrderCount", orderService.countByStatus("PAID"));
        stats.put("completedOrderCount", orderService.countByStatus("COMPLETED"));
        stats.put("cancelledOrderCount", orderService.countByStatus("CANCELLED"));
        
        return Result.success(stats);
    }

    /**
     * 获取订单趋势（最近12个月）
     */
    @GetMapping("/order-trend")
    public Result<List<Map<String, Object>>> getOrderTrend() {
        return Result.success(orderService.getOrderTrend());
    }

    /**
     * 获取订单类型分布
     */
    @GetMapping("/order-distribution")
    public Result<List<Map<String, Object>>> getOrderTypeDistribution() {
        return Result.success(orderService.getOrderTypeDistribution());
    }
    
    /**
     * 获取用户增长趋势（最近12个月）
     */
    @GetMapping("/user-growth")
    public Result<List<Map<String, Object>>> getUserGrowth() {
        return Result.success(userService.getUserGrowthTrend());
    }
    
    /**
     * 获取收入趋势
     * @param period 时间周期：day(最近7天), week(最近12周), month(最近12个月)
     */
    @GetMapping("/revenue-trend")
    public Result<List<Map<String, Object>>> getRevenueTrend(
            @RequestParam(defaultValue = "month") String period) {
        return Result.success(orderService.getRevenueTrend(period));
    }
    
    /**
     * 获取热门景点TOP10
     */
    @GetMapping("/top-scenic")
    public Result<List<Map<String, Object>>> getTopScenic() {
        return Result.success(scenicSpotService.getTopScenic(10));
    }
    
    /**
     * 获取热门酒店TOP10
     */
    @GetMapping("/top-hotels")
    public Result<List<Map<String, Object>>> getTopHotels() {
        return Result.success(hotelService.getTopHotels(10));
    }
    
    /**
     * 获取订单状态分布
     */
    @GetMapping("/order-status-distribution")
    public Result<List<Map<String, Object>>> getOrderStatusDistribution() {
        return Result.success(orderService.getOrderStatusDistribution());
    }
    
    /**
     * 获取综合统计（用于雷达图）
     */
    @GetMapping("/comprehensive-stats")
    public Result<Map<String, Object>> getComprehensiveStats() {
        Map<String, Object> stats = new HashMap<>();
        
        // 计算各项指标（0-100分）
        long totalUsers = userService.count();
        long totalOrders = orderService.count();
        long totalReviews = reviewService.count();
        long totalScenic = scenicSpotService.count();
        long totalHotels = hotelService.count();
        
        // 用户活跃度（基于订单数/用户数比例）
        double userActivity = totalUsers > 0 ? Math.min((double) totalOrders / totalUsers * 20, 100) : 0;
        
        // 内容丰富度（基于景点和酒店数量）
        double contentRichness = Math.min((totalScenic + totalHotels) / 2.0, 100);
        
        // 用户满意度（基于评价数量）
        double userSatisfaction = totalOrders > 0 ? Math.min((double) totalReviews / totalOrders * 100, 100) : 0;
        
        // 订单转化率（已支付订单/总订单）
        long paidOrders = orderService.countByStatus("PAID") + orderService.countByStatus("COMPLETED");
        double conversionRate = totalOrders > 0 ? (double) paidOrders / totalOrders * 100 : 0;
        
        // 平台活跃度（综合指标）
        double platformActivity = (userActivity + contentRichness + userSatisfaction + conversionRate) / 4;
        
        stats.put("userActivity", Math.round(userActivity));
        stats.put("contentRichness", Math.round(contentRichness));
        stats.put("userSatisfaction", Math.round(userSatisfaction));
        stats.put("conversionRate", Math.round(conversionRate));
        stats.put("platformActivity", Math.round(platformActivity));
        
        return Result.success(stats);
    }
}
