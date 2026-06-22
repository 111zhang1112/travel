package com.tourism.controller.admin;

import com.tourism.common.PageResult;
import com.tourism.common.Result;
import com.tourism.entity.Order;
import com.tourism.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 订单管理控制器
 */
@RestController
@RequestMapping("/api/admin/order")
@RequiredArgsConstructor
public class AdminOrderController {

    private final OrderService orderService;

    @GetMapping("/list")
    public Result<PageResult<Order>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String orderNo
    ) {
        PageResult<Order> result = orderService.getAdminOrders(page, size, status, orderNo);
        return Result.success(result);
    }

    @PostMapping("/complete/{orderNo}")
    public Result<Order> complete(@PathVariable String orderNo) {
        Order order = orderService.completeOrder(orderNo);
        return Result.success(order);
    }
}
