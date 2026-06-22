package com.tourism.websocket;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.tourism.config.SpringContextHolder;
import com.tourism.entity.ChatMessage;
import com.tourism.service.ChatService;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket 服务端
 * 实现点对点聊天
 */
@Slf4j
@Component
@ServerEndpoint("/ws/chat/{userId}")
public class WebSocketServer {

    // 存储所有连接的会话
    private static final Map<Long, Session> SESSIONS = new ConcurrentHashMap<>();
    
    /**
     * 获取 ChatService（从 Spring 容器中获取）
     */
    private ChatService getChatService() {
        return SpringContextHolder.getBean(ChatService.class);
    }

    /**
     * 连接建立
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("userId") Long userId) {
        SESSIONS.put(userId, session);
        log.info("WebSocket connected: userId={}, sessionId={}", userId, session.getId());
        
        // 发送连接成功消息
        sendMessage(session, JSON.toJSONString(Map.of(
                "type", "connected",
                "message", "连接成功"
        )));
    }

    /**
     * 接收消息
     */
    @OnMessage
    public void onMessage(String message, Session session, @PathParam("userId") Long userId) {
        log.info("Received message from userId={}: {}", userId, message);
        
        try {
            JSONObject json = JSON.parseObject(message);
            String type = json.getString("type");
            
            if ("chat".equals(type)) {
                Long receiverId = json.getLong("receiverId");
                String content = json.getString("content");
                String messageType = json.getString("messageType");
                
                if (messageType == null) {
                    messageType = "TEXT";
                }
                
                // 保存消息到数据库
                ChatMessage chatMessage = getChatService().saveMessage(userId, receiverId, content, messageType);
                
                // 发送给接收者
                sendToUser(receiverId, JSON.toJSONString(Map.of(
                        "type", "chat",
                        "senderId", userId,
                        "content", content,
                        "messageType", messageType,
                        "messageId", chatMessage.getId(),
                        "createTime", chatMessage.getCreateTime().toString()
                )));
                
                // 发送确认给发送者
                sendMessage(session, JSON.toJSONString(Map.of(
                        "type", "sent",
                        "messageId", chatMessage.getId(),
                        "status", "success"
                )));
            } else if ("read".equals(type)) {
                // 标记消息已读
                Long senderId = json.getLong("senderId");
                getChatService().markAsRead(senderId, userId);
            }
        } catch (Exception e) {
            log.error("Message processing error", e);
            sendMessage(session, JSON.toJSONString(Map.of(
                    "type", "error",
                    "message", "消息处理失败"
            )));
        }
    }

    /**
     * 连接关闭
     */
    @OnClose
    public void onClose(Session session, @PathParam("userId") Long userId) {
        SESSIONS.remove(userId);
        log.info("WebSocket disconnected: userId={}", userId);
    }

    /**
     * 连接错误
     */
    @OnError
    public void onError(Session session, Throwable error, @PathParam("userId") Long userId) {
        log.error("WebSocket error for userId={}: {}", userId, error.getMessage());
        SESSIONS.remove(userId);
    }

    /**
     * 发送消息给指定用户
     */
    public void sendToUser(Long userId, String message) {
        Session session = SESSIONS.get(userId);
        if (session != null && session.isOpen()) {
            sendMessage(session, message);
        }
    }

    /**
     * 发送消息
     */
    private void sendMessage(Session session, String message) {
        try {
            session.getBasicRemote().sendText(message);
        } catch (IOException e) {
            log.error("Send message error", e);
        }
    }

    /**
     * 检查用户是否在线
     */
    public static boolean isOnline(Long userId) {
        Session session = SESSIONS.get(userId);
        return session != null && session.isOpen();
    }

    /**
     * 获取在线用户数
     */
    public static int getOnlineCount() {
        return SESSIONS.size();
    }
}
