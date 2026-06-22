package com.tourism.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tourism.common.BusinessException;
import com.tourism.entity.PointsRecord;
import com.tourism.entity.SysUser;
import com.tourism.mapper.PointsRecordMapper;
import com.tourism.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * 积分服务
 */
@Service
@RequiredArgsConstructor
public class PointsService extends ServiceImpl<PointsRecordMapper, PointsRecord> {
    
    private final PointsRecordMapper pointsRecordMapper;
    private final SysUserMapper sysUserMapper;
    
    // 积分规则
    private static final int POINTS_PER_YUAN = 1;  // 每消费1元获得1积分
    private static final int REVIEW_POINTS = 10;   // 评价奖励10积分
    private static final int REVIEW_WITH_IMAGE_POINTS = 20;  // 带图评价奖励20积分
    
    /**
     * 获取用户积分余额
     */
    public Integer getUserPoints(Long userId) {
        SysUser user = sysUserMapper.selectById(userId);
        return user != null ? (user.getPoints() != null ? user.getPoints() : 0) : 0;
    }
    
    /**
     * 获取用户积分记录
     */
    public Page<PointsRecord> getPointsRecords(Long userId, int page, int size) {
        Page<PointsRecord> pageParam = new Page<>(page, size);
        return page(pageParam, new LambdaQueryWrapper<PointsRecord>()
                .eq(PointsRecord::getUserId, userId)
                .orderByDesc(PointsRecord::getCreateTime));
    }
    
    /**
     * 订单完成奖励积分
     */
    @Transactional
    public void rewardOrderPoints(Long userId, Long orderId, BigDecimal amount) {
        int points = amount.intValue() * POINTS_PER_YUAN;
        if (points <= 0) return;
        
        addPoints(userId, points, "ORDER", "订单消费奖励", orderId);
    }
    
    /**
     * 评价奖励积分
     */
    @Transactional
    public void rewardReviewPoints(Long userId, Long reviewId, boolean hasImages) {
        int points = hasImages ? REVIEW_WITH_IMAGE_POINTS : REVIEW_POINTS;
        addPoints(userId, points, "REVIEW", hasImages ? "带图评价奖励" : "评价奖励", reviewId);
    }
    
    /**
     * 积分兑换优惠券
     */
    @Transactional
    public void exchangePoints(Long userId, int points, String description) {
        if (points <= 0) {
            throw new BusinessException(400, "兑换积分必须大于0");
        }
        
        int currentPoints = getUserPoints(userId);
        if (currentPoints < points) {
            throw new BusinessException(400, "积分不足");
        }
        
        deductPoints(userId, points, "EXCHANGE", description, null);
    }
    
    /**
     * 管理员调整积分
     */
    @Transactional
    public void adminAdjustPoints(Long userId, int points, String description) {
        if (points > 0) {
            addPoints(userId, points, "ADMIN", description, null);
        } else if (points < 0) {
            deductPoints(userId, -points, "ADMIN", description, null);
        }
    }
    
    /**
     * 通用积分奖励方法
     */
    @Transactional
    public void addPoints(Long userId, int points, String description) {
        addPoints(userId, points, "REWARD", description, null);
    }
    
    /**
     * 增加积分
     */
    private void addPoints(Long userId, int points, String type, String description, Long relatedId) {
        // 创建积分记录
        PointsRecord record = new PointsRecord();
        record.setUserId(userId);
        record.setPoints(points);
        record.setType(type);
        record.setDescription(description);
        record.setRelatedId(relatedId);
        save(record);
        
        // 更新用户积分
        SysUser user = sysUserMapper.selectById(userId);
        if (user != null) {
            user.setPoints((user.getPoints() != null ? user.getPoints() : 0) + points);
            sysUserMapper.updateById(user);
        }
    }
    
    /**
     * 扣减积分
     */
    private void deductPoints(Long userId, int points, String type, String description, Long relatedId) {
        // 创建积分记录（负数）
        PointsRecord record = new PointsRecord();
        record.setUserId(userId);
        record.setPoints(-points);
        record.setType(type);
        record.setDescription(description);
        record.setRelatedId(relatedId);
        save(record);
        
        // 更新用户积分
        SysUser user = sysUserMapper.selectById(userId);
        if (user != null) {
            user.setPoints((user.getPoints() != null ? user.getPoints() : 0) - points);
            sysUserMapper.updateById(user);
        }
    }
}
