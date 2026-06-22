package com.tourism.controller.admin;

import com.tourism.common.PageResult;
import com.tourism.common.Result;
import com.tourism.entity.MerchantUser;
import com.tourism.service.MerchantUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 管理员商家审核控制器
 */
@RestController
@RequestMapping("/api/admin/merchant")
@RequiredArgsConstructor
public class AdminMerchantController {

    private final MerchantUserService merchantUserService;

    /**
     * 获取商家列表
     */
    @GetMapping("/list")
    public Result<PageResult<MerchantUser>> getMerchantList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String merchantType) {
        
        PageResult<MerchantUser> result = merchantUserService.getMerchantList(
                page, size, keyword, status, merchantType);
        return Result.success(result);
    }

    /**
     * 获取商家详情
     */
    @GetMapping("/{id}")
    public Result<MerchantUser> getMerchantDetail(@PathVariable Long id) {
        MerchantUser merchant = merchantUserService.getById(id);
        if (merchant == null) {
            return Result.error("商家不存在");
        }
        return Result.success(merchant);
    }

    /**
     * 获取待审核景点商家列表
     */
    @GetMapping("/pending/scenic")
    public Result<PageResult<MerchantUser>> getPendingScenicMerchants(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        
        PageResult<MerchantUser> result = merchantUserService.getPendingMerchants(
                page, size, "SCENIC");
        return Result.success(result);
    }

    /**
     * 获取待审核酒店商家列表
     */
    @GetMapping("/pending/hotel")
    public Result<PageResult<MerchantUser>> getPendingHotelMerchants(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        
        PageResult<MerchantUser> result = merchantUserService.getPendingMerchants(
                page, size, "HOTEL");
        return Result.success(result);
    }

    /**
     * 审核通过
     */
    @PostMapping("/approve/{id}")
    public Result<Void> approveMerchant(@PathVariable Long id) {
        merchantUserService.approveMerchant(id);
        return Result.success();
    }

    /**
     * 审核拒绝
     */
    @PostMapping("/reject/{id}")
    public Result<Void> rejectMerchant(
            @PathVariable Long id,
            @RequestBody(required = false) java.util.Map<String, String> body) {
        
        String reason = body != null ? body.get("reason") : "";
        merchantUserService.rejectMerchant(id, reason);
        return Result.success();
    }

    /**
     * 禁用商家
     */
    @PostMapping("/disable/{id}")
    public Result<Void> disableMerchant(@PathVariable Long id) {
        merchantUserService.disableMerchant(id);
        return Result.success();
    }

    /**
     * 启用商家
     */
    @PostMapping("/enable/{id}")
    public Result<Void> enableMerchant(@PathVariable Long id) {
        merchantUserService.enableMerchant(id);
        return Result.success();
    }
}
