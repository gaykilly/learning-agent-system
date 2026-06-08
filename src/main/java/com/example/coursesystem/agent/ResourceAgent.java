package com.example.coursesystem.agent;

import com.example.coursesystem.service.KnowledgeService;
import com.example.coursesystem.service.ProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

/**
 * 资源智能体 - 负责生成个性化学习资源
 */
@Component
@Scope("prototype")
public class ResourceAgent extends BaseAgent {

    @Autowired
    private ProfileService profileService;

    @Autowired
    private KnowledgeService knowledgeService;

    @Override
    public String getAgentName() {
        return "资源智能体";
    }

    @Override
    public String getAgentDescription() {
        return "负责根据学生画像生成个性化学习资源（教学大纲、PPT、练习题、思维导图、复习提纲）";
    }

    @Override
    public String process(String message, String context) {
        String prompt = buildFullPrompt(message, context);
        return aiService.callAI(buildSystemPrompt(), prompt);
    }

    @Override
    public Flux<String> processStream(String message, String context) {
        String prompt = buildFullPrompt(message, context);
        return aiService.callAIStream(buildSystemPrompt(), prompt);
    }

    /**
     * 生成特定类型的资源
     */
    public String generateResource(String type, String topic, String studentId) {
        String profileContext = profileService.getProfileContext(studentId);
        String prompt = buildResourcePrompt(type, topic, profileContext);
        return aiService.callAI(buildSystemPrompt(), prompt);
    }

    /**
     * 流式生成特定类型的资源
     */
    public Flux<String> generateResourceStream(String type, String topic, String studentId) {
        String profileContext = profileService.getProfileContext(studentId);
        String prompt = buildResourcePrompt(type, topic, profileContext);
        return aiService.callAIStream(buildSystemPrompt(), prompt);
    }

    @Override
    protected String buildSystemPrompt() {
        return """
                你是「资源智能体」，负责生成高质量的学习资源。

                你的能力：
                1. 生成教学大纲 - 包含课程目标、章节内容、考核方式等
                2. 生成PPT大纲 - 结构化的课件内容框架
                3. 生成练习题 - 选择题、填空题、简答题等
                4. 生成思维导图 - 知识点层级结构
                5. 生成复习提纲 - 核心考点总结

                生成要求：
                - 根据学生画像调整内容难度和风格
                - 视觉型学生：多用图表、结构化展示
                - 听觉型学生：多用解释性文字、案例
                - 实践型学生：多用代码示例、实操案例
                - 内容准确、结构清晰、格式规范
                - 使用Markdown格式输出
                """;
    }

    private String buildResourcePrompt(String type, String topic, String profileContext) {
        String typePrompt = switch (type) {
            case "syllabus" -> "请生成一份完整的高校教学大纲";
            case "ppt" -> "请生成一份完整的PPT课件大纲";
            case "exercise" -> "请生成一套配套练习题";
            case "mindmap" -> "请生成思维导图结构";
            case "review" -> "请生成期末复习提纲";
            default -> "请生成学习资源";
        };

        // 获取知识库上下文
        String knowledgeContext = knowledgeService.getKnowledgeDetailContext("人工智能导论", topic);

        return String.format("""
                %s

                【学生画像】
                %s

                【知识库参考】
                %s

                【主题/知识点】
                %s

                请根据学生画像和知识库内容个性化生成内容。
                """, typePrompt, profileContext, knowledgeContext, topic);
    }
}
