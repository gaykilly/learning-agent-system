package com.example.coursesystem.agent;

import com.example.coursesystem.entity.StudentProfile;
import com.example.coursesystem.service.ProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

/**
 * 画像智能体 - 负责学生画像构建和更新
 */
@Component
@Scope("prototype")
public class ProfileAgent extends BaseAgent {

    @Autowired
    private ProfileService profileService;

    @Override
    public String getAgentName() {
        return "画像智能体";
    }

    @Override
    public String getAgentDescription() {
        return "负责通过对话分析学生特征，构建和更新学习画像";
    }

    @Override
    public String process(String message, String context) {
        String studentId = extractStudentId(context);
        String profileContext = profileService.getProfileContext(studentId);

        String prompt = buildFullPrompt(message, profileContext);
        String response = aiService.callAI(buildSystemPrompt(), prompt);

        // 更新画像
        profileService.updateProfileFromChat(studentId, message);

        return response;
    }

    @Override
    public Flux<String> processStream(String message, String context) {
        String studentId = extractStudentId(context);
        String profileContext = profileService.getProfileContext(studentId);

        String prompt = buildFullPrompt(message, profileContext);

        // 异步更新画像
        new Thread(() -> profileService.updateProfileFromChat(studentId, message)).start();

        return aiService.callAIStream(buildSystemPrompt(), prompt);
    }

    @Override
    protected String buildSystemPrompt() {
        return """
                你是「画像智能体」，负责了解学生并构建学习画像。

                你的职责：
                1. 通过自然对话了解学生的基本信息（专业、年级等）
                2. 分析学生的知识基础水平
                3. 识别学生的认知风格（视觉型/听觉型/实践型）
                4. 了解学生的学习偏好
                5. 发现学生的薄弱点和易错点
                6. 明确学生的学习目标

                对话要求：
                - 语气亲切自然，像朋友聊天一样
                - 不要一次问太多问题，每次聚焦1-2个维度
                - 根据学生回答灵活调整后续问题
                - 适时总结已获取的信息，让学生确认

                当你获取到足够信息后，告诉学生画像已建立完成，可以开始学习了。
                """;
    }

    private String extractStudentId(String context) {
        if (context != null && context.contains("studentId:")) {
            return context.split("studentId:")[1].split(";")[0].trim();
        }
        return "default";
    }
}
