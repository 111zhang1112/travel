package com.tourism.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tourism.common.Result;
import com.tourism.entity.Banner;
import com.tourism.entity.Notice;
import com.tourism.entity.TravelRoute;
import com.tourism.service.BannerService;
import com.tourism.service.NoticeService;
import com.tourism.service.TravelRouteService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 公开系统配置控制器
 * 提供首页轮播图、公告、路线等公开数据
 */
@RestController
@RequestMapping("/api/system")
@RequiredArgsConstructor
public class SystemController {

    private final BannerService bannerService;
    private final NoticeService noticeService;
    private final TravelRouteService travelRouteService;

    /**
     * 获取启用的轮播图列表
     */
    @GetMapping("/banners")
    public Result<List<Banner>> getBanners() {
        LambdaQueryWrapper<Banner> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Banner::getStatus, 1);
        wrapper.orderByAsc(Banner::getSort);
        return Result.success(bannerService.list(wrapper));
    }

    /**
     * 获取启用的公告列表
     */
    @GetMapping("/notices")
    public Result<List<Notice>> getNotices() {
        LambdaQueryWrapper<Notice> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notice::getStatus, 1);
        wrapper.orderByDesc(Notice::getCreateTime);
        wrapper.last("LIMIT 10");
        return Result.success(noticeService.list(wrapper));
    }

    /**
     * 获取精选路线（首页展示）
     */
    @GetMapping("/routes/featured")
    public Result<List<TravelRoute>> getFeaturedRoutes(@RequestParam(defaultValue = "4") Integer limit) {
        LambdaQueryWrapper<TravelRoute> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TravelRoute::getStatus, 1);
        wrapper.orderByAsc(TravelRoute::getSort);
        wrapper.last("LIMIT " + limit);
        return Result.success(travelRouteService.list(wrapper));
    }

    /**
     * 获取路线列表（分页）
     */
    @GetMapping("/routes")
    public Result<Page<TravelRoute>> getRoutes(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "12") Integer size
    ) {
        Page<TravelRoute> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<TravelRoute> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TravelRoute::getStatus, 1);
        wrapper.orderByAsc(TravelRoute::getSort);
        return Result.success(travelRouteService.page(pageParam, wrapper));
    }

    /**
     * 获取路线详情
     */
    @GetMapping("/routes/{id}")
    public Result<TravelRoute> getRouteDetail(@PathVariable Long id) {
        TravelRoute route = travelRouteService.getById(id);
        if (route == null || route.getStatus() != 1) {
            return Result.error(404, "路线不存在");
        }
        return Result.success(route);
    }

    /**
     * 获取热门搜索关键词
     */
    @GetMapping("/hot-keywords")
    public Result<List<java.util.Map<String, Object>>> getHotKeywords() {
        // 返回热门关键词列表（可以从数据库统计，这里先返回静态数据）
        List<java.util.Map<String, Object>> keywords = new java.util.ArrayList<>();
        keywords.add(java.util.Map.of("keyword", "西湖", "count", 1250));
        keywords.add(java.util.Map.of("keyword", "千岛湖", "count", 980));
        keywords.add(java.util.Map.of("keyword", "普陀山", "count", 856));
        keywords.add(java.util.Map.of("keyword", "乌镇", "count", 742));
        keywords.add(java.util.Map.of("keyword", "雁荡山", "count", 635));
        keywords.add(java.util.Map.of("keyword", "莫干山", "count", 521));
        return Result.success(keywords);
    }
}
