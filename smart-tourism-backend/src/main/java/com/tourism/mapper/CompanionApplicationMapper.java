package com.tourism.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tourism.entity.CompanionApplication;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 结伴申请Mapper
 */
@Mapper
public interface CompanionApplicationMapper extends BaseMapper<CompanionApplication> {
    
    /**
     * 分页查询申请（带用户信息）
     */
    @Select("<script>" +
            "SELECT ca.*, u.username, u.nickname, u.avatar " +
            "FROM companion_application ca " +
            "LEFT JOIN sys_user u ON ca.user_id = u.id " +
            "WHERE 1=1 " +
            "<if test='companionId != null'> AND ca.companion_id = #{companionId} </if>" +
            "<if test='userId != null'> AND ca.user_id = #{userId} </if>" +
            "<if test='status != null'> AND ca.status = #{status} </if>" +
            "ORDER BY ca.create_time DESC" +
            "</script>")
    IPage<CompanionApplication> selectPageWithUser(
            Page<CompanionApplication> page,
            @Param("companionId") Long companionId,
            @Param("userId") Long userId,
            @Param("status") Integer status
    );
}
