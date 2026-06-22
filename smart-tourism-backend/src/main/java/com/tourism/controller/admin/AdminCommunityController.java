package com.tourism.controller.admin;

import com.tourism.common.PageResult;
import com.tourism.common.Result;
import com.tourism.entity.Comment;
import com.tourism.entity.CommunityPost;
import com.tourism.service.CommentService;
import com.tourism.service.CommunityPostService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 社区管理控制器
 */
@RestController
@RequestMapping("/api/admin/community")
@RequiredArgsConstructor
public class AdminCommunityController {

    private final CommunityPostService postService;
    private final CommentService commentService;

    // ==================== 动态管理 ====================

    /**
     * 获取动态列表
     */
    @GetMapping("/post/list")
    public Result<PageResult<CommunityPost>> getPostList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String keyword) {
        return Result.success(postService.getPostList(page, size, status, userId, keyword));
    }

    /**
     * 获取动态详情
     */
    @GetMapping("/post/{id}")
    public Result<CommunityPost> getPostDetail(@PathVariable Long id) {
        return Result.success(postService.getById(id));
    }

    /**
     * 审核通过
     */
    @PostMapping("/post/approve/{id}")
    public Result<Void> approvePost(@PathVariable Long id) {
        postService.approve(id);
        return Result.success();
    }

    /**
     * 审核拒绝
     */
    @PostMapping("/post/reject/{id}")
    public Result<Void> rejectPost(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String reason = body.get("reason");
        postService.reject(id, reason);
        return Result.success();
    }

    /**
     * 删除动态
     */
    @DeleteMapping("/post/{id}")
    public Result<Void> deletePost(@PathVariable Long id) {
        postService.softDelete(id);
        return Result.success();
    }

    /**
     * 恢复动态
     */
    @PostMapping("/post/restore/{id}")
    public Result<Void> restorePost(@PathVariable Long id) {
        postService.restore(id);
        return Result.success();
    }

    /**
     * 获取动态统计
     */
    @GetMapping("/post/stats")
    public Result<Map<String, Long>> getPostStats() {
        return Result.success(postService.getStats());
    }

    // ==================== 评论管理 ====================

    /**
     * 获取评论列表
     */
    @GetMapping("/comment/list")
    public Result<PageResult<Comment>> getCommentList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String targetType,
            @RequestParam(required = false) Long targetId,
            @RequestParam(required = false) String keyword) {
        return Result.success(commentService.getCommentList(page, size, status, targetType, targetId, keyword));
    }

    /**
     * 删除评论
     */
    @DeleteMapping("/comment/{id}")
    public Result<Void> deleteComment(@PathVariable Long id) {
        commentService.softDelete(id);
        return Result.success();
    }

    /**
     * 恢复评论
     */
    @PostMapping("/comment/restore/{id}")
    public Result<Void> restoreComment(@PathVariable Long id) {
        commentService.restore(id);
        return Result.success();
    }

    /**
     * 批量删除评论
     */
    @PostMapping("/comment/batch-delete")
    public Result<Void> batchDeleteComments(@RequestBody List<Long> ids) {
        commentService.batchDelete(ids);
        return Result.success();
    }

    /**
     * 获取评论统计
     */
    @GetMapping("/comment/stats")
    public Result<Map<String, Long>> getCommentStats() {
        return Result.success(commentService.getStats());
    }
}
