package com.tourism.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tourism.entity.Comment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 评论 Mapper
 */
@Mapper
public interface CommentMapper extends BaseMapper<Comment> {
    
    /**
     * 分页查询评论（带用户信息）
     */
    @Select("<script>" +
            "SELECT c.*, u.username, u.nickname, u.avatar " +
            "FROM comment c " +
            "LEFT JOIN sys_user u ON c.user_id = u.id " +
            "WHERE 1=1 " +
            "<if test='status != null'> AND c.status = #{status}</if> " +
            "<if test='targetType != null and targetType != \"\"'> AND c.target_type = #{targetType}</if> " +
            "<if test='targetId != null'> AND c.target_id = #{targetId}</if> " +
            "<if test='keyword != null and keyword != \"\"'> AND c.content LIKE CONCAT('%', #{keyword}, '%')</if> " +
            "ORDER BY c.id DESC" +
            "</script>")
    IPage<Comment> selectPageWithUser(Page<Comment> page,
                                      @Param("status") Integer status,
                                      @Param("targetType") String targetType,
                                      @Param("targetId") Long targetId,
                                      @Param("keyword") String keyword);
    
    /**
     * 统计各状态数量
     */
    @Select("SELECT COUNT(*) FROM comment WHERE status = #{status}")
    Long countByStatus(@Param("status") Integer status);
}
