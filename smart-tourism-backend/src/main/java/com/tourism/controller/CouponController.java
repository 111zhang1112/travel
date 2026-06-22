package com.tourism.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tourism.common.Result;
import com.tourism.entity.Coupon;
import com.tourism.entity.UserCoupon;
import com.tourism.service.CouponService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * 优惠券控制器
 */
@RestController
@RequestMapping("/api/coupon")
@RequiredArgsConstructor
public class CouponController {
    
    private final CouponService couponService;
    
    /**
     * 获取可领取的优惠券列表
     */
    @GetMapping("/available")
    public Result<List<Coupon>> getAvailableCoupons() {
        Long userId = null;
        try {
            userId = StpUtil.getLoginIdAsLong();
        } catch (Exception ignored) {}
        
        List<Coupon> coupons = couponService.getAvailableCoupons(userId);
        return Result.success(coupons);
    }
    
    /**
     * 领取优惠券
     */
    @PostMapping("/receive/{id}")
    public Result<Void> receiveCoupon(@PathVariable Long id) {
        Long userId = StpUtil.getLoginIdAsLong();
        couponService.receiveCoupon(userId, id);
        return Result.success();
    }
    
    /**
     * 获取我的优惠券列表
     */
    @GetMapping("/my")
    public Result<Page<UserCoupon>> getMyCoupons(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = StpUtil.getLoginIdAsLong();
        Page<UserCoupon> result = couponService.getUserCoupons(userId, status, page, size);
        return Result.success(result);
    }
    
    /**
     * 获取可用优惠券（下单时）
     */
    @GetMapping("/usable")
    public Result<List<UserCoupon>> getUsableCoupons(@RequestParam BigDecimal amount) {
        Long userId = StpUtil.getLoginIdAsLong();
        List<UserCoupon> coupons = couponService.getUsableCoupons(userId, amount);
        return Result.success(coupons);
    }
}
