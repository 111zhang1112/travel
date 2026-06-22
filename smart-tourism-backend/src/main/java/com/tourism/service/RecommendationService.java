package com.tourism.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tourism.entity.Interaction;
import com.tourism.entity.ScenicSpot;
import com.tourism.mapper.InteractionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 推荐服务
 * 实现基于协同过滤的推荐算法
 */
@Service
@RequiredArgsConstructor
public class RecommendationService extends ServiceImpl<InteractionMapper, Interaction> {

    private final ScenicSpotService scenicSpotService;
    private final InteractionMapper interactionMapper;

    // 冷启动阈值：用户交互数少于此值时使用热门推荐
    private static final int COLD_START_THRESHOLD = 3;

    /**
     * 获取推荐景点
     * 如果用户有足够的交互历史，使用协同过滤
     * 否则返回热门景点（冷启动）
     */
    public List<ScenicSpot> getRecommendations(Long userId, int limit) {
        if (userId == null) {
            // 未登录用户，返回热门景点
            return getPopularSpots(limit);
        }

        // 检查用户交互数量
        int interactionCount = interactionMapper.countByUserId(userId);
        
        if (interactionCount < COLD_START_THRESHOLD) {
            // 冷启动：返回热门景点
            return getPopularSpots(limit);
        }

        // 协同过滤推荐
        return collaborativeFiltering(userId, limit);
    }

    /**
     * 协同过滤算法
     * 1. 找到与当前用户有相似行为的用户
     * 2. 获取这些用户交互过但当前用户未交互的景点
     */
    private List<ScenicSpot> collaborativeFiltering(Long userId, int limit) {
        // 获取推荐的景点ID
        List<Long> recommendedIds = interactionMapper.findRecommendedScenicIds(userId, limit);
        
        if (recommendedIds.isEmpty()) {
            // 如果协同过滤没有结果，回退到热门推荐
            return getPopularSpots(limit);
        }

        // 查询景点详情
        LambdaQueryWrapper<ScenicSpot> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(ScenicSpot::getId, recommendedIds);
        wrapper.eq(ScenicSpot::getStatus, 1);
        
        List<ScenicSpot> spots = scenicSpotService.list(wrapper);
        
        // 如果结果不足，补充热门景点
        if (spots.size() < limit) {
            List<ScenicSpot> popular = getPopularSpots(limit - spots.size());
            for (ScenicSpot spot : popular) {
                if (!spots.contains(spot)) {
                    spots.add(spot);
                }
            }
        }
        
        return spots;
    }

    /**
     * 获取热门景点（按浏览量排序）
     */
    private List<ScenicSpot> getPopularSpots(int limit) {
        return scenicSpotService.getPopular(limit);
    }

    /**
     * 记录用户行为
     */
    @Transactional
    public void recordInteraction(Long userId, String targetType, Long targetId, String actionType) {
        // 检查是否已存在相同的交互记录
        LambdaQueryWrapper<Interaction> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Interaction::getUserId, userId);
        wrapper.eq(Interaction::getTargetType, targetType);
        wrapper.eq(Interaction::getTargetId, targetId);
        wrapper.eq(Interaction::getActionType, actionType);
        
        if (count(wrapper) == 0) {
            Interaction interaction = new Interaction();
            interaction.setUserId(userId);
            interaction.setTargetType(targetType);
            interaction.setTargetId(targetId);
            interaction.setActionType(actionType);
            save(interaction);
        }
    }

    /**
     * 获取用户的交互历史
     */
    public List<Interaction> getUserInteractions(Long userId, String targetType) {
        LambdaQueryWrapper<Interaction> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Interaction::getUserId, userId);
        if (targetType != null) {
            wrapper.eq(Interaction::getTargetType, targetType);
        }
        wrapper.orderByDesc(Interaction::getCreateTime);
        return list(wrapper);
    }
}
