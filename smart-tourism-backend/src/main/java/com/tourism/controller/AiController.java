package com.tourism.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.tourism.service.AiService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * AI 智能助手控制器
 */
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiService aiService;

    /**
     * 流式对话
     */
    @GetMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chatStream(
            @RequestParam String message,
            @RequestParam(required = false) Long userId
    ) {
        SseEmitter emitter = new SseEmitter(60000L);  // 60秒超时
        
        emitter.onTimeout(() -> {
            emitter.complete();
        });
        
        emitter.onError(e -> {
            emitter.complete();
        });

        Long actualUserId = userId;
        if (actualUserId == null && StpUtil.isLogin()) {
            actualUserId = StpUtil.getLoginIdAsLong();
        }

        aiService.chatStream(message, actualUserId, emitter);
        
        return emitter;
    }

    /**
     * 智能行程规划
     */
    @GetMapping(value = "/itinerary/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter planItinerary(
            @RequestParam String destination,
            @RequestParam Integer days,
            @RequestParam(required = false) String preferences,
            @RequestParam(required = false) Long userId
    ) {
        SseEmitter emitter = new SseEmitter(120000L);  // 120秒超时
        
        emitter.onTimeout(() -> {
            emitter.complete();
        });
        
        emitter.onError(e -> {
            emitter.complete();
        });

        aiService.planItinerary(destination, days, preferences, emitter);
        
        return emitter;
    }
}
