package com.tourism.controller;

import com.tourism.common.PageResult;
import com.tourism.common.Result;
import com.tourism.dto.CreateOrderRequest;
import com.tourism.entity.Order;
import com.tourism.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 订单控制器
 */
@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/create")
    public Result<Order> create(@Valid @RequestBody CreateOrderRequest request) {
        Order order = orderService.createOrder(request);
        return Result.success(order);
    }

    @GetMapping("/list")
    public Result<PageResult<Order>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String status
    ) {
        PageResult<Order> result = orderService.getUserOrders(page, size, status);
        return Result.success(result);
    }

    @GetMapping("/detail/{orderNo}")
    public Result<Order> detail(@PathVariable String orderNo) {
        Order order = orderService.getByOrderNo(orderNo);
        return Result.success(order);
    }

    @PostMapping("/pay/{orderNo}")
    public Result<Order> pay(@PathVariable String orderNo) {
        Order order = orderService.payOrder(orderNo);
        return Result.success(order);
    }

    @PostMapping("/cancel/{orderNo}")
    public Result<Order> cancel(@PathVariable String orderNo) {
        Order order = orderService.cancelOrder(orderNo);
        return Result.success(order);
    }
}
