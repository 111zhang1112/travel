package com.tourism.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tourism.entity.TravelCompanion;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 结伴Mapper
 */
@Mapper
public interface TravelCompanionMapper extends BaseMapper<TravelCompanion> {
    
    /**
     * 分页查询结伴（带用户信息）
     */
    @Select("<script>" +
            "SELECT tc.*, u.username, u.nickname, u.avatar " +
            "FROM travel_companion tc " +
            "LEFT JOIN sys_user u ON tc.user_id = u.id " +
            "WHERE 1=1 " +
            "<if test='status != null'> AND tc.status = #{status} </if>" +
            "<if test='userId != null'> AND tc.user_id = #{userId} </if>" +
            "<if test='destination != null and destination != \"\"'> AND tc.destination LIKE CONCAT('%', #{destination}, '%') </if>" +
            "<if test='budgetRange != null and budgetRange != \"\"'> AND tc.budget_range = #{budgetRange} </if>" +
            "<if test='travelStyle != null and travelStyle != \"\"'> AND tc.travel_style = #{travelStyle} </if>" +
            "<if test='startDate != null'> AND tc.start_date &gt;= #{startDate} </if>" +
            "<if test='endDate != null'> AND tc.end_date &lt;= #{endDate} </if>" +
            "ORDER BY tc.create_time DESC" +
            "</script>")
    IPage<TravelCompanion> selectPageWithUser(
            Page<TravelCompanion> page,
            @Param("status") Integer status,
            @Param("userId") Long userId,
            @Param("destination") String destination,
            @Param("budgetRange") String budgetRange,
            @Param("travelStyle") String travelStyle,
            @Param("startDate") String startDate,
            @Param("endDate") String endDate
    );
    
    /**
     * 统计各状态数量
     */
    @Select("SELECT COUNT(*) FROM travel_companion WHERE status = #{status}")
    Long countByStatus(@Param("status") Integer status);
}
