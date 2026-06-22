package com.tourism.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.tourism.common.BusinessException;
import com.tourism.common.PageResult;
import com.tourism.common.Result;
import com.tourism.entity.CompanionApplication;
import com.tourism.entity.TravelCompanion;
import com.tourism.service.CompanionApplicationService;
import com.tourism.service.TravelCompanionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

/**
 * 结伴控制器（用户端）
 */
@RestController
@RequestMapping("/api/companion")
@RequiredArgsConstructor
public class CompanionController {

    private final TravelCompanionService companionService;
    private final CompanionApplicationService applicationService;

    /**
     * 获取结伴列表（只显示招募中的）
     */
    @GetMapping("/list")
    public Result<PageResult<TravelCompanion>> getCompanions(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String destination,
            @RequestParam(required = false) String budgetRange,
            @RequestParam(required = false) String travelStyle,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        Long userId = null;
        try {
            userId = StpUtil.getLoginIdAsLong();
        } catch (Exception ignored) {
        }
        return Result.success(companionService.getCompanionList(
                page, size, 1, null, destination, budgetRange, travelStyle, startDate, endDate, userId));
    }

    /**
     * 获取结伴详情
     */
    @GetMapping("/{id}")
    public Result<TravelCompanion> getCompanionDetail(@PathVariable Long id) {
        TravelCompanion companion = companionService.getById(id);
        if (companion == null) {
            throw new BusinessException(404, "结伴信息不存在");
        }
        // 增加浏览量
        companion.setViewCount(companion.getViewCount() + 1);
        companionService.updateById(companion);
        return Result.success(companion);
    }

    /**
     * 发布结伴
     */
    @PostMapping("/create")
    public Result<TravelCompanion> createCompanion(@RequestBody TravelCompanion companion) {
        Long userId = StpUtil.getLoginIdAsLong();
        
        companion.setUserId(userId);
        companion.setCurrentPeople(1);
        companion.setViewCount(0);
        companion.setLikeCount(0);
        companion.setCommentCount(0);
        companion.setStatus(0); // 待审核
        
        companionService.save(companion);
        return Result.success(companion);
    }

    /**
     * 更新结伴
     */
    @PutMapping("/{id}")
    public Result<Void> updateCompanion(@PathVariable Long id, @RequestBody TravelCompanion companion) {
        Long userId = StpUtil.getLoginIdAsLong();
        TravelCompanion existing = companionService.getById(id);
        
        if (existing == null) {
            throw new BusinessException(404, "结伴信息不存在");
        }
        if (!existing.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权修改此结伴");
        }
        
        companion.setId(id);
        companion.setUserId(userId);
        companionService.updateById(companion);
        return Result.success();
    }

    /**
     * 删除结伴
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteCompanion(@PathVariable Long id) {
        Long userId = StpUtil.getLoginIdAsLong();
        TravelCompanion companion = companionService.getById(id);
        
        if (companion == null) {
            throw new BusinessException(404, "结伴信息不存在");
        }
        if (!companion.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权删除此结伴");
        }
        
        companionService.softDelete(id);
        return Result.success();
    }

    /**
     * 结束招募
     */
    @PostMapping("/{id}/end")
    public Result<Void> endRecruit(@PathVariable Long id) {
        Long userId = StpUtil.getLoginIdAsLong();
        companionService.endRecruit(id, userId);
        return Result.success();
    }

    /**
     * 点赞结伴
     */
    @PostMapping("/{id}/like")
    public Result<Void> likeCompanion(@PathVariable Long id) {
        TravelCompanion companion = companionService.getById(id);
        if (companion == null) {
            throw new BusinessException(404, "结伴信息不存在");
        }
        companion.setLikeCount(companion.getLikeCount() + 1);
        companionService.updateById(companion);
        return Result.success();
    }

    /**
     * 获取我发布的结伴
     */
    @GetMapping("/my-posts")
    public Result<PageResult<TravelCompanion>> getMyCompanions(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        Long userId = StpUtil.getLoginIdAsLong();
        return Result.success(companionService.getCompanionList(
                page, size, null, userId, null, null, null, null, null, userId));
    }

    // ==================== 申请相关 ====================

    /**
     * 申请加入
     */
    @PostMapping("/{id}/apply")
    public Result<Void> applyCompanion(@PathVariable Long id, @RequestBody Map<String, String> body) {
        Long userId = StpUtil.getLoginIdAsLong();
        String message = body.get("message");
        applicationService.apply(id, userId, message);
        return Result.success();
    }

    /**
     * 获取我的申请
     */
    @GetMapping("/my-applications")
    public Result<PageResult<CompanionApplication>> getMyApplications(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        Long userId = StpUtil.getLoginIdAsLong();
        return Result.success(applicationService.getApplicationList(page, size, null, userId, null));
    }

    /**
     * 获取结伴的申请列表（仅发布者可见）
     */
    @GetMapping("/{id}/applications")
    public Result<PageResult<CompanionApplication>> getCompanionApplications(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        Long userId = StpUtil.getLoginIdAsLong();
        TravelCompanion companion = companionService.getById(id);
        
        if (companion == null) {
            throw new BusinessException(404, "结伴信息不存在");
        }
        if (!companion.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权查看申请列表");
        }
        
        return Result.success(applicationService.getApplicationList(page, size, id, null, null));
    }

    /**
     * 同意申请
     */
    @PostMapping("/applications/{id}/approve")
    public Result<Void> approveApplication(@PathVariable Long id, @RequestBody Map<String, String> body) {
        Long userId = StpUtil.getLoginIdAsLong();
        String reply = body.get("reply");
        applicationService.approve(id, userId, reply);
        return Result.success();
    }

    /**
     * 拒绝申请
     */
    @PostMapping("/applications/{id}/reject")
    public Result<Void> rejectApplication(@PathVariable Long id, @RequestBody Map<String, String> body) {
        Long userId = StpUtil.getLoginIdAsLong();
        String reply = body.get("reply");
        applicationService.reject(id, userId, reply);
        return Result.success();
    }
}
