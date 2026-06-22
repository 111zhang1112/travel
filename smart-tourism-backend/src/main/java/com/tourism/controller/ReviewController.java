package com.tourism.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tourism.common.Result;
import com.tourism.entity.Review;
import com.tourism.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 评价控制器
 */
@RestController
@RequestMapping("/api/review")
@RequiredArgsConstructor
public class ReviewController {
    
    private final ReviewService reviewService;
    
    /**
     * 提交评价
     */
    @PostMapping("/submit")
    public Result<Review> submitReview(@RequestBody Review review) {
        Long userId = StpUtil.getLoginIdAsLong();
        Review result = reviewService.submitReview(userId, review);
        return Result.success(result);
    }
    
    /**
     * 获取目标的评价列表
     */
    @GetMapping("/list")
    public Result<Page<Review>> getReviewList(
            @RequestParam String targetType,
            @RequestParam Long targetId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = null;
        try {
            userId = StpUtil.getLoginIdAsLong();
        } catch (Exception ignored) {}
        
        Page<Review> result = reviewService.getReviewsByTarget(targetType, targetId, page, size, userId);
        return Result.success(result);
    }
    
    /**
     * 获取我的评价列表
     */
    @GetMapping("/my")
    public Result<Page<Review>> getMyReviews(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = StpUtil.getLoginIdAsLong();
        Page<Review> result = reviewService.getReviewsByUser(userId, page, size);
        return Result.success(result);
    }
    
    /**
     * 删除评价
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteReview(@PathVariable Long id) {
        Long userId = StpUtil.getLoginIdAsLong();
        reviewService.deleteReview(userId, id);
        return Result.success();
    }
    
    /**
     * 获取评价统计
     */
    @GetMapping("/stats")
    public Result<ReviewService.ReviewStats> getReviewStats(
            @RequestParam String targetType,
            @RequestParam Long targetId) {
        ReviewService.ReviewStats stats = reviewService.getReviewStats(targetType, targetId);
        return Result.success(stats);
    }
    
    /**
     * 点赞/取消点赞评价
     */
    @PostMapping("/like/{id}")
    public Result<Boolean> toggleLike(@PathVariable Long id) {
        Long userId = StpUtil.getLoginIdAsLong();
        boolean liked = reviewService.toggleLike(userId, id);
        return Result.success(liked);
    }
}
