package com.tourism.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tourism.entity.PointsRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface PointsRecordMapper extends BaseMapper<PointsRecord> {
    
    @Select("SELECT COALESCE(SUM(points), 0) FROM points_record WHERE user_id = #{userId}")
    Integer sumPointsByUserId(Long userId);
}
