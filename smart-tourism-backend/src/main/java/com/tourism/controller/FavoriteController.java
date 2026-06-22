package com.tourism.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.tourism.common.Result;
import com.tourism.dto.FavoriteDTO;
import com.tourism.service.FavoriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 收藏控制器
 */
@RestController
@RequestMapping("/api/favorite")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;

    /**
     * 添加收藏
     */
    @PostMapping
    public Result<Void> addFavorite(
            @RequestParam String targetType,
            @RequestParam Long targetId
    ) {
        Long userId = StpUtil.getLoginIdAsLong();
        favoriteService.addFavorite(userId, targetType, targetId);
        return Result.success();
    }

    /**
     * 取消收藏
     */
    @DeleteMapping
    public Result<Void> removeFavorite(
            @RequestParam String targetType,
            @RequestParam Long targetId
    ) {
        Long userId = StpUtil.getLoginIdAsLong();
        favoriteService.removeFavorite(userId, targetType, targetId);
        return Result.success();
    }

    /**
     * 检查是否已收藏
     */
    @GetMapping("/check")
    public Result<Boolean> checkFavorite(
            @RequestParam String targetType,
            @RequestParam Long targetId
    ) {
        Long userId = StpUtil.getLoginIdAsLong();
        boolean isFavorite = favoriteService.isFavorite(userId, targetType, targetId);
        return Result.success(isFavorite);
    }

    /**
     * 获取用户收藏列表
     */
    @GetMapping("/list")
    public Result<List<FavoriteDTO>> getFavorites(
            @RequestParam(required = false) String targetType
    ) {
        Long userId = StpUtil.getLoginIdAsLong();
        List<FavoriteDTO> favorites = favoriteService.getUserFavorites(userId, targetType);
        return Result.success(favorites);
    }
}
