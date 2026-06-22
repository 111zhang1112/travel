package com.tourism.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.tourism.common.Result;
import com.tourism.dto.*;
import com.tourism.service.MerchantUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 商家认证控制器
 */
@RestController
@RequestMapping("/api/merchant/auth")
@RequiredArgsConstructor
public class MerchantAuthController {

    private final MerchantUserService merchantUserService;

    /**
     * 商家注册
     */
    @PostMapping("/register")
    public Result<MerchantInfoResponse> register(@Valid @RequestBody MerchantRegisterRequest request) {
        MerchantInfoResponse response = merchantUserService.register(request);
        return Result.success("注册成功,请等待审核", response);
    }

    /**
     * 商家登录
     */
    @PostMapping("/login")
    public Result<MerchantLoginResponse> login(@Valid @RequestBody MerchantLoginRequest request) {
        MerchantLoginResponse response = merchantUserService.login(request);
        return Result.success("登录成功", response);
    }

    /**
     * 获取当前商家信息
     */
    @GetMapping("/info")
    public Result<MerchantInfoResponse> getMerchantInfo() {
        MerchantInfoResponse response = merchantUserService.getCurrentMerchantInfo();
        return Result.success(response);
    }

    /**
     * 更新商家信息
     */
    @PutMapping("/profile")
    public Result<MerchantInfoResponse> updateProfile(@Valid @RequestBody MerchantUpdateRequest request) {
        MerchantInfoResponse response = merchantUserService.updateMerchantInfo(request);
        return Result.success("更新成功", response);
    }

    /**
     * 修改密码
     */
    @PostMapping("/change-password")
    public Result<Void> changePassword(@RequestBody ChangePasswordRequest request) {
        merchantUserService.changePassword(request.getOldPassword(), request.getNewPassword());
        return Result.success("密码修改成功", null);
    }

    /**
     * 商家登出
     */
    @PostMapping("/logout")
    public Result<Void> logout() {
        StpUtil.logout();
        return Result.success("登出成功", null);
    }
    
    /**
     * 修改密码请求DTO
     */
    public static class ChangePasswordRequest {
        private String oldPassword;
        private String newPassword;
        
        public String getOldPassword() {
            return oldPassword;
        }
        
        public void setOldPassword(String oldPassword) {
            this.oldPassword = oldPassword;
        }
        
        public String getNewPassword() {
            return newPassword;
        }
        
        public void setNewPassword(String newPassword) {
            this.newPassword = newPassword;
        }
    }
}
