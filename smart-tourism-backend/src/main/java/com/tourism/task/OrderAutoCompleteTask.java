package com.tourism.task;

import com.tourism.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 订单自动完成定时任务
 * 每天凌晨2点执行，将已支付超过15天的订单自动设置为已完成
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderAutoCompleteTask {

    private final OrderService orderService;

    /**
     * 自动完成订单天数
     */
    private static final int AUTO_COMPLETE_DAYS = 15;

    /**
     * 每天凌晨2点执行
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void autoCompleteOrders() {
        log.info("开始执行订单自动完成任务...");
        try {
            int count = orderService.autoCompleteOrders(AUTO_COMPLETE_DAYS);
            log.info("订单自动完成任务执行完毕，共完成 {} 个订单", count);
        } catch (Exception e) {
            log.error("订单自动完成任务执行失败", e);
        }
    }
}
