package com.example.coursesystem.agent;

import com.example.coursesystem.entity.LearningHistory;
import com.example.coursesystem.repository.LearningHistoryRepository;
import com.example.coursesystem.service.ProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * 评估智能体 - 负责学习效果评估（加分项）
 */
@Component
@Scope("prototype")
public class EvalAgent extends BaseAgent {

    @Autowired
    private ProfileService profileService;

    @Autowired
    private LearningHistoryRepository historyRepository;

    @Override
    public String getAgentName() {
        return "评估智能体";
    }

    @Override
    public String getAgentDescription() {
        return "负责评估学生学习效果，提供学习建议";
    }

    @Override
    public String process(String message, String context) {
        String studentId = extractStudentId(context);
        String profileContext = profileService.getProfileContext(studentId);
        String historyContext = getHistoryContext(studentId);
        String fullContext = profileContext + "\n" + historyContext;

        String prompt = buildFullPrompt(message, fullContext);
        return aiService.callAI(buildSystemPrompt(), prompt);
    }

    @Override
    public Flux<String> processStream(String message, String context) {
        String studentId = extractStudentId(context);
        String profileContext = profileService.getProfileContext(studentId);
        String historyContext = getHistoryContext(studentId);
        String fullContext = profileContext + "\n" + historyContext;

        String prompt = buildFullPrompt(message, fullContext);
        return aiService.callAIStream(buildSystemPrompt(), prompt);
    }

    /**
     * 生成学习报告
     */
    public String generateReport(String studentId) {
        String profileContext = profileService.getProfileContext(studentId);
        String historyContext = getHistoryContext(studentId);

        String prompt = String.format("""
                请根据以下信息生成一份详细的学习效果评估报告。

                【学生画像】
                %s

                【学习历史】
                %s

                请从以下维度进行评估：
                1. 知识掌握程度
                2. 学习态度和投入度
                3. 薄弱环节分析
                4. 进步趋势
                5. 改进建议

                使用Markdown格式输出，包含图表建议。
                """, profileContext, historyContext);

        return aiService.callAI(buildSystemPrompt(), prompt);
    }

    @Override
    protected String buildSystemPrompt() {
        return """
                你是「评估智能体」，负责评估学生的学习效果。

                你的职责：
                1. 分析学生的学习历史和行为
                2. 评估知识掌握程度
                3. 识别学习中的问题和不足
                4. 提供改进建议和学习策略
                5. 生成可视化学习报告

                评估原则：
                - 客观公正，数据驱动
                - 注重进步和成长
                - 提出具体可行的建议
                - 鼓励为主，批评为辅
                - 关注学习效率和效果

                输出格式：
                - 使用Markdown格式
                - 包含数据统计
                - 提供可视化建议（图表类型）
                - 给出明确的改进方向
                """;
    }

    private String getHistoryContext(String studentId) {
        List<LearningHistory> histories = historyRepository.findByStudentIdOrderByCreateTimeDesc(studentId);
        if (histories.isEmpty()) {
            return "暂无学习历史记录";
        }

        StringBuilder sb = new StringBuilder("最近学习记录：\n");
        int count = Math.min(histories.size(), 10);
        for (int i = 0; i < count; i++) {
            LearningHistory h = histories.get(i);
            sb.append(String.format("- [%s] %s\n", h.getType(), h.getContent()));
        }
        return sb.toString();
    }

    private String extractStudentId(String context) {
        if (context != null && context.contains("studentId:")) {
            return context.split("studentId:")[1].split(";")[0].trim();
        }
        return "default";
    }
}
