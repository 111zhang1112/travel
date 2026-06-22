package com.tourism.controller.admin;

import com.tourism.common.PageResult;
import com.tourism.common.Result;
import com.tourism.entity.TravelCompanion;
import com.tourism.service.TravelCompanionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 结伴管理控制器（管理端）
 */
@RestController
@RequestMapping("/api/admin/companion")
@RequiredArgsConstructor
public class AdminCompanionController {

    private final TravelCompanionService companionService;

    /**
     * 分页查询结伴
     */
    @GetMapping("/list")
    public Result<PageResult<TravelCompanion>> getCompanions(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String destination) {
        return Result.success(companionService.getCompanionList(
                page, size, status, null, destination, null, null, null, null, null));
    }

    /**
     * 审核通过
     */
    @PostMapping("/{id}/approve")
    public Result<Void> approve(@PathVariable Long id) {
        companionService.approve(id);
        return Result.success();
    }

    /**
     * 审核拒绝
     */
    @PostMapping("/{id}/reject")
    public Result<Void> reject(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String reason = body.get("reason");
        companionService.reject(id, reason);
        return Result.success();
    }

    /**
     * 删除结伴
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        companionService.softDelete(id);
        return Result.success();
    }

    /**
     * 获取统计数据
     */
    @GetMapping("/stats")
    public Result<Map<String, Long>> getStats() {
        return Result.success(companionService.getStats());
    }
}
