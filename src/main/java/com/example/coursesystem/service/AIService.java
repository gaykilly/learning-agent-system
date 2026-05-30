package com.example.coursesystem.service;

import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.alibaba.dashscope.aigc.generation.models.QwenParam;
import com.alibaba.dashscope.common.Message;
import com.alibaba.dashscope.common.Role;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.util.List;

/**
 * AI服务 - 使用通义千问API
 */
@Service
public class AIService {

    @Value("${dashscope.api-key}")
    private String apiKey;

    @Value("${dashscope.model:qwen-turbo}")
    private String model;

    /**
     * 非流式调用AI
     */
    public String callAI(String systemPrompt, String userMessage) {
        try {
            Message systemMsg = Message.builder()
                    .role(Role.SYSTEM.getValue())
                    .content(systemPrompt)
                    .build();

            Message userMsg = Message.builder()
                    .role(Role.USER.getValue())
                    .content(userMessage)
                    .build();

            QwenParam param = QwenParam.builder()
                    .apiKey(apiKey)
                    .model(model)
                    .messages(List.of(systemMsg, userMsg))
                    .temperature(0.7f)
                    .build();

            Generation generation = new Generation();
            GenerationResult result = generation.call(param);

            if (result.getOutput() == null || result.getOutput().getText() == null) {
                return "生成失败：API返回结果为空，请检查API_KEY";
            }
            return result.getOutput().getText();

        } catch (Exception e) {
            e.printStackTrace();
            return "调用失败：" + e.getMessage();
        }
    }

    /**
     * 流式调用AI
     */
    public Flux<String> callAIStream(String systemPrompt, String userMessage) {
        Sinks.Many<String> sink = Sinks.many().unicast().onBackpressureBuffer();

        new Thread(() -> {
            try {
                Message systemMsg = Message.builder()
                        .role(Role.SYSTEM.getValue())
                        .content(systemPrompt)
                        .build();

                Message userMsg = Message.builder()
                        .role(Role.USER.getValue())
                        .content(userMessage)
                        .build();

                QwenParam param = QwenParam.builder()
                        .apiKey(apiKey)
                        .model(model)
                        .messages(List.of(systemMsg, userMsg))
                        .temperature(0.7f)
                        .incrementalOutput(true)
                        .build();

                Generation generation = new Generation();

                generation.streamCall(param)
                        .subscribe(result -> {
                            if (result.getOutput() != null && result.getOutput().getText() != null) {
                                sink.tryEmitNext(result.getOutput().getText());
                            }
                        }, error -> {
                            sink.tryEmitError(error);
                        }, () -> {
                            sink.tryEmitComplete();
                        });

            } catch (Exception e) {
                sink.tryEmitError(e);
            }
        }).start();

        return sink.asFlux();
    }
}
