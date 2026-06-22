package com.tourism.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tourism.common.BusinessException;
import com.tourism.common.PageResult;
import com.tourism.common.Result;
import com.tourism.entity.Comment;
import com.tourism.entity.CommunityPost;
import com.tourism.service.CommentService;
import com.tourism.service.CommunityPostService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 社区控制器（用户端）
 */
@Slf4j
@RestController
@RequestMapping("/api/community")
@RequiredArgsConstructor
public class CommunityController {

    private final CommunityPostService postService;
    private final CommentService commentService;

    /**
     * 获取动态列表（只显示已发布的）
     */
    @GetMapping("/posts")
    public Result<PageResult<CommunityPost>> getPosts(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(postService.getPostList(page, size, 1, null, null));
    }

    /**
     * 获取动态详情
     */
    @GetMapping("/posts/{id}")
    public Result<CommunityPost> getPostDetail(@PathVariable Long id) {
        try {
            CommunityPost post = postService.getPostDetailWithUser(id);
            if (post == null) {
                throw new BusinessException(404, "动态不存在");
            }
            if (post.getStatus() != 1) {
                throw new BusinessException(404, "动态不存在或未发布");
            }
            // 增加浏览量（处理null值）
            Integer currentViewCount = post.getViewCount();
            post.setViewCount(currentViewCount == null ? 1 : currentViewCount + 1);
            postService.updateById(post);
            return Result.success(post);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("获取动态详情失败, id={}", id, e);
            throw new BusinessException(500, "获取动态详情失败: " + e.getMessage());
        }
    }

    /**
     * 发布动态
     */
    @PostMapping("/posts")
    public Result<CommunityPost> createPost(@RequestBody Map<String, Object> body) {
        Long userId = StpUtil.getLoginIdAsLong();
        
        CommunityPost post = new CommunityPost();
        post.setUserId(userId);
        
        // 如果前端没有传 title，使用 content 的前20个字符作为 title
        String title = (String) body.get("title");
        String content = (String) body.get("content");
        if (title == null || title.trim().isEmpty()) {
            if (content != null && content.length() > 20) {
                title = content.substring(0, 20) + "...";
            } else {
                title = content != null ? content : "无标题";
            }
        }
        
        post.setTitle(title);
        post.setContent(content);
        post.setImages((String) body.get("images"));
        post.setLocation((String) body.get("location"));
        post.setViewCount(0);
        post.setLikeCount(0);
        post.setCommentCount(0);
        post.setStatus(0); // 待审核
        
        postService.save(post);
        return Result.success(post);
    }

    /**
     * 删除自己的动态
     */
    @DeleteMapping("/posts/{id}")
    public Result<Void> deletePost(@PathVariable Long id) {
        Long userId = StpUtil.getLoginIdAsLong();
        CommunityPost post = postService.getById(id);
        
        if (post == null) {
            throw new BusinessException(404, "动态不存在");
        }
        if (!post.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权删除此动态");
        }
        
        post.setStatus(3);
        postService.updateById(post);
        return Result.success();
    }

    /**
     * 点赞动态
     */
    @PostMapping("/posts/{id}/like")
    public Result<Void> likePost(@PathVariable Long id) {
        CommunityPost post = postService.getById(id);
        if (post == null || post.getStatus() != 1) {
            throw new BusinessException(404, "动态不存在");
        }
        // 处理null值
        Integer currentLikeCount = post.getLikeCount();
        post.setLikeCount(currentLikeCount == null ? 1 : currentLikeCount + 1);
        postService.updateById(post);
        return Result.success();
    }

    /**
     * 获取我的动态
     */
    @GetMapping("/my-posts")
    public Result<PageResult<CommunityPost>> getMyPosts(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        Long userId = StpUtil.getLoginIdAsLong();
        return Result.success(postService.getPostList(page, size, null, userId, null));
    }

    // ==================== 评论相关 ====================

    /**
     * 获取评论列表
     */
    @GetMapping("/comments")
    public Result<PageResult<Comment>> getComments(
            @RequestParam String targetType,
            @RequestParam Long targetId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        return Result.success(commentService.getCommentList(page, size, 1, targetType, targetId, null));
    }

    /**
     * 发表评论
     */
    @PostMapping("/comments")
    public Result<Comment> createComment(@RequestBody Map<String, Object> body) {
        Long userId = StpUtil.getLoginIdAsLong();
        
        Comment comment = new Comment();
        comment.setUserId(userId);
        comment.setTargetType((String) body.get("targetType"));
        comment.setTargetId(Long.valueOf(body.get("targetId").toString()));
        comment.setContent((String) body.get("content"));
        if (body.get("parentId") != null) {
            comment.setParentId(Long.valueOf(body.get("parentId").toString()));
        }
        comment.setLikeCount(0);
        comment.setStatus(1); // 直接发布
        
        commentService.save(comment);
        
        // 如果是动态评论，更新评论数（处理null值）
        if ("POST".equals(comment.getTargetType())) {
            CommunityPost post = postService.getById(comment.getTargetId());
            if (post != null) {
                Integer currentCommentCount = post.getCommentCount();
                post.setCommentCount(currentCommentCount == null ? 1 : currentCommentCount + 1);
                postService.updateById(post);
            }
        }
        
        return Result.success(comment);
    }

    /**
     * 删除自己的评论
     */
    @DeleteMapping("/comments/{id}")
    public Result<Void> deleteComment(@PathVariable Long id) {
        Long userId = StpUtil.getLoginIdAsLong();
        Comment comment = commentService.getById(id);
        
        if (comment == null) {
            throw new BusinessException(404, "评论不存在");
        }
        if (!comment.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权删除此评论");
        }
        
        comment.setStatus(0);
        commentService.updateById(comment);
        
        // 如果是动态评论，更新评论数（处理null值）
        if ("POST".equals(comment.getTargetType())) {
            CommunityPost post = postService.getById(comment.getTargetId());
            if (post != null) {
                Integer currentCommentCount = post.getCommentCount();
                if (currentCommentCount != null && currentCommentCount > 0) {
                    post.setCommentCount(currentCommentCount - 1);
                    postService.updateById(post);
                }
            }
        }
        
        return Result.success();
    }

    /**
     * 点赞评论
     */
    @PostMapping("/comments/{id}/like")
    public Result<Void> likeComment(@PathVariable Long id) {
        Comment comment = commentService.getById(id);
        if (comment == null || comment.getStatus() != 1) {
            throw new BusinessException(404, "评论不存在");
        }
        // 处理null值
        Integer currentLikeCount = comment.getLikeCount();
        comment.setLikeCount(currentLikeCount == null ? 1 : currentLikeCount + 1);
        commentService.updateById(comment);
        return Result.success();
    }
}
