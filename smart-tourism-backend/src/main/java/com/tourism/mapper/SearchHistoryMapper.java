package com.tourism.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tourism.dto.HotSearchDTO;
import com.tourism.entity.SearchHistory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Delete;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 搜索历史Mapper接口
 */
@Mapper
public interface SearchHistoryMapper extends BaseMapper<SearchHistory> {
    
    /**
     * 查询用户搜索历史（按时间倒序，限制数量）
     * 
     * @param userId 用户ID
     * @param limit 限制数量
     * @return 搜索历史列表
     */
    @Select("SELECT * FROM search_history WHERE user_id = #{userId} " +
            "ORDER BY search_time DESC LIMIT #{limit}")
    List<SearchHistory> selectUserSearchHistory(@Param("userId") Long userId, 
                                                @Param("limit") Integer limit);
    
    /**
     * 查询用户特定关键词的搜索记录
     * 
     * @param userId 用户ID
     * @param keyword 关键词
     * @return 搜索历史记录
     */
    @Select("SELECT * FROM search_history WHERE user_id = #{userId} AND keyword = #{keyword} LIMIT 1")
    SearchHistory selectByUserIdAndKeyword(@Param("userId") Long userId, 
                                          @Param("keyword") String keyword);
    
    /**
     * 查询热门搜索（最近7天内，按搜索次数排序）
     * 
     * @param startTime 开始时间
     * @param limit 限制数量
     * @return 热门搜索列表
     */
    @Select("SELECT keyword, COUNT(*) as searchCount " +
            "FROM search_history " +
            "WHERE search_time >= #{startTime} " +
            "GROUP BY keyword " +
            "ORDER BY searchCount DESC " +
            "LIMIT #{limit}")
    List<HotSearchDTO> selectHotSearches(@Param("startTime") LocalDateTime startTime, 
                                        @Param("limit") Integer limit);
    
    /**
     * 清理过期的搜索记录
     * 
     * @param expireTime 过期时间
     * @return 删除的记录数
     */
    @Delete("DELETE FROM search_history WHERE search_time < #{expireTime}")
    int deleteExpiredRecords(@Param("expireTime") LocalDateTime expireTime);
    
    /**
     * 统计总搜索次数
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 搜索次数
     */
    @Select("SELECT COUNT(*) FROM search_history " +
            "WHERE search_time >= #{startTime} AND search_time <= #{endTime}")
    Long countSearches(@Param("startTime") LocalDateTime startTime, 
                      @Param("endTime") LocalDateTime endTime);
    
    /**
     * 按日期统计搜索次数
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 日期和搜索次数的映射
     */
    @Select("SELECT DATE(search_time) as date, COUNT(*) as count " +
            "FROM search_history " +
            "WHERE search_time >= #{startTime} AND search_time <= #{endTime} " +
            "GROUP BY DATE(search_time) " +
            "ORDER BY date")
    List<java.util.Map<String, Object>> countSearchesByDate(@Param("startTime") LocalDateTime startTime, 
                                                            @Param("endTime") LocalDateTime endTime);
    
    /**
     * 查询热门关键词统计
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param limit 限制数量
     * @return 关键词统计列表
     */
    @Select("SELECT keyword, COUNT(*) as count " +
            "FROM search_history " +
            "WHERE search_time >= #{startTime} AND search_time <= #{endTime} " +
            "GROUP BY keyword " +
            "ORDER BY count DESC " +
            "LIMIT #{limit}")
    List<java.util.Map<String, Object>> selectTopKeywords(@Param("startTime") LocalDateTime startTime, 
                                                          @Param("endTime") LocalDateTime endTime,
                                                          @Param("limit") Integer limit);
}
