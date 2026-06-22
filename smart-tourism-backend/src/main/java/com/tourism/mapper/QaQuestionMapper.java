package com.tourism.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tourism.entity.QaQuestion;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * 问答问题 Mapper
 */
@Mapper
public interface QaQuestionMapper extends BaseMapper<QaQuestion> {
    
    /**
     * 分页查询问题列表（带用户信息）
     */
    @Select("""
        <script>
        SELECT q.*, u.username, u.nickname, u.avatar
        FROM qa_question q
        LEFT JOIN sys_user u ON q.user_id = u.id
        WHERE q.status = 1
        <if test="category != null and category != ''">
            AND q.category = #{category}
        </if>
        <if test="keyword != null and keyword != ''">
            AND (q.title LIKE CONCAT('%', #{keyword}, '%') OR q.content LIKE CONCAT('%', #{keyword}, '%'))
        </if>
        <if test="isSolved != null">
            AND q.is_solved = #{isSolved}
        </if>
        ORDER BY q.create_time DESC
        </script>
    """)
    IPage<QaQuestion> selectQuestionPage(Page<QaQuestion> page,
                                         @Param("category") String category,
                                         @Param("keyword") String keyword,
                                         @Param("isSolved") Integer isSolved);
    
    /**
     * 查询问题详情（带用户信息）
     */
    @Select("""
        SELECT q.*, u.username, u.nickname, u.avatar
        FROM qa_question q
        LEFT JOIN sys_user u ON q.user_id = u.id
        WHERE q.id = #{id}
    """)
    QaQuestion selectQuestionDetail(@Param("id") Long id);
    
    /**
     * 增加浏览量
     */
    @Update("UPDATE qa_question SET view_count = view_count + 1 WHERE id = #{id}")
    int incrementViewCount(@Param("id") Long id);
    
    /**
     * 增加回答数
     */
    @Update("UPDATE qa_question SET answer_count = answer_count + 1 WHERE id = #{id}")
    int incrementAnswerCount(@Param("id") Long id);
    
    /**
     * 减少回答数
     */
    @Update("UPDATE qa_question SET answer_count = answer_count - 1 WHERE id = #{id} AND answer_count > 0")
    int decrementAnswerCount(@Param("id") Long id);
    
    /**
     * 增加点赞数
     */
    @Update("UPDATE qa_question SET like_count = like_count + 1 WHERE id = #{id}")
    int incrementLikeCount(@Param("id") Long id);
    
    /**
     * 设置最佳答案
     */
    @Update("UPDATE qa_question SET best_answer_id = #{answerId}, is_solved = 1 WHERE id = #{id}")
    int setBestAnswer(@Param("id") Long id, @Param("answerId") Long answerId);
}
