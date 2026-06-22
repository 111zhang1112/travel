package com.tourism.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tourism.common.Result;
import com.tourism.entity.Coupon;
import com.tourism.service.CouponService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 管理员优惠券控制器
 */
@RestController
@RequestMapping("/api/admin/coupon")
@RequiredArgsConstructor
public class AdminCouponController {
    
    private final CouponService couponService;
    
    /**
     * 获取优惠券列表
     */
    @GetMapping("/list")
    public Result<Page<Coupon>> listCoupons(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword) {
        Page<Coupon> result = couponService.adminListCoupons(page, size, keyword);
        return Result.success(result);
    }
    
    /**
     * 创建优惠券
     */
    @PostMapping
    public Result<Coupon> createCoupon(@RequestBody Coupon coupon) {
        coupon.setStatus(1);
        couponService.save(coupon);
        return Result.success(coupon);
    }
    
    /**
     * 更新优惠券
     */
    @PutMapping("/{id}")
    public Result<Void> updateCoupon(@PathVariable Long id, @RequestBody Coupon coupon) {
        coupon.setId(id);
        couponService.updateById(coupon);
        return Result.success();
    }
    
    /**
     * 删除优惠券
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteCoupon(@PathVariable Long id) {
        couponService.removeById(id);
        return Result.success();
    }
    
    /**
     * 启用/禁用优惠券
     */
    @PostMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        Coupon coupon = new Coupon();
        coupon.setId(id);
        coupon.setStatus(status);
        couponService.updateById(coupon);
        return Result.success();
    }
}
