package com.tourism.controller.admin;

import com.tourism.common.PageResult;
import com.tourism.common.Result;
import com.tourism.entity.TravelGuide;
import com.tourism.service.TravelGuideService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 攻略管理控制器
 */
@RestController
@RequestMapping("/api/admin/guide")
@RequiredArgsConstructor
public class AdminGuideController {

    private final TravelGuideService travelGuideService;

    @GetMapping("/list")
    public Result<PageResult<TravelGuide>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer status
    ) {
        PageResult<TravelGuide> result = travelGuideService.getAdminList(page, size, status);
        return Result.success(result);
    }

    @PostMapping("/approve/{id}")
    public Result<Void> approve(@PathVariable Long id) {
        travelGuideService.approve(id);
        return Result.success();
    }

    @PostMapping("/reject/{id}")
    public Result<Void> reject(@PathVariable Long id) {
        travelGuideService.reject(id);
        return Result.success();
    }
}
