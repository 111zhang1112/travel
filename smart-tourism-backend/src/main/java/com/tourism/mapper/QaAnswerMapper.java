package com.tourism.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tourism.entity.QaAnswer;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 问答答案 Mapper
 */
@Mapper
public interface QaAnswerMapper extends BaseMapper<QaAnswer> {
    
    /**
     * 查询问题的所有答案（带用户信息）
     */
    @Select("""
        SELECT a.*, u.username, u.nickname, u.avatar
        FROM qa_answer a
        LEFT JOIN sys_user u ON a.user_id = u.id
        WHERE a.question_id = #{questionId} AND a.status = 1
        ORDER BY a.is_best DESC, a.like_count DESC, a.create_time DESC
    """)
    List<QaAnswer> selectAnswersByQuestionId(@Param("questionId") Long questionId);
    
    /**
     * 增加点赞数
     */
    @Update("UPDATE qa_answer SET like_count = like_count + 1 WHERE id = #{id}")
    int incrementLikeCount(@Param("id") Long id);
    
    /**
     * 设置为最佳答案
     */
    @Update("UPDATE qa_answer SET is_best = 1 WHERE id = #{id}")
    int setAsBest(@Param("id") Long id);
    
    /**
     * 取消最佳答案
     */
    @Update("UPDATE qa_answer SET is_best = 0 WHERE question_id = #{questionId}")
    int cancelBest(@Param("questionId") Long questionId);
}
