package com.example.coursesystem.agent;

import com.example.coursesystem.service.AIService;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.publisher.Flux;

/**
 * 智能体基类 - 所有智能体的父类
 */
public abstract class BaseAgent {

    @Autowired
    protected AIService aiService;

    public void setAiService(AIService aiService) {
        this.aiService = aiService;
    }

    /**
     * 智能体名称
     */
    public abstract String getAgentName();

    /**
     * 智能体描述
     */
    public abstract String getAgentDescription();

    /**
     * 处理用户请求（非流式）
     * @param message 用户消息
     * @param context 上下文信息
     * @return 响应结果
     */
    public abstract String process(String message, String context);

    /**
     * 处理用户请求（流式）
     * @param message 用户消息
     * @param context 上下文信息
     * @return 流式响应
     */
    public abstract Flux<String> processStream(String message, String context);

    /**
     * 构建系统提示词
     * @return 系统提示词
     */
    protected abstract String buildSystemPrompt();

    /**
     * 构建完整提示词（包含上下文）
     * @param message 用户消息
     * @param context 上下文
     * @return 完整提示词
     */
    protected String buildFullPrompt(String message, String context) {
        StringBuilder prompt = new StringBuilder();
        prompt.append(buildSystemPrompt());
        if (context != null && !context.isEmpty()) {
            prompt.append("\n\n【上下文信息】\n").append(context);
        }
        prompt.append("\n\n【用户消息】\n").append(message);
        return prompt.toString();
    }
}
