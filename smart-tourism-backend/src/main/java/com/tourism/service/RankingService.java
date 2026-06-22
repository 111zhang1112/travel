package com.tourism.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tourism.entity.Hotel;
import com.tourism.entity.ScenicSpot;
import com.tourism.mapper.HotelMapper;
import com.tourism.mapper.ScenicSpotMapper;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * 排行榜服务
 */
@Service
@RequiredArgsConstructor
public class RankingService {
    
    private final ScenicSpotMapper scenicSpotMapper;
    private final HotelMapper hotelMapper;
    
    /**
     * 获取景点热门榜
     */
    public List<ScenicSpot> getScenicRanking(int limit, String period) {
        // 按浏览量、评分综合排序
        return scenicSpotMapper.selectList(new LambdaQueryWrapper<ScenicSpot>()
                .eq(ScenicSpot::getStatus, 1)
                .orderByDesc(ScenicSpot::getViewCount)
                .orderByDesc(ScenicSpot::getRating)
                .last("LIMIT " + limit));
    }
    
    /**
     * 获取酒店热门榜
     */
    public List<Hotel> getHotelRanking(int limit, String period) {
        // 按评分、星级排序
        return hotelMapper.selectList(new LambdaQueryWrapper<Hotel>()
                .eq(Hotel::getStatus, 1)
                .orderByDesc(Hotel::getRating)
                .orderByDesc(Hotel::getStarRating)
                .last("LIMIT " + limit));
    }
    
    /**
     * 获取综合排行数据
     */
    public RankingData getRankingData() {
        RankingData data = new RankingData();
        data.setScenicRanking(getScenicRanking(10, "all"));
        data.setHotelRanking(getHotelRanking(10, "all"));
        return data;
    }
    
    @Data
    public static class RankingData {
        private List<ScenicSpot> scenicRanking;
        private List<Hotel> hotelRanking;
    }
}
