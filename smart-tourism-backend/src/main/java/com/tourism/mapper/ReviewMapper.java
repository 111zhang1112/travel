package com.tourism.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tourism.entity.Review;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;

/**
 * 评价Mapper
 */
@Mapper
public interface ReviewMapper extends BaseMapper<Review> {
    
    /**
     * 计算目标的平均评分
     */
    @Select("SELECT COALESCE(AVG(rating), 0) FROM review WHERE target_type = #{targetType} AND target_id = #{targetId} AND status = 1")
    BigDecimal calculateAverageRating(@Param("targetType") String targetType, @Param("targetId") Long targetId);
    
    /**
     * 统计目标的评价数量
     */
    @Select("SELECT COUNT(*) FROM review WHERE target_type = #{targetType} AND target_id = #{targetId} AND status = 1")
    Integer countReviews(@Param("targetType") String targetType, @Param("targetId") Long targetId);
}
