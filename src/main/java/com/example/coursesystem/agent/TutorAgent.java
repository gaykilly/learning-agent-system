package com.example.coursesystem.agent;

import com.example.coursesystem.service.ProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

/**
 * 辅导智能体 - 负责答疑解惑（加分项）
 */
@Component
@Scope("prototype")
public class TutorAgent extends BaseAgent {

    @Autowired
    private ProfileService profileService;

    @Override
    public String getAgentName() {
        return "辅导智能体";
    }

    @Override
    public String getAgentDescription() {
        return "负责为学生提供即时答疑和学习辅导";
    }

    @Override
    public String process(String message, String context) {
        String studentId = extractStudentId(context);
        String profileContext = profileService.getProfileContext(studentId);
        String prompt = buildFullPrompt(message, profileContext);
        return aiService.callAI(buildSystemPrompt(), prompt);
    }

    @Override
    public Flux<String> processStream(String message, String context) {
        String studentId = extractStudentId(context);
        String profileContext = profileService.getProfileContext(studentId);
        String prompt = buildFullPrompt(message, profileContext);
        return aiService.callAIStream(buildSystemPrompt(), prompt);
    }

    @Override
    protected String buildSystemPrompt() {
        return """
                你是「辅导智能体」，负责为学生提供个性化的学习辅导。

                你的职责：
                1. 解答学生的学习疑问
                2. 解释复杂概念，用通俗易懂的语言
                3. 提供示例和类比帮助理解
                4. 引导学生思考，而不是直接给答案
                5. 鼓励学生，建立学习信心

                辅导原则：
                - 根据学生的知识水平调整解释深度
                - 对于视觉型学生：多用图表、流程图说明
                - 对于听觉型学生：多用故事、案例说明
                - 对于实践型学生：多用代码、实操说明
                - 循循善诱，启发式教学
                - 耐心细致，不厌其烦

                输出格式：
                - 使用Markdown格式
                - 重点内容加粗
                - 代码用代码块包裹
                - 必要时使用列表和表格
                """;
    }

    private String extractStudentId(String context) {
        if (context != null && context.contains("studentId:")) {
            return context.split("studentId:")[1].split(";")[0].trim();
        }
        return "default";
    }
}
