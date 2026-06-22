package com.tourism.controller.admin;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tourism.common.Result;
import com.tourism.entity.QaAnswer;
import com.tourism.entity.QaQuestion;
import com.tourism.mapper.QaAnswerMapper;
import com.tourism.service.QaService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 管理员问答控制器
 */
@RestController
@RequestMapping("/api/admin/qa")
@RequiredArgsConstructor
@SaCheckRole("ADMIN")
public class AdminQaController {
    
    private final QaService qaService;
    private final QaAnswerMapper qaAnswerMapper;
    
    /**
     * 获取问题列表（包含所有状态）
     */
    @GetMapping("/questions")
    public Result<IPage<QaQuestion>> getQuestions(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer status) {
        Page<QaQuestion> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<QaQuestion> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(QaQuestion::getStatus, status);
        }
        wrapper.orderByDesc(QaQuestion::getCreateTime);
        IPage<QaQuestion> questions = qaService.page(pageParam, wrapper);
        return Result.success(questions);
    }
    
    /**
     * 审核问题
     */
    @PutMapping("/questions/{id}/review")
    public Result<Void> reviewQuestion(
            @PathVariable Long id,
            @RequestParam Integer status,
            @RequestParam(required = false) String rejectReason) {
        QaQuestion question = qaService.getById(id);
        if (question == null) {
            return Result.error(404, "问题不存在");
        }
        question.setStatus(status);
        if (status == 2) { // 拒绝
            question.setRejectReason(rejectReason);
        }
        qaService.updateById(question);
        return Result.success();
    }
    
    /**
     * 删除问题
     */
    @DeleteMapping("/questions/{id}")
    public Result<Void> deleteQuestion(@PathVariable Long id) {
        QaQuestion question = qaService.getById(id);
        if (question == null) {
            return Result.error(404, "问题不存在");
        }
        question.setStatus(3); // 已删除
        qaService.updateById(question);
        return Result.success();
    }
    
    /**
     * 获取答案列表
     */
    @GetMapping("/answers")
    public Result<IPage<QaAnswer>> getAnswers(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer status) {
        Page<QaAnswer> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<QaAnswer> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(QaAnswer::getStatus, status);
        }
        wrapper.orderByDesc(QaAnswer::getCreateTime);
        IPage<QaAnswer> answers = qaAnswerMapper.selectPage(pageParam, wrapper);
        return Result.success(answers);
    }
    
    /**
     * 删除答案
     */
    @DeleteMapping("/answers/{id}")
    public Result<Void> deleteAnswer(@PathVariable Long id) {
        QaAnswer answer = qaAnswerMapper.selectById(id);
        if (answer == null) {
            return Result.error(404, "答案不存在");
        }
        answer.setStatus(0); // 已删除
        qaAnswerMapper.updateById(answer);
        return Result.success();
    }
}
