package com.tourism.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tourism.entity.CommunityPost;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 社区动态 Mapper
 */
@Mapper
public interface CommunityPostMapper extends BaseMapper<CommunityPost> {
    
    /**
     * 分页查询动态（带用户信息）
     */
    @Select("<script>" +
            "SELECT p.*, " +
            "COALESCE(u.username, 'unknown') as username, " +
            "COALESCE(u.nickname, u.username, 'unknown') as nickname, " +
            "COALESCE(u.avatar, '/images/avatar/default.png') as avatar " +
            "FROM community_post p " +
            "LEFT JOIN sys_user u ON p.user_id = u.id " +
            "WHERE 1=1 " +
            "<if test='status != null'> AND p.status = #{status}</if> " +
            "<if test='userId != null'> AND p.user_id = #{userId}</if> " +
            "<if test='keyword != null and keyword != \"\"'> AND p.content LIKE CONCAT('%', #{keyword}, '%')</if> " +
            "ORDER BY p.id DESC" +
            "</script>")
    IPage<CommunityPost> selectPageWithUser(Page<CommunityPost> page, 
                                            @Param("status") Integer status,
                                            @Param("userId") Long userId,
                                            @Param("keyword") String keyword);
    
    /**
     * 根据ID查询动态详情（带用户信息）
     */
    @Select("SELECT p.*, " +
            "COALESCE(u.username, 'unknown') as username, " +
            "COALESCE(u.nickname, u.username, 'unknown') as nickname, " +
            "COALESCE(u.avatar, '/images/avatar/default.png') as avatar " +
            "FROM community_post p " +
            "LEFT JOIN sys_user u ON p.user_id = u.id " +
            "WHERE p.id = #{id}")
    CommunityPost selectByIdWithUser(@Param("id") Long id);
    
    /**
     * 统计各状态数量
     */
    @Select("SELECT COUNT(*) FROM community_post WHERE status = #{status}")
    Long countByStatus(@Param("status") Integer status);
}
