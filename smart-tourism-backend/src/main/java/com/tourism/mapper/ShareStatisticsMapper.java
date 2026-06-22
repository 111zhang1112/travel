package com.tourism.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tourism.entity.ShareStatistics;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 分享统计 Mapper
 */
@Mapper
public interface ShareStatisticsMapper extends BaseMapper<ShareStatistics> {
    
    /**
     * 根据内容类型和ID查询统计
     */
    @Select("SELECT * FROM share_statistics WHERE content_type = #{contentType} AND content_id = #{contentId}")
    ShareStatistics getByContent(@Param("contentType") String contentType, @Param("contentId") Long contentId);
    
    /**
     * 增加分享次数
     */
    @Update("UPDATE share_statistics SET share_count = share_count + 1 WHERE content_type = #{contentType} AND content_id = #{contentId}")
    int incrementShareCount(@Param("contentType") String contentType, @Param("contentId") Long contentId);
    
    /**
     * 增加访问次数
     */
    @Update("UPDATE share_statistics SET view_count = view_count + 1 WHERE content_type = #{contentType} AND content_id = #{contentId}")
    int incrementViewCount(@Param("contentType") String contentType, @Param("contentId") Long contentId);
    
    /**
     * 获取分享排行榜
     */
    @Select("""
        SELECT * FROM share_statistics 
        WHERE content_type = #{contentType}
        ORDER BY share_count DESC
        LIMIT #{limit}
    """)
    List<ShareStatistics> getTopShared(@Param("contentType") String contentType, @Param("limit") int limit);
    
    /**
     * 获取访问排行榜
     */
    @Select("""
        SELECT * FROM share_statistics 
        WHERE content_type = #{contentType}
        ORDER BY view_count DESC
        LIMIT #{limit}
    """)
    List<ShareStatistics> getTopViewed(@Param("contentType") String contentType, @Param("limit") int limit);
    
    /**
     * 更新内容表的分享计数
     */
    @Update("UPDATE ${tableName} SET share_count = share_count + 1 WHERE id = #{contentId}")
    int updateContentShareCount(@Param("tableName") String tableName, @Param("contentId") Long contentId);
    
    /**
     * 获取分享排行榜(带时间范围)
     */
    @Select("""
        SELECT sr.content_type, sr.content_id, COUNT(*) as share_count, SUM(sr.view_count) as view_count
        FROM share_record sr
        WHERE sr.content_type = #{contentType}
        AND sr.create_time >= #{startTime}
        GROUP BY sr.content_type, sr.content_id
        ORDER BY share_count DESC
        LIMIT #{limit}
    """)
    List<Map<String, Object>> getShareRanking(@Param("contentType") String contentType,
                                               @Param("startTime") LocalDateTime startTime,
                                               @Param("limit") int limit);
    
    /**
     * 获取平台分享统计
     */
    @Select("""
        SELECT platform, COUNT(*) as count
        FROM share_record
        WHERE create_time >= #{startTime}
        GROUP BY platform
        ORDER BY count DESC
    """)
    List<Map<String, Object>> getPlatformStatistics(@Param("startTime") LocalDateTime startTime);
    
    /**
     * 获取总体统计数据
     */
    @Select("""
        SELECT 
            COUNT(*) as totalShares,
            COUNT(DISTINCT user_id) as totalUsers,
            COALESCE(SUM(view_count), 0) as totalViews
        FROM share_record
        WHERE create_time >= #{startTime}
    """)
    Map<String, Object> getOverallStatistics(@Param("startTime") LocalDateTime startTime);
}
