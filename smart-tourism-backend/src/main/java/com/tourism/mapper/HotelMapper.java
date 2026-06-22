package com.tourism.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tourism.entity.Hotel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 酒店 Mapper
 */
@Mapper
public interface HotelMapper extends BaseMapper<Hotel> {
    
    /**
     * 获取热门酒店TOP N（基于星级和订单数）
     * 注意：这里使用star_rating作为排序依据，如果有订单统计可以优化
     */
    @Select("SELECT h.id, h.name, h.star_rating, h.price_min, h.price_max, " +
            "COALESCE(COUNT(o.id), 0) as order_count " +
            "FROM hotel h " +
            "LEFT JOIN orders o ON o.product_type = 'HOTEL' AND o.product_id = h.id " +
            "WHERE h.status = 1 " +
            "GROUP BY h.id, h.name, h.star_rating, h.price_min, h.price_max " +
            "ORDER BY order_count DESC, h.star_rating DESC " +
            "LIMIT #{limit}")
    List<Map<String, Object>> getTopHotels(@Param("limit") int limit);
}
