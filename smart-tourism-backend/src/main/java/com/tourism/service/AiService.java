package com.tourism.service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.tourism.config.DeepSeekConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import okhttp3.sse.EventSource;
import okhttp3.sse.EventSourceListener;
import okhttp3.sse.EventSources;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * AI 服务 - 集成硅基流动 API (SiliconFlow)
 * 使用免费的 Qwen 模型，兼容 OpenAI API 格式
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiService {

    private final DeepSeekConfig deepSeekConfig;
    private final OkHttpClient okHttpClient;

    private static final String CHAT_ENDPOINT = "/chat/completions";

    /**
     * 流式对话
     */
    public void chatStream(String message, Long userId, SseEmitter emitter) {
        String systemPrompt = """
            你是一位资深的浙江省旅游向导，名叫"小游"。你对浙江省内的旅游资源非常熟悉。
            
            你的专业领域包括：
            1. 浙江省内各地市的景点推荐（杭州西湖、宁波普陀山、绍兴鲁迅故里、嘉兴乌镇、湖州莫干山、金华横店、衢州江郎山、台州天台山、丽水古堰画乡、温州雁荡山、舟山东极岛等）
            2. 浙江特色美食推荐（杭帮菜、宁波海鲜、绍兴黄酒、金华火腿等）
            3. 浙江省内酒店民宿推荐
            4. 浙江旅游路线规划和行程安排
            5. 浙江旅游的最佳时节、避坑指南、交通攻略
            
            回答要求：
            - 只回答浙江省内的旅游相关问题
            - 如果用户询问浙江省外的内容，礼貌地引导回浙江旅游话题
            - 提供具体、实用的建议，包括具体地点、价格区间、游玩时长等
            - 语气友好专业，回答简洁实用
            - 可以主动推荐一些小众但有特色的浙江景点
            """;

        sendStreamRequest(systemPrompt, message, emitter);
    }

    /**
     * 智能行程规划
     */
    public void planItinerary(String destination, int days, String preferences, SseEmitter emitter) {
        String systemPrompt = """
            你是一个专业的旅游行程规划师。请根据用户的需求，生成详细的旅游行程规划。
            
            输出格式要求：
            1. 使用 Markdown 格式
            2. 按天数分组，每天包含上午、下午、晚上三个时段
            3. 每个时段包含：时间、地点、活动描述、推荐理由
            4. 在最后提供一些实用的旅游小贴士
            
            示例格式：
            ## Day 1
            ### 上午 (9:00-12:00)
            **地点：** xxx
            **活动：** xxx
            **推荐理由：** xxx
            
            ### 下午 (14:00-18:00)
            ...
            
            ### 晚上 (19:00-21:00)
            ...
            
            ## 旅游小贴士
            - xxx
            - xxx
            """;

        String userMessage = String.format(
                "请帮我规划一个%s的%d日游行程。%s",
                destination,
                days,
                preferences != null ? "我的偏好是：" + preferences : ""
        );

        sendStreamRequest(systemPrompt, userMessage, emitter);
    }

    /**
     * 发送流式请求到 DeepSeek API
     */
    private void sendStreamRequest(String systemPrompt, String userMessage, SseEmitter emitter) {
        JSONObject requestBody = new JSONObject();
        // 使用硅基流动的 Qwen 模型（免费且效果好）
        requestBody.put("model", "Qwen/Qwen2.5-7B-Instruct");
        requestBody.put("stream", true);
        requestBody.put("messages", List.of(
                Map.of("role", "system", "content", systemPrompt),
                Map.of("role", "user", "content", userMessage)
        ));

        Request request = new Request.Builder()
                .url(deepSeekConfig.getBaseUrl() + CHAT_ENDPOINT)
                .header("Authorization", "Bearer " + deepSeekConfig.getApiKey())
                .header("Content-Type", "application/json")
                .post(RequestBody.create(requestBody.toJSONString(), MediaType.parse("application/json")))
                .build();

        EventSource.Factory factory = EventSources.createFactory(okHttpClient);
        
        factory.newEventSource(request, new EventSourceListener() {
            @Override
            public void onEvent(EventSource eventSource, String id, String type, String data) {
                try {
                    if ("[DONE]".equals(data)) {
                        emitter.send(SseEmitter.event().data("[DONE]"));
                        emitter.complete();
                        return;
                    }

                    JSONObject json = JSON.parseObject(data);
                    JSONObject delta = json.getJSONArray("choices")
                            .getJSONObject(0)
                            .getJSONObject("delta");
                    
                    String content = delta.getString("content");
                    if (content != null && !content.isEmpty()) {
                        emitter.send(SseEmitter.event().data(content));
                    }
                } catch (Exception e) {
                    log.error("SSE send error", e);
                }
            }

            @Override
            public void onFailure(EventSource eventSource, Throwable t, Response response) {
                String errorMsg = "AI服务暂时不可用，请稍后重试";
                
                if (response != null) {
                    try {
                        String responseBody = response.body() != null ? response.body().string() : "No response body";
                        log.error("SiliconFlow API error - Status: {}, Body: {}", response.code(), responseBody);
                        
                        // 根据状态码提供更具体的错误信息
                        if (response.code() == 401) {
                            errorMsg = "API密钥无效或未授权";
                        } else if (response.code() == 402 || response.code() == 429) {
                            errorMsg = "API额度不足或超出限制";
                        } else if (response.code() == 403) {
                            errorMsg = "API访问被拒绝，请检查账户状态";
                        } else if (response.code() >= 500) {
                            errorMsg = "AI服务器错误，请稍后重试";
                        }
                    } catch (Exception e) {
                        log.error("Failed to read response body", e);
                    }
                } else {
                    log.error("SiliconFlow API error - No response", t);
                    if (t != null) {
                        errorMsg = "网络连接失败: " + t.getMessage();
                    }
                }
                
                try {
                    emitter.send(SseEmitter.event()
                            .name("error")
                            .data(errorMsg));
                    emitter.complete();
                } catch (IOException e) {
                    log.error("SSE error send failed", e);
                }
            }

            @Override
            public void onClosed(EventSource eventSource) {
                try {
                    emitter.complete();
                } catch (Exception e) {
                    log.error("SSE close error", e);
                }
            }
        });
    }
}
