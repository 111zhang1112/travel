package com.tourism.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tourism.common.BusinessException;
import com.tourism.common.PageResult;
import com.tourism.common.Result;
import com.tourism.entity.TravelRoute;
import com.tourism.service.TravelRouteService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 旅游路线控制器
 */
@RestController
@RequestMapping("/api/travel-routes")
@RequiredArgsConstructor
public class TravelRouteController {

    private final TravelRouteService routeService;

    /**
     * 获取路线列表(分页)
     */
    @GetMapping
    public Result<PageResult<TravelRoute>> getRoutes(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        
        QueryWrapper<TravelRoute> wrapper = new QueryWrapper<>();
        wrapper.eq("status", 1)  // 只显示已发布的路线
                .orderByAsc("sort")
                .orderByDesc("create_time");
        
        Page<TravelRoute> pageParam = new Page<>(page, size);
        Page<TravelRoute> result = routeService.page(pageParam, wrapper);
        
        return Result.success(new PageResult<>(
                result.getRecords(),
                result.getTotal(),
                result.getCurrent(),
                result.getSize()
        ));
    }

    /**
     * 获取路线详情
     */
    @GetMapping("/{id}")
    public Result<TravelRoute> getRouteDetail(@PathVariable Long id) {
        TravelRoute route = routeService.getById(id);
        if (route == null) {
            throw new BusinessException(404, "路线不存在");
        }
        return Result.success(route);
    }

    /**
     * 获取精选路线
     */
    @GetMapping("/featured")
    public Result<List<TravelRoute>> getFeaturedRoutes(
            @RequestParam(defaultValue = "4") Integer limit) {
        
        QueryWrapper<TravelRoute> wrapper = new QueryWrapper<>();
        wrapper.eq("status", 1)
                .orderByAsc("sort")
                .orderByDesc("create_time")
                .last("LIMIT " + limit);
        
        List<TravelRoute> routes = routeService.list(wrapper);
        return Result.success(routes);
    }
}
