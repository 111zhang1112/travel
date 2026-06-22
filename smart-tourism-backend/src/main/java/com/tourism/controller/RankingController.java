package com.tourism.controller;

import com.tourism.common.Result;
import com.tourism.entity.Hotel;
import com.tourism.entity.ScenicSpot;
import com.tourism.service.RankingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 排行榜控制器
 */
@RestController
@RequestMapping("/api/ranking")
@RequiredArgsConstructor
public class RankingController {
    
    private final RankingService rankingService;
    
    /**
     * 获取景点热门榜
     */
    @GetMapping("/scenic")
    public Result<List<ScenicSpot>> getScenicRanking(
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "all") String period) {
        List<ScenicSpot> ranking = rankingService.getScenicRanking(limit, period);
        return Result.success(ranking);
    }
    
    /**
     * 获取酒店热门榜
     */
    @GetMapping("/hotel")
    public Result<List<Hotel>> getHotelRanking(
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "all") String period) {
        List<Hotel> ranking = rankingService.getHotelRanking(limit, period);
        return Result.success(ranking);
    }
    
    /**
     * 获取综合排行数据
     */
    @GetMapping("/all")
    public Result<RankingService.RankingData> getAllRanking() {
        RankingService.RankingData data = rankingService.getRankingData();
        return Result.success(data);
    }
}
