package com.tourism.config;

import cn.dev33.satoken.stp.StpInterface;
import com.tourism.entity.MerchantUser;
import com.tourism.entity.SysUser;
import com.tourism.service.MerchantUserService;
import com.tourism.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Sa-Token 权限认证接口实现
 */
@Component
@RequiredArgsConstructor
public class StpInterfaceImpl implements StpInterface {

    private final UserService userService;
    private final MerchantUserService merchantUserService;

    /**
     * 返回一个账号所拥有的权限码集合
     */
    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        return new ArrayList<>();
    }

    /**
     * 返回一个账号所拥有的角色标识集合
     */
    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        List<String> roles = new ArrayList<>();
        Long userId = Long.parseLong(loginId.toString());
        
        // 先尝试作为商家用户查询
        MerchantUser merchant = merchantUserService.getById(userId);
        if (merchant != null) {
            // 商家用户统一返回MERCHANT角色
            roles.add("MERCHANT");
            return roles;
        }
        
        // 如果不是商家,再作为普通用户查询
        SysUser user = userService.getUserById(userId);
        if (user != null && user.getRole() != null) {
            roles.add(user.getRole());
        }
        
        return roles;
    }
}
