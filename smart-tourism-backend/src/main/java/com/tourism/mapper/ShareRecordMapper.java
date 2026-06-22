package com.tourism.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tourism.entity.ShareRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 分享记录 Mapper
 */
@Mapper
public interface ShareRecordMapper extends BaseMapper<ShareRecord> {
    
    /**
     * 根据分享URL查询记录
     */
    @Select("SELECT * FROM share_record WHERE share_url = #{shareUrl}")
    ShareRecord getByShareUrl(@Param("shareUrl") String shareUrl);
    
    /**
     * 增加访问次数
     */
    @Update("UPDATE share_record SET view_count = view_count + 1 WHERE id = #{id}")
    int incrementViewCount(@Param("id") Long id);
    
    /**
     * 查询用户今日分享次数
     */
    @Select("""
        SELECT COUNT(*) FROM share_record 
        WHERE user_id = #{userId} 
        AND DATE(create_time) = CURDATE()
    """)
    int getTodayShareCount(@Param("userId") Long userId);
    
    /**
     * 检查用户是否已分享过该内容(24小时内)
     */
    @Select("""
        SELECT COUNT(*) FROM share_record 
        WHERE user_id = #{userId} 
        AND content_type = #{contentType}
        AND content_id = #{contentId}
        AND create_time > #{since}
    """)
    int checkRecentShare(@Param("userId") Long userId,
                         @Param("contentType") String contentType,
                         @Param("contentId") Long contentId,
                         @Param("since") LocalDateTime since);
    
    /**
     * 获取分享排行榜
     */
    @Select("""
        SELECT content_type, content_id, COUNT(*) as share_count
        FROM share_record
        WHERE content_type = #{contentType}
        AND create_time >= #{startTime}
        GROUP BY content_type, content_id
        ORDER BY share_count DESC
        LIMIT #{limit}
    """)
    List<ShareRecord> getShareRanking(@Param("contentType") String contentType,
                                      @Param("startTime") LocalDateTime startTime,
                                      @Param("limit") int limit);
}
