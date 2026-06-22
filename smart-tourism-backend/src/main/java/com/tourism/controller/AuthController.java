package com.tourism.controller;

import com.tourism.common.Result;
import com.tourism.dto.LoginRequest;
import com.tourism.dto.LoginResponse;
import com.tourism.dto.RegisterRequest;
import com.tourism.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public Result<LoginResponse> register(@Valid @RequestBody RegisterRequest request) {
        LoginResponse response = userService.register(request);
        return Result.success(response);
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = userService.login(request);
        return Result.success(response);
    }

    /**
     * 管理员登录
     */
    @PostMapping("/admin-login")
    public Result<LoginResponse> adminLogin(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = userService.adminLogin(request);
        return Result.success(response);
    }

    /**
     * 获取当前用户信息
     */
    @GetMapping("/info")
    public Result<LoginResponse.UserVO> getUserInfo() {
        LoginResponse.UserVO user = userService.getCurrentUser();
        return Result.success(user);
    }

    /**
     * 退出登录
     */
    @PostMapping("/logout")
    public Result<Void> logout() {
        userService.logout();
        return Result.success();
    }

    /**
     * 更新用户头像
     */
    @PostMapping("/avatar")
    public Result<LoginResponse.UserVO> updateAvatar(@RequestBody UpdateAvatarRequest request) {
        Long userId = cn.dev33.satoken.stp.StpUtil.getLoginIdAsLong();
        userService.updateAvatar(userId, request.getAvatarUrl());
        LoginResponse.UserVO user = userService.getCurrentUser();
        return Result.success(user);
    }

    /**
     * 更新用户信息
     */
    @PutMapping("/info")
    public Result<LoginResponse.UserVO> updateUserInfo(@RequestBody UpdateUserInfoRequest request) {
        Long userId = cn.dev33.satoken.stp.StpUtil.getLoginIdAsLong();
        userService.updateUserInfo(userId, request.getNickname(), request.getPhone(), request.getEmail());
        LoginResponse.UserVO user = userService.getCurrentUser();
        return Result.success(user);
    }

    /**
     * 更新头像请求
     */
    public static class UpdateAvatarRequest {
        private String avatarUrl;

        public String getAvatarUrl() {
            return avatarUrl;
        }

        public void setAvatarUrl(String avatarUrl) {
            this.avatarUrl = avatarUrl;
        }
    }

    /**
     * 修改密码
     */
    @PostMapping("/change-password")
    public Result<Void> changePassword(@RequestBody ChangePasswordRequest request) {
        Long userId = cn.dev33.satoken.stp.StpUtil.getLoginIdAsLong();
        userService.changePassword(userId, request.getOldPassword(), request.getNewPassword());
        return Result.success();
    }

    /**
     * 更新用户信息请求
     */
    public static class UpdateUserInfoRequest {
        private String nickname;
        private String phone;
        private String email;

        public String getNickname() {
            return nickname;
        }

        public void setNickname(String nickname) {
            this.nickname = nickname;
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }
    }

    /**
     * 修改密码请求
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
