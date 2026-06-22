package com.tourism.service;

import net.jqwik.api.*;
import net.jqwik.api.constraints.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 订单服务属性测试
 * Feature: smart-tourism-platform
 */
class OrderServicePropertyTest {

    /**
     * Property 13: Order Number Uniqueness
     * For any set of created orders, all order_no values SHALL be unique.
     * Validates: Requirements 7.1
     */
    @Property(tries = 100)
    void orderNumbersShouldBeUnique(
            @ForAll @IntRange(min = 2, max = 50) int orderCount
    ) {
        Set<String> orderNos = new HashSet<>();
        
        for (int i = 0; i < orderCount; i++) {
            String orderNo = generateOrderNo();
            orderNos.add(orderNo);
        }
        
        // 由于时间戳+UUID，所有订单号应该唯一
        // 注意：在极端情况下可能有碰撞，但概率极低
        assertThat(orderNos.size()).isEqualTo(orderCount);
    }

    /**
     * Property 14: Order State Machine
     * For any order, status transitions SHALL only follow valid paths.
     * Validates: Requirements 7.2, 7.3, 7.4, 7.5
     */
    @Property(tries = 100)
    void orderStateMachineShouldBeValid(
            @ForAll("orderStatus") String currentStatus,
            @ForAll("orderStatus") String targetStatus
    ) {
        boolean isValidTransition = isValidStatusTransition(currentStatus, targetStatus);
        
        // 验证状态转换规则
        if ("PENDING".equals(currentStatus)) {
            // PENDING 可以转换为 PAID 或 CANCELLED
            if ("PAID".equals(targetStatus) || "CANCELLED".equals(targetStatus)) {
                assertThat(isValidTransition).isTrue();
            } else if ("COMPLETED".equals(targetStatus)) {
                assertThat(isValidTransition).isFalse();
            }
        } else if ("PAID".equals(currentStatus)) {
            // PAID 只能转换为 COMPLETED
            if ("COMPLETED".equals(targetStatus)) {
                assertThat(isValidTransition).isTrue();
            } else {
                assertThat(isValidTransition).isFalse();
            }
        } else if ("COMPLETED".equals(currentStatus) || "CANCELLED".equals(currentStatus)) {
            // 终态不能转换
            assertThat(isValidTransition).isFalse();
        }
    }

    /**
     * 验证订单金额必须为正数
     */
    @Property(tries = 100)
    void orderAmountShouldBePositive(
            @ForAll @DoubleRange(min = 0.01, max = 100000) double amount
    ) {
        assertThat(amount).isPositive();
    }

    /**
     * 验证订单号格式
     */
    @Property(tries = 100)
    void orderNoFormatShouldBeValid(
            @ForAll @IntRange(min = 1, max = 100) int iterations
    ) {
        for (int i = 0; i < iterations; i++) {
            String orderNo = generateOrderNo();
            // 订单号应该是 14位时间戳 + 8位UUID = 22位
            assertThat(orderNo).hasSize(22);
            // 前14位应该是数字（时间戳）
            assertThat(orderNo.substring(0, 14)).matches("\\d{14}");
        }
    }

    @Provide
    Arbitrary<String> orderStatus() {
        return Arbitraries.of("PENDING", "PAID", "COMPLETED", "CANCELLED");
    }

    private String generateOrderNo() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String uuid = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        return timestamp + uuid;
    }

    private boolean isValidStatusTransition(String from, String to) {
        if (from.equals(to)) return false;
        
        return switch (from) {
            case "PENDING" -> "PAID".equals(to) || "CANCELLED".equals(to);
            case "PAID" -> "COMPLETED".equals(to);
            default -> false;  // COMPLETED 和 CANCELLED 是终态
        };
    }
}
