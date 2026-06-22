package com.tourism.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tourism.common.BusinessException;
import com.tourism.entity.*;
import com.tourism.mapper.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * 评价服务
 */
@Service
@RequiredArgsConstructor
public class ReviewService extends ServiceImpl<ReviewMapper, Review> {
    
    private final ReviewMapper reviewMapper;
    private final ScenicSpotMapper scenicSpotMapper;
    private final HotelMapper hotelMapper;
    private final SysUserMapper sysUserMapper;
    private final ReviewLikeMapper reviewLikeMapper;
    
    /**
     * 提交评价
     */
    @Transactional
    public Review submitReview(Long userId, Review review) {
        // 检查是否已评价
        Review existing = getOne(new LambdaQueryWrapper<Review>()
                .eq(Review::getUserId, userId)
                .eq(Review::getTargetType, review.getTargetType())
                .eq(Review::getTargetId, review.getTargetId()));
        
        if (existing != null) {
            throw new BusinessException(400, "您已经评价过了");
        }
        
        // 验证评分范围
        if (review.getRating() == null || review.getRating().compareTo(BigDecimal.ONE) < 0 
                || review.getRating().compareTo(new BigDecimal("5")) > 0) {
            throw new BusinessException(400, "评分必须在1-5之间");
        }
        
        review.setUserId(userId);
        review.setStatus(0);  // 待审核状态
        review.setLikeCount(0);
        save(review);
        
        // 更新目标的平均评分
        updateTargetRating(review.getTargetType(), review.getTargetId());
        
        return review;
    }
    
    /**
     * 更新目标的平均评分
     */
    public void updateTargetRating(String targetType, Long targetId) {
        BigDecimal avgRating = reviewMapper.calculateAverageRating(targetType, targetId);
        Integer reviewCount = reviewMapper.countReviews(targetType, targetId);
        
        // 保留一位小数
        if (avgRating != null && avgRating.compareTo(BigDecimal.ZERO) > 0) {
            avgRating = avgRating.setScale(1, RoundingMode.HALF_UP);
        } else {
            avgRating = new BigDecimal("4.5"); // 默认评分
        }
        
        if ("SCENIC".equals(targetType)) {
            ScenicSpot spot = new ScenicSpot();
            spot.setId(targetId);
            spot.setRating(avgRating);
            scenicSpotMapper.updateById(spot);
        } else if ("HOTEL".equals(targetType)) {
            Hotel hotel = new Hotel();
            hotel.setId(targetId);
            hotel.setRating(avgRating);
            hotelMapper.updateById(hotel);
        }
    }
    
    /**
     * 获取目标的评价列表
     */
    public Page<Review> getReviewsByTarget(String targetType, Long targetId, int page, int size, Long currentUserId) {
        Page<Review> pageParam = new Page<>(page, size);
        Page<Review> result = page(pageParam, new LambdaQueryWrapper<Review>()
                .eq(Review::getTargetType, targetType)
                .eq(Review::getTargetId, targetId)
                .eq(Review::getStatus, 1)
                .orderByDesc(Review::getLikeCount)
                .orderByDesc(Review::getCreateTime));
        
        // 填充用户信息和点赞状态
        result.getRecords().forEach(review -> {
            fillUserInfo(review);
            if (currentUserId != null) {
                fillLikeStatus(review, currentUserId);
            }
        });
        
        return result;
    }
    
    /**
     * 获取用户的评价列表
     */
    public Page<Review> getReviewsByUser(Long userId, int page, int size) {
        Page<Review> pageParam = new Page<>(page, size);
        Page<Review> result = page(pageParam, new LambdaQueryWrapper<Review>()
                .eq(Review::getUserId, userId)
                .orderByDesc(Review::getCreateTime));
        
        // 填充目标名称
        result.getRecords().forEach(this::fillTargetName);
        
        return result;
    }
    
    /**
     * 删除评价
     */
    @Transactional
    public void deleteReview(Long userId, Long reviewId) {
        Review review = getById(reviewId);
        if (review == null) {
            throw new BusinessException(404, "评价不存在");
        }
        if (!review.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权删除他人评价");
        }
        
        removeById(reviewId);
        
        // 更新目标的平均评分
        updateTargetRating(review.getTargetType(), review.getTargetId());
    }
    
    /**
     * 获取评价统计
     */
    public ReviewStats getReviewStats(String targetType, Long targetId) {
        BigDecimal avgRating = reviewMapper.calculateAverageRating(targetType, targetId);
        Integer count = reviewMapper.countReviews(targetType, targetId);
        
        ReviewStats stats = new ReviewStats();
        stats.setAverageRating(avgRating != null ? avgRating.setScale(1, RoundingMode.HALF_UP) : new BigDecimal("0"));
        stats.setTotalCount(count != null ? count : 0);
        
        return stats;
    }
    
    private void fillUserInfo(Review review) {
        SysUser user = sysUserMapper.selectById(review.getUserId());
        if (user != null) {
            review.setUsername(user.getNickname() != null ? user.getNickname() : user.getUsername());
            review.setUserAvatar(user.getAvatar());
        }
    }
    
    private void fillTargetName(Review review) {
        if ("SCENIC".equals(review.getTargetType())) {
            ScenicSpot spot = scenicSpotMapper.selectById(review.getTargetId());
            if (spot != null) {
                review.setTargetName(spot.getName());
            }
        } else if ("HOTEL".equals(review.getTargetType())) {
            Hotel hotel = hotelMapper.selectById(review.getTargetId());
            if (hotel != null) {
                review.setTargetName(hotel.getName());
            }
        }
    }
    
    private void fillLikeStatus(Review review, Long userId) {
        Long count = reviewLikeMapper.selectCount(new LambdaQueryWrapper<ReviewLike>()
                .eq(ReviewLike::getReviewId, review.getId())
                .eq(ReviewLike::getUserId, userId));
        review.setLiked(count > 0);
    }
    
    /**
     * 点赞/取消点赞评价
     */
    @Transactional
    public boolean toggleLike(Long userId, Long reviewId) {
        Review review = getById(reviewId);
        if (review == null) {
            throw new BusinessException(404, "评价不存在");
        }
        
        ReviewLike existing = reviewLikeMapper.selectOne(new LambdaQueryWrapper<ReviewLike>()
                .eq(ReviewLike::getReviewId, reviewId)
                .eq(ReviewLike::getUserId, userId));
        
        if (existing != null) {
            // 取消点赞
            reviewLikeMapper.deleteById(existing.getId());
            review.setLikeCount(Math.max(0, (review.getLikeCount() != null ? review.getLikeCount() : 0) - 1));
            updateById(review);
            return false;
        } else {
            // 点赞
            ReviewLike like = new ReviewLike();
            like.setReviewId(reviewId);
            like.setUserId(userId);
            reviewLikeMapper.insert(like);
            review.setLikeCount((review.getLikeCount() != null ? review.getLikeCount() : 0) + 1);
            updateById(review);
            return true;
        }
    }
    
    /**
     * 管理员审核评价
     */
    @Transactional
    public void auditReview(Long reviewId, Integer status) {
        Review review = getById(reviewId);
        if (review == null) {
            throw new BusinessException(404, "评价不存在");
        }
        
        review.setStatus(status);
        updateById(review);
        
        // 如果审核通过，更新目标评分
        if (status == 1) {
            updateTargetRating(review.getTargetType(), review.getTargetId());
        }
    }
    
    /**
     * 管理员获取待审核评价列表
     */
    public Page<Review> getPendingReviews(int page, int size) {
        Page<Review> pageParam = new Page<>(page, size);
        Page<Review> result = page(pageParam, new LambdaQueryWrapper<Review>()
                .eq(Review::getStatus, 0)
                .orderByAsc(Review::getCreateTime));
        
        result.getRecords().forEach(review -> {
            fillUserInfo(review);
            fillTargetName(review);
        });
        
        return result;
    }
    
    /**
     * 管理员获取所有评价列表
     */
    public Page<Review> adminListReviews(int page, int size, Integer status, String targetType) {
        Page<Review> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Review> wrapper = new LambdaQueryWrapper<Review>()
                .orderByDesc(Review::getCreateTime);
        
        if (status != null) {
            wrapper.eq(Review::getStatus, status);
        }
        if (targetType != null && !targetType.isEmpty()) {
            wrapper.eq(Review::getTargetType, targetType);
        }
        
        Page<Review> result = page(pageParam, wrapper);
        result.getRecords().forEach(review -> {
            fillUserInfo(review);
            fillTargetName(review);
        });
        
        return result;
    }
    
    /**
     * 评价统计内部类
     */
    @lombok.Data
    public static class ReviewStats {
        private BigDecimal averageRating;
        private Integer totalCount;
    }
}
