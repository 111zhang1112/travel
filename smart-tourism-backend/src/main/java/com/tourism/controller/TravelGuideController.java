package com.tourism.controller;

import com.tourism.common.PageResult;
import com.tourism.common.Result;
import com.tourism.entity.TravelGuide;
import com.tourism.service.TravelGuideService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 旅游攻略控制器
 */
@RestController
@RequestMapping("/api/guide")
@RequiredArgsConstructor
public class TravelGuideController {

    private final TravelGuideService travelGuideService;

    @GetMapping("/list")
    public Result<PageResult<TravelGuide>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword
    ) {
        PageResult<TravelGuide> result = travelGuideService.getPublicList(page, size, keyword);
        return Result.success(result);
    }

    @GetMapping("/detail/{id}")
    public Result<TravelGuide> detail(@PathVariable Long id) {
        TravelGuide guide = travelGuideService.getDetail(id);
        return Result.success(guide);
    }

    @PostMapping("/create")
    public Result<TravelGuide> create(@RequestBody TravelGuide guide) {
        TravelGuide result = travelGuideService.create(guide);
        return Result.success(result);
    }

    @PutMapping("/update/{id}")
    public Result<TravelGuide> update(@PathVariable Long id, @RequestBody TravelGuide guide) {
        TravelGuide result = travelGuideService.update(id, guide);
        return Result.success(result);
    }
}
