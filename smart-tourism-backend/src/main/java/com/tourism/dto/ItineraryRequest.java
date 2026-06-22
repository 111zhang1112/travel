package com.tourism.dto;

import lombok.Data;

/**
 * 行程规划请求
 */
@Data
public class ItineraryRequest {
    private String destination;  // 目的地
    private Integer days;        // 天数
    private String preferences;  // 偏好（美食、文化、自然等）
    private Long userId;
}
