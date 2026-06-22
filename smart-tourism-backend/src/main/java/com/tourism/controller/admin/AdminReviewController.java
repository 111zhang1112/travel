package com.tourism.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tourism.common.Result;
import com.tourism.entity.Review;
import com.tourism.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 管理员评价控制器
 */
@RestController
@RequestMapping("/api/admin/review")
@RequiredArgsConstructor
public class AdminReviewController {
    
    private final ReviewService reviewService;
    
    /**
     * 获取评价列表
     */
    @GetMapping("/list")
    public Result<Page<Review>> listReviews(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String targetType) {
        Page<Review> result = reviewService.adminListReviews(page, size, status, targetType);
        return Result.success(result);
    }
    
    /**
     * 获取待审核评价列表
     */
    @GetMapping("/pending")
    public Result<Page<Review>> getPendingReviews(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<Review> result = reviewService.getPendingReviews(page, size);
        return Result.success(result);
    }
    
    /**
     * 审核评价
     */
    @PostMapping("/audit/{id}")
    public Result<Void> auditReview(
            @PathVariable Long id,
            @RequestParam Integer status) {
        reviewService.auditReview(id, status);
        return Result.success();
    }
    
    /**
     * 批量审核评价
     */
    @PostMapping("/audit/batch")
    public Result<Void> batchAuditReviews(
            @RequestBody java.util.List<Long> ids,
            @RequestParam Integer status) {
        ids.forEach(id -> reviewService.auditReview(id, status));
        return Result.success();
    }
    
    /**
     * 删除评价
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteReview(@PathVariable Long id) {
        reviewService.removeById(id);
        return Result.success();
    }
}
