package com.tourism.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tourism.common.Result;
import com.tourism.entity.Banner;
import com.tourism.entity.Category;
import com.tourism.entity.Notice;
import com.tourism.entity.TravelRoute;
import com.tourism.service.BannerService;
import com.tourism.service.CategoryService;
import com.tourism.service.NoticeService;
import com.tourism.service.TravelRouteService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 系统配置管理控制器
 */
@RestController
@RequestMapping("/api/admin/system")
@RequiredArgsConstructor
public class AdminSystemController {

    private final BannerService bannerService;
    private final NoticeService noticeService;
    private final CategoryService categoryService;
    private final TravelRouteService travelRouteService;

    // ==================== 轮播图管理 ====================
    
    /**
     * 轮播图列表
     */
    @GetMapping("/banner/list")
    public Result<Page<Banner>> bannerList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size
    ) {
        Page<Banner> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Banner> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(Banner::getSort).orderByDesc(Banner::getCreateTime);
        
        Page<Banner> result = bannerService.page(pageParam, wrapper);
        return Result.success(result);
    }

    /**
     * 创建轮播图
     */
    @PostMapping("/banner")
    public Result<Banner> createBanner(@RequestBody Banner banner) {
        bannerService.save(banner);
        return Result.success(banner);
    }

    /**
     * 更新轮播图
     */
    @PutMapping("/banner/{id}")
    public Result<Void> updateBanner(@PathVariable Long id, @RequestBody Banner banner) {
        banner.setId(id);
        bannerService.updateById(banner);
        return Result.success();
    }

    /**
     * 删除轮播图
     */
    @DeleteMapping("/banner/{id}")
    public Result<Void> deleteBanner(@PathVariable Long id) {
        bannerService.removeById(id);
        return Result.success();
    }

    // ==================== 公告管理 ====================
    
    /**
     * 公告列表
     */
    @GetMapping("/notice/list")
    public Result<Page<Notice>> noticeList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String type
    ) {
        Page<Notice> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Notice> wrapper = new LambdaQueryWrapper<>();
        
        if (type != null && !type.isEmpty()) {
            wrapper.eq(Notice::getType, type);
        }
        
        wrapper.orderByDesc(Notice::getCreateTime);
        Page<Notice> result = noticeService.page(pageParam, wrapper);
        return Result.success(result);
    }

    /**
     * 创建公告
     */
    @PostMapping("/notice")
    public Result<Notice> createNotice(@RequestBody Notice notice) {
        noticeService.save(notice);
        return Result.success(notice);
    }

    /**
     * 更新公告
     */
    @PutMapping("/notice/{id}")
    public Result<Void> updateNotice(@PathVariable Long id, @RequestBody Notice notice) {
        notice.setId(id);
        noticeService.updateById(notice);
        return Result.success();
    }

    /**
     * 删除公告
     */
    @DeleteMapping("/notice/{id}")
    public Result<Void> deleteNotice(@PathVariable Long id) {
        noticeService.removeById(id);
        return Result.success();
    }

    /**
     * 发布公告
     */
    @PutMapping("/notice/{id}/publish")
    public Result<Void> publishNotice(@PathVariable Long id) {
        Notice notice = noticeService.getById(id);
        if (notice == null) {
            return Result.error(404, "公告不存在");
        }
        notice.setStatus(1);
        noticeService.updateById(notice);
        return Result.success();
    }

    // ==================== 分类管理 ====================
    
    /**
     * 分类列表
     */
    @GetMapping("/category/list")
    public Result<List<Category>> categoryList(@RequestParam(required = false) String type) {
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        if (type != null && !type.isEmpty()) {
            wrapper.eq(Category::getType, type);
        }
        wrapper.orderByAsc(Category::getSort).orderByDesc(Category::getCreateTime);
        return Result.success(categoryService.list(wrapper));
    }

    /**
     * 创建分类
     */
    @PostMapping("/category")
    public Result<Category> createCategory(@RequestBody Category category) {
        categoryService.save(category);
        return Result.success(category);
    }

    /**
     * 更新分类
     */
    @PutMapping("/category/{id}")
    public Result<Void> updateCategory(@PathVariable Long id, @RequestBody Category category) {
        category.setId(id);
        categoryService.updateById(category);
        return Result.success();
    }

    /**
     * 删除分类
     */
    @DeleteMapping("/category/{id}")
    public Result<Void> deleteCategory(@PathVariable Long id) {
        categoryService.removeById(id);
        return Result.success();
    }

    // ==================== 路线管理 ====================
    
    /**
     * 路线列表
     */
    @GetMapping("/route/list")
    public Result<Page<TravelRoute>> routeList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size
    ) {
        Page<TravelRoute> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<TravelRoute> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(TravelRoute::getSort).orderByDesc(TravelRoute::getCreateTime);
        return Result.success(travelRouteService.page(pageParam, wrapper));
    }

    /**
     * 创建路线
     */
    @PostMapping("/route")
    public Result<TravelRoute> createRoute(@RequestBody TravelRoute route) {
        travelRouteService.save(route);
        return Result.success(route);
    }

    /**
     * 更新路线
     */
    @PutMapping("/route/{id}")
    public Result<Void> updateRoute(@PathVariable Long id, @RequestBody TravelRoute route) {
        route.setId(id);
        travelRouteService.updateById(route);
        return Result.success();
    }

    /**
     * 删除路线
     */
    @DeleteMapping("/route/{id}")
    public Result<Void> deleteRoute(@PathVariable Long id) {
        travelRouteService.removeById(id);
        return Result.success();
    }

    /**
     * 更新路线状态
     */
    @PutMapping("/route/{id}/status")
    public Result<Void> updateRouteStatus(@PathVariable Long id, @RequestParam Integer status) {
        TravelRoute route = travelRouteService.getById(id);
        if (route == null) {
            return Result.error(404, "路线不存在");
        }
        route.setStatus(status);
        travelRouteService.updateById(route);
        return Result.success();
    }
}
