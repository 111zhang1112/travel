package com.tourism.dto;

import com.tourism.entity.SysUser;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 登录响应
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    private String token;
    private UserVO user;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserVO {
        private Long id;
        private String username;
        private String nickname;
        private String avatar;
        private String role;
        private String phone;
        private String email;
        
        public static UserVO fromEntity(SysUser user) {
            UserVO vo = new UserVO();
            vo.setId(user.getId());
            vo.setUsername(user.getUsername());
            vo.setNickname(user.getNickname());
            vo.setAvatar(user.getAvatar());
            vo.setRole(user.getRole());
            vo.setPhone(user.getPhone());
            vo.setEmail(user.getEmail());
            return vo;
        }
    }
}
