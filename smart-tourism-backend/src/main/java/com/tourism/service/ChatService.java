package com.tourism.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tourism.entity.ChatMessage;
import com.tourism.entity.SysUser;
import com.tourism.mapper.ChatMessageMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 聊天服务
 */
@Service
@RequiredArgsConstructor
public class ChatService extends ServiceImpl<ChatMessageMapper, ChatMessage> {

    private final UserService userService;

    /**
     * 保存消息
     */
    @Transactional
    public ChatMessage saveMessage(Long senderId, Long receiverId, String content, String messageType) {
        ChatMessage message = new ChatMessage();
        message.setSenderId(senderId);
        message.setReceiverId(receiverId);
        message.setContent(content);
        message.setMessageType(messageType);
        message.setReadStatus(0);
        message.setCreateTime(java.time.LocalDateTime.now());
        save(message);
        return message;
    }

    /**
     * 获取聊天历史
     */
    public List<ChatMessage> getChatHistory(Long userId1, Long userId2, int limit) {
        List<ChatMessage> messages = baseMapper.getChatHistory(userId1, userId2, limit);
        
        // 填充发送者信息
        messages.forEach(msg -> {
            SysUser sender = userService.getUserById(msg.getSenderId());
            if (sender != null) {
                msg.setSenderName(sender.getNickname());
                msg.setSenderAvatar(sender.getAvatar());
            }
        });
        
        return messages;
    }

    /**
     * 标记消息为已读
     */
    @Transactional
    public void markAsRead(Long senderId, Long receiverId) {
        baseMapper.markAsRead(senderId, receiverId);
    }

    /**
     * 获取未读消息数
     */
    public int getUnreadCount(Long userId) {
        return baseMapper.getUnreadCount(userId);
    }

    /**
     * 获取有未读消息的用户列表（用于客服工作台）
     */
    public List<Long> getUnreadSenders(Long userId) {
        return baseMapper.getUnreadSenders(userId);
    }
}
