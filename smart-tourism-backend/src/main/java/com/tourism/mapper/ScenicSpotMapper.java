package com.tourism.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tourism.entity.ScenicSpot;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 景点 Mapper
 */
@Mapper
public interface ScenicSpotMapper extends BaseMapper<ScenicSpot> {
    
    /**
     * 获取热门景点TOP N（基于浏览量）
     */
    @Select("SELECT id, name, view_count, rating, ticket_price " +
            "FROM scenic_spot " +
            "WHERE status = 1 " +
            "ORDER BY view_count DESC " +
            "LIMIT #{limit}")
    List<Map<String, Object>> getTopScenic(@Param("limit") int limit);
}
