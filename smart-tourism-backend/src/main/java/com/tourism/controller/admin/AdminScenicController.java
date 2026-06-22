package com.tourism.controller.admin;

import com.tourism.common.PageResult;
import com.tourism.common.Result;
import com.tourism.entity.Category;
import com.tourism.entity.ScenicSpot;
import com.tourism.service.ScenicSpotService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 景点管理控制器（管理端）
 */
@RestController
@RequestMapping("/api/admin/scenic")
@RequiredArgsConstructor
public class AdminScenicController {

    private final ScenicSpotService scenicSpotService;

    /**
     * 分页查询景点列表（支持分类筛选）
     */
    @GetMapping("/list")
    public Result<PageResult<ScenicSpot>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Long categoryId
    ) {
        PageResult<ScenicSpot> result = scenicSpotService.getAdminList(page, size, keyword, status, categoryId);
        return Result.success(result);
    }
    
    /**
     * 更新景点状态（上架/下架）
     */
    @PutMapping("/updateStatus/{id}")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        scenicSpotService.updateStatus(id, status);
        return Result.success();
    }
    
    /**
     * 获取景点分类列表
     */
    @GetMapping("/categories")
    public Result<List<Category>> getCategories() {
        List<Category> categories = scenicSpotService.getScenicCategories();
        return Result.success(categories);
    }

    /**
     * 创建景点
     */
    @PostMapping("/create")
    public Result<ScenicSpot> create(@RequestBody ScenicSpot spot) {
        ScenicSpot result = scenicSpotService.create(spot);
        return Result.success(result);
    }

    /**
     * 更新景点
     */
    @PutMapping("/update/{id}")
    public Result<ScenicSpot> update(@PathVariable Long id, @RequestBody ScenicSpot spot) {
        ScenicSpot result = scenicSpotService.update(id, spot);
        return Result.success(result);
    }

    /**
     * 删除景点（软删除）
     */
    @DeleteMapping("/delete/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        scenicSpotService.softDelete(id);
        return Result.success();
    }
}
