package com.tourism.service;

import com.tourism.mapper.OrderMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 商家数据统计服务
 */
@Service
@RequiredArgsConstructor
public class MerchantStatsService {

    private final OrderMapper orderMapper;

    /**
     * 获取商家经营概况
     */
    public Map<String, Object> getMerchantOverview(Long merchantId) {
        return orderMapper.getMerchantOrderStats(merchantId);
    }

    /**
     * 获取商家订单趋势（最近12个月）
     */
    public List<Map<String, Object>> getMerchantOrderTrend(Long merchantId) {
        return orderMapper.getMerchantOrderTrend(merchantId);
    }

    /**
     * 获取商家收入趋势
     * @param merchantId 商家ID
     * @param period 时间周期：day(最近7天), week(最近12周), month(最近12个月)
     */
    public List<Map<String, Object>> getMerchantRevenueTrend(Long merchantId, String period) {
        switch (period) {
            case "day":
                return orderMapper.getMerchantRevenueTrendByDay(merchantId);
            case "week":
                return orderMapper.getMerchantRevenueTrendByWeek(merchantId);
            case "month":
            default:
                return orderMapper.getMerchantRevenueTrendByMonth(merchantId);
        }
    }

    /**
     * 获取商家产品销量排名
     */
    public List<Map<String, Object>> getMerchantProductRanking(Long merchantId, Integer limit) {
        return orderMapper.getMerchantProductRanking(merchantId, limit != null ? limit : 10);
    }
}
