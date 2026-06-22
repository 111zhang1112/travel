package com.tourism.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tourism.entity.ChatMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 聊天消息 Mapper
 */
@Mapper
public interface ChatMessageMapper extends BaseMapper<ChatMessage> {
    
    /**
     * 获取两个用户之间的聊天记录
     */
    @Select("""
        SELECT * FROM chat_message 
        WHERE (sender_id = #{userId1} AND receiver_id = #{userId2})
           OR (sender_id = #{userId2} AND receiver_id = #{userId1})
        ORDER BY create_time ASC
        LIMIT #{limit}
    """)
    List<ChatMessage> getChatHistory(@Param("userId1") Long userId1, 
                                     @Param("userId2") Long userId2,
                                     @Param("limit") int limit);
    
    /**
     * 标记消息为已读
     */
    @Update("""
        UPDATE chat_message 
        SET read_status = 1 
        WHERE sender_id = #{senderId} AND receiver_id = #{receiverId} AND read_status = 0
    """)
    int markAsRead(@Param("senderId") Long senderId, @Param("receiverId") Long receiverId);
    
    /**
     * 获取未读消息数
     */
    @Select("SELECT COUNT(*) FROM chat_message WHERE receiver_id = #{userId} AND read_status = 0")
    int getUnreadCount(@Param("userId") Long userId);
    
    /**
     * 获取有未读消息的发送者列表
     */
    @Select("""
        SELECT DISTINCT sender_id FROM chat_message 
        WHERE receiver_id = #{userId} AND read_status = 0
    """)
    List<Long> getUnreadSenders(@Param("userId") Long userId);
}
