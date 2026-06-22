package com.tourism.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tourism.common.Result;
import com.tourism.entity.QaAnswer;
import com.tourism.entity.QaQuestion;
import com.tourism.service.QaService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 问答控制器
 */
@RestController
@RequestMapping("/api/qa")
@RequiredArgsConstructor
public class QaController {
    
    private final QaService qaService;
    
    /**
     * 获取问题列表
     */
    @GetMapping("/questions")
    public Result<IPage<QaQuestion>> getQuestions(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer isSolved) {
        IPage<QaQuestion> questions = qaService.getQuestions(page, size, category, keyword, isSolved);
        return Result.success(questions);
    }
    
    /**
     * 获取问题详情
     */
    @GetMapping("/questions/{id}")
    public Result<QaQuestion> getQuestionDetail(@PathVariable Long id) {
        QaQuestion question = qaService.getQuestionDetail(id);
        return Result.success(question);
    }
    
    /**
     * 发布问题
     */
    @PostMapping("/questions")
    public Result<QaQuestion> createQuestion(@RequestBody QaQuestion question) {
        QaQuestion created = qaService.createQuestion(question);
        return Result.success(created);
    }
    
    /**
     * 点赞问题
     */
    @PostMapping("/questions/{id}/like")
    public Result<Void> likeQuestion(@PathVariable Long id) {
        qaService.likeQuestion(id);
        return Result.success();
    }
    
    /**
     * 获取问题的答案列表
     */
    @GetMapping("/questions/{questionId}/answers")
    public Result<List<QaAnswer>> getAnswers(@PathVariable Long questionId) {
        List<QaAnswer> answers = qaService.getAnswers(questionId);
        return Result.success(answers);
    }
    
    /**
     * 回答问题
     */
    @PostMapping("/answers")
    public Result<QaAnswer> createAnswer(@RequestBody QaAnswer answer) {
        QaAnswer created = qaService.createAnswer(answer);
        return Result.success(created);
    }
    
    /**
     * 点赞答案
     */
    @PostMapping("/answers/{id}/like")
    public Result<Void> likeAnswer(@PathVariable Long id) {
        qaService.likeAnswer(id);
        return Result.success();
    }
    
    /**
     * 设置最佳答案
     */
    @PostMapping("/questions/{questionId}/best-answer/{answerId}")
    public Result<Void> setBestAnswer(@PathVariable Long questionId, @PathVariable Long answerId) {
        qaService.setBestAnswer(questionId, answerId);
        return Result.success();
    }
    
    /**
     * 删除答案
     */
    @DeleteMapping("/answers/{id}")
    public Result<Void> deleteAnswer(@PathVariable Long id) {
        qaService.deleteAnswer(id);
        return Result.success();
    }
}
