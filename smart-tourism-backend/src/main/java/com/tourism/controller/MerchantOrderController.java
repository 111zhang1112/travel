package com.tourism.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.stp.StpUtil;
import com.tourism.common.PageResult;
import com.tourism.common.Result;
import com.tourism.entity.Order;
import com.tourism.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 商家订单管理控制器
 */
@RestController
@RequestMapping("/api/merchant/orders")
@RequiredArgsConstructor
@SaCheckRole("MERCHANT")
public class MerchantOrderController {

    private final OrderService orderService;

    /**
     * 获取商家订单列表
     */
    @GetMapping
    public Result<PageResult<Order>> getMerchantOrders(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String orderNo
    ) {
        Long merchantId = StpUtil.getLoginIdAsLong();
        PageResult<Order> result = orderService.getMerchantOrders(merchantId, page, size, status, orderNo);
        return Result.success(result);
    }

    /**
     * 获取订单详情
     */
    @GetMapping("/{orderNo}")
    public Result<Order> getOrderDetail(@PathVariable String orderNo) {
        Long merchantId = StpUtil.getLoginIdAsLong();
        Order order = orderService.getMerchantOrderDetail(merchantId, orderNo);
        return Result.success(order);
    }

    /**
     * 获取订单统计
     */
    @GetMapping("/stats")
    public Result<Map<String, Object>> getOrderStats() {
        Long merchantId = StpUtil.getLoginIdAsLong();
        Map<String, Object> stats = orderService.getMerchantOrderStats(merchantId);
        return Result.success(stats);
    }
}
