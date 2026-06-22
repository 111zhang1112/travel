package com.tourism.controller.admin;

import cn.dev33.satoken.stp.StpUtil;
import com.tourism.common.Result;
import com.tourism.entity.ChatMessage;
import com.tourism.entity.SysUser;
import com.tourism.service.ChatService;
import com.tourism.service.UserService;
import com.tourism.websocket.WebSocketServer;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 客服聊天管理控制器
 */
@RestController
@RequestMapping("/api/admin/chat")
@RequiredArgsConstructor
public class AdminChatController {

    private final ChatService chatService;
    private final UserService userService;

    /**
     * 获取待接入用户列表（有未读消息的用户）
     */
    @GetMapping("/waiting-users")
    public Result<List<Map<String, Object>>> getWaitingUsers() {
        Long adminId = StpUtil.getLoginIdAsLong();
        List<Long> senderIds = chatService.getUnreadSenders(adminId);
        
        List<Map<String, Object>> users = senderIds.stream()
                .map(userId -> {
                    SysUser user = userService.getUserById(userId);
                    Map<String, Object> userMap = new HashMap<>();
                    userMap.put("userId", userId);
                    userMap.put("nickname", user != null ? user.getNickname() : "未知用户");
                    userMap.put("avatar", user != null && user.getAvatar() != null ? user.getAvatar() : "");
                    userMap.put("online", WebSocketServer.isOnline(userId));
                    return userMap;
                })
                .collect(Collectors.toList());
        
        return Result.success(users);
    }

    /**
     * 获取与指定用户的聊天历史
     */
    @GetMapping("/history/{userId}")
    public Result<List<ChatMessage>> getChatHistory(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "100") Integer limit
    ) {
        Long adminId = StpUtil.getLoginIdAsLong();
        List<ChatMessage> history = chatService.getChatHistory(adminId, userId, limit);
        return Result.success(history);
    }
}
