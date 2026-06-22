package com.tourism.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tourism.common.BusinessException;
import com.tourism.entity.Coupon;
import com.tourism.entity.UserCoupon;
import com.tourism.mapper.CouponMapper;
import com.tourism.mapper.UserCouponMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 优惠券服务
 */
@Service
@RequiredArgsConstructor
public class CouponService extends ServiceImpl<CouponMapper, Coupon> {
    
    private final CouponMapper couponMapper;
    private final UserCouponMapper userCouponMapper;
    
    /**
     * 获取可领取的优惠券列表
     */
    public List<Coupon> getAvailableCoupons(Long userId) {
        LocalDateTime now = LocalDateTime.now();
        List<Coupon> coupons = list(new LambdaQueryWrapper<Coupon>()
                .eq(Coupon::getStatus, 1)
                .gt(Coupon::getRemainCount, 0)
                .le(Coupon::getStartTime, now)
                .ge(Coupon::getEndTime, now)
                .orderByDesc(Coupon::getCreateTime));
        
        // 标记用户是否已领取
        if (userId != null) {
            List<Long> receivedIds = userCouponMapper.selectList(
                    new LambdaQueryWrapper<UserCoupon>().eq(UserCoupon::getUserId, userId)
            ).stream().map(UserCoupon::getCouponId).toList();
            
            coupons.forEach(c -> {
                c.setReceived(receivedIds.contains(c.getId()));
                c.setCanReceive(!c.getReceived() && c.getRemainCount() > 0);
            });
        }
        
        return coupons;
    }
    
    /**
     * 领取优惠券
     */
    @Transactional
    public void receiveCoupon(Long userId, Long couponId) {
        Coupon coupon = getById(couponId);
        if (coupon == null) {
            throw new BusinessException(404, "优惠券不存在");
        }
        
        LocalDateTime now = LocalDateTime.now();
        if (coupon.getStatus() != 1) {
            throw new BusinessException(400, "优惠券已下架");
        }
        if (coupon.getRemainCount() <= 0) {
            throw new BusinessException(400, "优惠券已领完");
        }
        if (now.isBefore(coupon.getStartTime()) || now.isAfter(coupon.getEndTime())) {
            throw new BusinessException(400, "优惠券不在有效期内");
        }
        
        // 检查是否已领取
        Long count = userCouponMapper.selectCount(new LambdaQueryWrapper<UserCoupon>()
                .eq(UserCoupon::getUserId, userId)
                .eq(UserCoupon::getCouponId, couponId));
        if (count > 0) {
            throw new BusinessException(400, "您已领取过该优惠券");
        }
        
        // 扣减库存
        coupon.setRemainCount(coupon.getRemainCount() - 1);
        updateById(coupon);
        
        // 创建用户优惠券
        UserCoupon userCoupon = new UserCoupon();
        userCoupon.setUserId(userId);
        userCoupon.setCouponId(couponId);
        userCoupon.setStatus("UNUSED");
        userCouponMapper.insert(userCoupon);
    }
    
    /**
     * 获取用户的优惠券列表
     */
    public Page<UserCoupon> getUserCoupons(Long userId, String status, int page, int size) {
        Page<UserCoupon> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<UserCoupon> wrapper = new LambdaQueryWrapper<UserCoupon>()
                .eq(UserCoupon::getUserId, userId)
                .orderByDesc(UserCoupon::getCreateTime);
        
        if (status != null && !status.isEmpty()) {
            wrapper.eq(UserCoupon::getStatus, status);
        }
        
        Page<UserCoupon> result = userCouponMapper.selectPage(pageParam, wrapper);
        
        // 填充优惠券详情
        result.getRecords().forEach(uc -> {
            Coupon coupon = getById(uc.getCouponId());
            uc.setCoupon(coupon);
            
            // 检查是否过期
            if ("UNUSED".equals(uc.getStatus()) && coupon != null 
                    && LocalDateTime.now().isAfter(coupon.getEndTime())) {
                uc.setStatus("EXPIRED");
                userCouponMapper.updateById(uc);
            }
        });
        
        return result;
    }
    
    /**
     * 获取用户可用的优惠券（下单时使用）
     */
    public List<UserCoupon> getUsableCoupons(Long userId, BigDecimal orderAmount) {
        List<UserCoupon> userCoupons = userCouponMapper.selectList(
                new LambdaQueryWrapper<UserCoupon>()
                        .eq(UserCoupon::getUserId, userId)
                        .eq(UserCoupon::getStatus, "UNUSED"));
        
        LocalDateTime now = LocalDateTime.now();
        return userCoupons.stream()
                .filter(uc -> {
                    Coupon coupon = getById(uc.getCouponId());
                    if (coupon == null) return false;
                    uc.setCoupon(coupon);
                    
                    // 检查有效期
                    if (now.isBefore(coupon.getStartTime()) || now.isAfter(coupon.getEndTime())) {
                        return false;
                    }
                    // 检查最低消费
                    return orderAmount.compareTo(coupon.getMinAmount()) >= 0;
                })
                .toList();
    }
    
    /**
     * 使用优惠券
     */
    @Transactional
    public void useCoupon(Long userCouponId, Long orderId) {
        UserCoupon userCoupon = userCouponMapper.selectById(userCouponId);
        if (userCoupon == null || !"UNUSED".equals(userCoupon.getStatus())) {
            throw new BusinessException(400, "优惠券不可用");
        }
        
        userCoupon.setStatus("USED");
        userCoupon.setOrderId(orderId);
        userCoupon.setUseTime(LocalDateTime.now());
        userCouponMapper.updateById(userCoupon);
    }
    
    /**
     * 计算优惠金额
     */
    public BigDecimal calculateDiscount(Long couponId, BigDecimal orderAmount) {
        Coupon coupon = getById(couponId);
        if (coupon == null) return BigDecimal.ZERO;
        
        if ("AMOUNT".equals(coupon.getType())) {
            // 满减券
            return coupon.getValue();
        } else if ("DISCOUNT".equals(coupon.getType())) {
            // 折扣券
            BigDecimal discount = orderAmount.multiply(BigDecimal.ONE.subtract(coupon.getValue()));
            return discount.setScale(2, RoundingMode.HALF_UP);
        }
        return BigDecimal.ZERO;
    }
    
    /**
     * 管理员分页查询优惠券
     */
    public Page<Coupon> adminListCoupons(int page, int size, String keyword) {
        Page<Coupon> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Coupon> wrapper = new LambdaQueryWrapper<Coupon>()
                .orderByDesc(Coupon::getCreateTime);
        
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(Coupon::getName, keyword);
        }
        
        return page(pageParam, wrapper);
    }
}
