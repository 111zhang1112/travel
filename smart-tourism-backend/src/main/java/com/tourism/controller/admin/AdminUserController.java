package com.tourism.controller.admin;

import cn.dev33.satoken.secure.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tourism.common.Result;
import com.tourism.entity.SysUser;
import com.tourism.service.PointsService;
import com.tourism.service.UserService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 用户管理控制器
 */
@RestController
@RequestMapping("/api/admin/user")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserService userService;
    private final PointsService pointsService;

    @Data
    public static class CreateUserRequest {
        private String username;
        private String password;
        private String nickname;
        private String phone;
        private String role;
    }

    @Data
    public static class AdjustPointsRequest {
        private Integer points;
        private String description;
    }

    /**
     * 分页查询用户列表
     */
    @GetMapping("/list")
    public Result<Page<SysUser>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword
    ) {
        Page<SysUser> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(SysUser::getUsername, keyword)
                   .or()
                   .like(SysUser::getNickname, keyword);
        }
        
        wrapper.orderByDesc(SysUser::getCreateTime);
        Page<SysUser> result = userService.page(pageParam, wrapper);
        
        // 清除密码字段
        result.getRecords().forEach(user -> user.setPassword(null));
        
        return Result.success(result);
    }

    /**
     * 启用/禁用用户
     */
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(
            @PathVariable Long id,
            @RequestParam Integer status
    ) {
        SysUser user = userService.getById(id);
        if (user == null) {
            return Result.error(404, "用户不存在");
        }
        
        user.setStatus(status);
        userService.updateById(user);
        
        return Result.success();
    }

    /**
     * 删除用户
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        userService.removeById(id);
        return Result.success();
    }

    /**
     * 创建用户
     */
    @PostMapping("/create")
    public Result<SysUser> create(@RequestBody CreateUserRequest request) {
        // 检查用户名是否已存在
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getUsername, request.getUsername());
        if (userService.count(wrapper) > 0) {
            return Result.error(400, "用户名已存在");
        }

        // 创建用户
        SysUser user = new SysUser();
        user.setUsername(request.getUsername());
        user.setPassword(BCrypt.hashpw(request.getPassword()));
        user.setNickname(request.getNickname() != null ? request.getNickname() : request.getUsername());
        user.setPhone(request.getPhone());
        user.setRole(request.getRole() != null ? request.getRole() : "USER");
        user.setStatus(1);
        user.setCreateTime(java.time.LocalDateTime.now());
        user.setUpdateTime(java.time.LocalDateTime.now());
        userService.save(user);

        user.setPassword(null);
        return Result.success(user);
    }

    /**
     * 调整用户积分
     */
    @PostMapping("/{id}/points")
    public Result<Void> adjustPoints(
            @PathVariable Long id,
            @RequestBody AdjustPointsRequest request
    ) {
        SysUser user = userService.getById(id);
        if (user == null) {
            return Result.error(404, "用户不存在");
        }

        pointsService.adminAdjustPoints(id, request.getPoints(), request.getDescription());
        return Result.success();
    }
}
