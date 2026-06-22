package com.tourism.controller;

import com.tourism.common.PageResult;
import com.tourism.common.Result;
import com.tourism.entity.Category;
import com.tourism.entity.ScenicSpot;
import com.tourism.service.ScenicSpotService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 景点控制器（公开接口）
 */
@RestController
@RequestMapping("/api/scenic")
@RequiredArgsConstructor
public class ScenicSpotController {

    private final ScenicSpotService scenicSpotService;

    /**
     * 分页查询景点列表
     */
    @GetMapping("/list")
    public Result<PageResult<ScenicSpot>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) java.math.BigDecimal priceMin,
            @RequestParam(required = false) java.math.BigDecimal priceMax,
            @RequestParam(required = false) String region,
            @RequestParam(required = false) Double minRating,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String sortBy
    ) {
        PageResult<ScenicSpot> result = scenicSpotService.getPublicList(page, size, keyword, priceMin, priceMax, region, minRating, categoryId, sortBy);
        return Result.success(result);
    }

    /**
     * 搜索景点
     */
    @GetMapping("/search")
    public Result<List<ScenicSpot>> search(@RequestParam String keyword) {
        List<ScenicSpot> result = scenicSpotService.search(keyword);
        return Result.success(result);
    }

    /**
     * 获取景点详情
     */
    @GetMapping("/detail/{id}")
    public Result<ScenicSpot> detail(@PathVariable Long id) {
        ScenicSpot spot = scenicSpotService.getDetail(id);
        return Result.success(spot);
    }

    /**
     * 获取热门景点
     */
    @GetMapping("/popular")
    public Result<List<ScenicSpot>> popular(
            @RequestParam(defaultValue = "10") Integer limit
    ) {
        List<ScenicSpot> result = scenicSpotService.getPopular(limit);
        return Result.success(result);
    }

    /**
     * 获取高评分景点
     */
    @GetMapping("/top-rated")
    public Result<List<ScenicSpot>> topRated(
            @RequestParam(defaultValue = "10") Integer limit
    ) {
        List<ScenicSpot> result = scenicSpotService.getTopRated(limit);
        return Result.success(result);
    }

    /**
     * 按地区查询景点（使用location字段）
     */
    @GetMapping("/by-region")
    public Result<List<ScenicSpot>> getByRegion(
            @RequestParam String region,
            @RequestParam(defaultValue = "10") Integer limit
    ) {
        List<ScenicSpot> result = scenicSpotService.getByRegion(region, limit);
        return Result.success(result);
    }

    /**
     * 获取所有地区列表（从location字段）
     */
    @GetMapping("/regions")
    public Result<List<String>> getRegions() {
        List<String> regions = scenicSpotService.getAllRegions();
        return Result.success(regions);
    }
    
    /**
     * 获取景点分类列表（公开接口）
     */
    @GetMapping("/categories")
    public Result<List<Category>> getCategories() {
        List<Category> categories = scenicSpotService.getScenicCategories();
        return Result.success(categories);
    }
}
