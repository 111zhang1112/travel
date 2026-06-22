package com.tourism.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.tourism.common.Result;
import com.tourism.entity.ChatMessage;
import com.tourism.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 聊天控制器
 */
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    /**
     * 获取聊天历史
     */
    @GetMapping("/history/{targetUserId}")
    public Result<List<ChatMessage>> getHistory(
            @PathVariable Long targetUserId,
            @RequestParam(defaultValue = "100") Integer limit
    ) {
        Long currentUserId = StpUtil.getLoginIdAsLong();
        List<ChatMessage> history = chatService.getChatHistory(currentUserId, targetUserId, limit);
        return Result.success(history);
    }

    /**
     * 获取未读消息数
     */
    @GetMapping("/unread-count")
    public Result<Integer> getUnreadCount() {
        Long userId = StpUtil.getLoginIdAsLong();
        int count = chatService.getUnreadCount(userId);
        return Result.success(count);
    }

    /**
     * 标记消息已读
     */
    @PostMapping("/mark-read/{senderId}")
    public Result<Void> markAsRead(@PathVariable Long senderId) {
        Long receiverId = StpUtil.getLoginIdAsLong();
        chatService.markAsRead(senderId, receiverId);
        return Result.success();
    }
}
