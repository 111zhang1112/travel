package com.tourism.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tourism.entity.Interaction;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 用户行为 Mapper
 */
@Mapper
public interface InteractionMapper extends BaseMapper<Interaction> {
    
    /**
     * 查找与当前用户有相似行为的用户ID列表
     */
    @Select("""
        SELECT DISTINCT i2.user_id 
        FROM interactions i1 
        JOIN interactions i2 ON i1.target_type = i2.target_type 
            AND i1.target_id = i2.target_id 
            AND i1.user_id != i2.user_id
        WHERE i1.user_id = #{userId}
        LIMIT 50
    """)
    List<Long> findSimilarUsers(@Param("userId") Long userId);
    
    /**
     * 获取相似用户交互过但当前用户未交互的景点ID
     * 使用子查询解决 DISTINCT + ORDER BY 不兼容问题
     */
    @Select("""
        SELECT target_id FROM (
            SELECT i.target_id, MAX(i.create_time) as latest_time
            FROM interactions i 
            WHERE i.user_id IN (
                SELECT DISTINCT i2.user_id 
                FROM interactions i1 
                JOIN interactions i2 ON i1.target_type = i2.target_type 
                    AND i1.target_id = i2.target_id 
                    AND i1.user_id != i2.user_id
                WHERE i1.user_id = #{userId}
            )
            AND i.target_type = 'SCENIC'
            AND i.target_id NOT IN (
                SELECT target_id FROM interactions 
                WHERE user_id = #{userId} AND target_type = 'SCENIC'
            )
            GROUP BY i.target_id
            ORDER BY latest_time DESC
            LIMIT #{limit}
        ) AS recommended
    """)
    List<Long> findRecommendedScenicIds(@Param("userId") Long userId, @Param("limit") int limit);
    
    /**
     * 统计用户交互数量
     */
    @Select("SELECT COUNT(*) FROM interactions WHERE user_id = #{userId}")
    int countByUserId(@Param("userId") Long userId);
}
