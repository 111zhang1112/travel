package com.tourism.service;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tourism.common.BusinessException;
import com.tourism.entity.QaAnswer;
import com.tourism.entity.QaQuestion;
import com.tourism.mapper.QaAnswerMapper;
import com.tourism.mapper.QaQuestionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 问答服务
 */
@Service
@RequiredArgsConstructor
public class QaService extends ServiceImpl<QaQuestionMapper, QaQuestion> {
    
    private final QaQuestionMapper qaQuestionMapper;
    private final QaAnswerMapper qaAnswerMapper;
    
    /**
     * 分页查询问题列表
     */
    public IPage<QaQuestion> getQuestions(int page, int size, String category, String keyword, Integer isSolved) {
        Page<QaQuestion> pageParam = new Page<>(page, size);
        return qaQuestionMapper.selectQuestionPage(pageParam, category, keyword, isSolved);
    }
    
    /**
     * 获取问题详情
     */
    public QaQuestion getQuestionDetail(Long id) {
        QaQuestion question = qaQuestionMapper.selectQuestionDetail(id);
        if (question == null) {
            throw new BusinessException(404, "问题不存在");
        }
        // 增加浏览量
        qaQuestionMapper.incrementViewCount(id);
        return question;
    }
    
    /**
     * 发布问题
     */
    public QaQuestion createQuestion(QaQuestion question) {
        Long userId = StpUtil.getLoginIdAsLong();
        question.setUserId(userId);
        question.setViewCount(0);
        question.setAnswerCount(0);
        question.setLikeCount(0);
        question.setIsSolved(0);
        question.setStatus(0); // 待审核
        save(question);
        return question;
    }
    
    /**
     * 点赞问题
     */
    public void likeQuestion(Long id) {
        qaQuestionMapper.incrementLikeCount(id);
    }
    
    /**
     * 获取问题的所有答案
     */
    public List<QaAnswer> getAnswers(Long questionId) {
        return qaAnswerMapper.selectAnswersByQuestionId(questionId);
    }
    
    /**
     * 回答问题
     */
    @Transactional
    public QaAnswer createAnswer(QaAnswer answer) {
        Long userId = StpUtil.getLoginIdAsLong();
        answer.setUserId(userId);
        answer.setLikeCount(0);
        answer.setIsBest(0);
        answer.setStatus(1); // 正常
        qaAnswerMapper.insert(answer);
        
        // 增加问题的回答数
        qaQuestionMapper.incrementAnswerCount(answer.getQuestionId());
        
        return answer;
    }
    
    /**
     * 点赞答案
     */
    public void likeAnswer(Long id) {
        qaAnswerMapper.incrementLikeCount(id);
    }
    
    /**
     * 设置最佳答案
     */
    @Transactional
    public void setBestAnswer(Long questionId, Long answerId) {
        Long userId = StpUtil.getLoginIdAsLong();
        
        // 验证问题是否属于当前用户
        QaQuestion question = getById(questionId);
        if (question == null) {
            throw new BusinessException(404, "问题不存在");
        }
        if (!question.getUserId().equals(userId)) {
            throw new BusinessException(403, "只能为自己的问题设置最佳答案");
        }
        
        // 验证答案是否存在
        QaAnswer answer = qaAnswerMapper.selectById(answerId);
        if (answer == null || !answer.getQuestionId().equals(questionId)) {
            throw new BusinessException(404, "答案不存在");
        }
        
        // 取消之前的最佳答案
        qaAnswerMapper.cancelBest(questionId);
        
        // 设置新的最佳答案
        qaAnswerMapper.setAsBest(answerId);
        qaQuestionMapper.setBestAnswer(questionId, answerId);
    }
    
    /**
     * 删除答案
     */
    @Transactional
    public void deleteAnswer(Long id) {
        Long userId = StpUtil.getLoginIdAsLong();
        
        QaAnswer answer = qaAnswerMapper.selectById(id);
        if (answer == null) {
            throw new BusinessException(404, "答案不存在");
        }
        if (!answer.getUserId().equals(userId)) {
            throw new BusinessException(403, "只能删除自己的答案");
        }
        
        // 软删除
        answer.setStatus(0);
        qaAnswerMapper.updateById(answer);
        
        // 减少问题的回答数
        qaQuestionMapper.decrementAnswerCount(answer.getQuestionId());
    }
}
