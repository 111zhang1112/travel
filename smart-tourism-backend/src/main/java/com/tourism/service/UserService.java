package com.tourism.service;

import cn.dev33.satoken.secure.BCrypt;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tourism.common.BusinessException;
import com.tourism.dto.LoginRequest;
import com.tourism.dto.LoginResponse;
import com.tourism.dto.RegisterRequest;
import com.tourism.entity.SysUser;
import com.tourism.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 用户服务
 */
@Service
@RequiredArgsConstructor
public class UserService extends ServiceImpl<SysUserMapper, SysUser> {

    /**
     * 用户注册
     */
    @Transactional
    public LoginResponse register(RegisterRequest request) {
        // 检查用户名是否已存在
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getUsername, request.getUsername());
        if (count(wrapper) > 0) {
            throw new BusinessException(400, "用户名已存在");
        }

        // 创建用户
        SysUser user = new SysUser();
        user.setUsername(request.getUsername());
        user.setPassword(BCrypt.hashpw(request.getPassword()));
        user.setNickname(request.getNickname() != null ? request.getNickname() : request.getUsername());
        user.setPhone(request.getPhone());
        user.setEmail(request.getEmail());
        user.setRole("USER");
        user.setStatus(1);
        save(user);

        // 登录并返回 token
        StpUtil.login(user.getId());
        String token = StpUtil.getTokenValue();

        return new LoginResponse(token, LoginResponse.UserVO.fromEntity(user));
    }

    /**
     * 用户登录
     */
    public LoginResponse login(LoginRequest request) {
        // 查询用户
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getUsername, request.getUsername());
        SysUser user = getOne(wrapper);

        if (user == null) {
            throw new BusinessException(400, "用户名或密码错误");
        }

        // 验证密码
        if (!BCrypt.checkpw(request.getPassword(), user.getPassword())) {
            throw new BusinessException(400, "用户名或密码错误");
        }

        // 检查状态
        if (user.getStatus() != 1) {
            throw new BusinessException(400, "账号已被禁用");
        }

        // 登录
        StpUtil.login(user.getId());
        String token = StpUtil.getTokenValue();

        return new LoginResponse(token, LoginResponse.UserVO.fromEntity(user));
    }

    /**
     * 管理员登录
     * 验证用户角色必须为 ADMIN
     */
    public LoginResponse adminLogin(LoginRequest request) {
        // 查询用户
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getUsername, request.getUsername());
        SysUser user = getOne(wrapper);

        if (user == null) {
            throw new BusinessException(400, "用户名或密码错误");
        }

        // 验证密码
        if (!BCrypt.checkpw(request.getPassword(), user.getPassword())) {
            throw new BusinessException(400, "用户名或密码错误");
        }

        // 检查状态
        if (user.getStatus() != 1) {
            throw new BusinessException(400, "账号已被禁用");
        }

        // 验证管理员角色
        if (!"ADMIN".equals(user.getRole())) {
            throw new BusinessException(403, "无管理员权限");
        }

        // 登录并设置角色
        StpUtil.login(user.getId());
        // 设置角色信息到 Sa-Token
        StpUtil.getSession().set("role", user.getRole());
        String token = StpUtil.getTokenValue();

        return new LoginResponse(token, LoginResponse.UserVO.fromEntity(user));
    }

    /**
     * 获取当前登录用户信息
     */
    public LoginResponse.UserVO getCurrentUser() {
        Long userId = StpUtil.getLoginIdAsLong();
        SysUser user = getById(userId);
        if (user == null) {
            throw new BusinessException(401, "用户不存在");
        }
        return LoginResponse.UserVO.fromEntity(user);
    }

    /**
     * 退出登录
     */
    public void logout() {
        StpUtil.logout();
    }

    /**
     * 根据ID获取用户
     */
    public SysUser getUserById(Long userId) {
        return getById(userId);
    }

    /**
     * 更新用户头像
     */
    @Transactional
    public void updateAvatar(Long userId, String avatarUrl) {
        SysUser user = getById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        user.setAvatar(avatarUrl);
        updateById(user);
    }

    /**
     * 更新用户信息
     */
    @Transactional
    public void updateUserInfo(Long userId, String nickname, String phone, String email) {
        SysUser user = getById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        if (nickname != null) {
            user.setNickname(nickname);
        }
        if (phone != null) {
            user.setPhone(phone);
        }
        if (email != null) {
            user.setEmail(email);
        }
        updateById(user);
    }

    /**
     * 修改密码
     */
    @Transactional
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        SysUser user = getById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        
        // 验证原密码
        if (!user.getPassword().equals(oldPassword)) {
            throw new BusinessException(400, "原密码错误");
        }
        
        // 更新新密码
        user.setPassword(newPassword);
        updateById(user);
    }
    
    /**
     * 获取用户增长趋势（最近12个月）
     */
    public java.util.List<java.util.Map<String, Object>> getUserGrowthTrend() {
        return baseMapper.getUserGrowthTrend();
    }
}

