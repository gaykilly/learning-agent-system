package com.example.coursesystem.service;

import com.example.coursesystem.entity.KnowledgeMastery;
import com.example.coursesystem.entity.QuestionRecord;
import com.example.coursesystem.repository.KnowledgeMasteryRepository;
import com.example.coursesystem.repository.QuestionRecordRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 考试冲刺服务 - 核心功能
 */
@Service
public class ExamService {

    @Autowired
    private QuestionRecordRepository questionRecordRepository;

    @Autowired
    private KnowledgeMasteryRepository masteryRepository;

    @Autowired
    private AIService aiService;

    @Autowired
    private KnowledgeService knowledgeService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 保存答题记录并更新掌握度
     */
    public QuestionRecord saveAnswerRecord(QuestionRecord record) {
        // 保存答题记录
        QuestionRecord saved = questionRecordRepository.save(record);

        // 更新知识点掌握度
        updateKnowledgeMastery(record);

        return saved;
    }

    /**
     * 更新知识点掌握度
     */
    private void updateKnowledgeMastery(QuestionRecord record) {
        if (record.getKnowledgeTags() == null) return;

        String[] tags = record.getKnowledgeTags().split(",");
        for (String tag : tags) {
            tag = tag.trim();
            if (tag.isEmpty()) continue;

            KnowledgeMastery mastery = masteryRepository
                .findByStudentIdAndKnowledgeTag(record.getStudentId(), tag)
                .orElseGet(() -> {
                    KnowledgeMastery m = new KnowledgeMastery();
                    m.setStudentId(record.getStudentId());
                    m.setCourseName(record.getCourseName());
                    m.setKnowledgeTag(tag);
                    m.setKnowledgeName(tag);
                    m.setMasteryLevel(0);
                    m.setTotalAttempts(0);
                    m.setCorrectCount(0);
                    m.setStreak(0);
                    return m;
                });

            mastery.setTotalAttempts(mastery.getTotalAttempts() + 1);
            mastery.setLastAttemptTime(LocalDateTime.now());

            if (Boolean.TRUE.equals(record.getIsCorrect())) {
                mastery.setCorrectCount(mastery.getCorrectCount() + 1);
                mastery.setStreak(mastery.getStreak() + 1);
            } else {
                mastery.setStreak(0);
            }

            // 计算掌握度：正确率 * 70% + 连续正确 * 30%
            double accuracy = (double) mastery.getCorrectCount() / mastery.getTotalAttempts();
            double streakBonus = Math.min(mastery.getStreak() / 5.0, 1.0);
            int newLevel = (int) (accuracy * 70 + streakBonus * 30);
            mastery.setMasteryLevel(Math.min(newLevel, 100));

            masteryRepository.save(mastery);
        }
    }

    /**
     * 获取学生薄弱知识点（掌握度最低的N个）
     */
    public List<KnowledgeMastery> getWeakestKnowledge(String studentId, String courseName, int limit) {
        return masteryRepository.findWeakestKnowledge(studentId, courseName, limit);
    }

    /**
     * 获取知识点掌握度列表
     */
    public List<KnowledgeMastery> getKnowledgeMasteryList(String studentId, String courseName) {
        return masteryRepository.findByStudentIdAndCourseNameOrderByMasteryLevelAsc(studentId, courseName);
    }

    /**
     * 获取平均掌握度
     */
    public Double getAverageMastery(String studentId, String courseName) {
        Double avg = masteryRepository.getAverageMastery(studentId, courseName);
        return avg != null ? avg : 0.0;
    }

    /**
     * 获取错题列表
     */
    public List<QuestionRecord> getWrongQuestions(String studentId, String courseName) {
        return questionRecordRepository.findByStudentIdAndIsCorrectOrderByAnswerTimeDesc(studentId, false)
            .stream()
            .filter(q -> courseName == null || courseName.equals(q.getCourseName()))
            .collect(Collectors.toList());
    }

    /**
     * 获取待复习题目（间隔重复）
     */
    public List<QuestionRecord> getQuestionsForReview(String studentId) {
        return questionRecordRepository.findQuestionsForReview(studentId, LocalDateTime.now());
    }

    /**
     * 生成模拟试卷
     */
    public String generateMockExam(String studentId, String courseName, int questionCount) {
        // 1. 获取薄弱知识点
        List<KnowledgeMastery> weakPoints = getWeakestKnowledge(studentId, courseName, 5);

        // 2. 获取知识库内容
        String knowledgeContext = knowledgeService.getCourseContext(courseName);

        // 3. 构建提示词
        StringBuilder weakTags = new StringBuilder();
        for (KnowledgeMastery km : weakPoints) {
            weakTags.append("- ").append(km.getKnowledgeName())
                    .append("（掌握度：").append(km.getMasteryLevel()).append("%）\n");
        }

        String prompt = String.format("""
                请为学生生成一份模拟期末试卷。

                【课程】%s

                【学生薄弱知识点】
                %s

                【知识库参考】
                %s

                【要求】
                1. 题目数量：%d道
                2. 题型分布：选择题40%%、填空题20%%、简答题40%%
                3. 重点覆盖薄弱知识点
                4. 难度适中，符合期末考试水平
                5. 每道题标注涉及的知识点
                6. 提供参考答案和评分标准

                请使用Markdown格式输出。
                """, courseName, weakTags.toString(), knowledgeContext, questionCount);

        return aiService.callAI("你是大学课程考试命题专家", prompt);
    }

    /**
     * 生成考前冲刺计划
     */
    public String generateStudyPlan(String studentId, String courseName, int daysUntilExam) {
        List<KnowledgeMastery> masteryList = getKnowledgeMasteryList(studentId, courseName);
        Double avgMastery = getAverageMastery(studentId, courseName);

        StringBuilder masteryInfo = new StringBuilder();
        for (KnowledgeMastery km : masteryList) {
            String level = km.getMasteryLevel() >= 80 ? "已掌握" :
                          km.getMasteryLevel() >= 60 ? "基本掌握" : "需加强";
            masteryInfo.append("- ").append(km.getKnowledgeName())
                       .append("：").append(km.getMasteryLevel()).append("%（").append(level).append("）\n");
        }

        String prompt = String.format("""
                请为学生制定一份%d天的考前冲刺计划。

                【课程】%s
                【当前平均掌握度】%.1f%%

                【知识点掌握情况】
                %s

                【要求】
                1. 每天的学习任务具体可执行
                2. 优先复习薄弱知识点
                3. 包含复习方法建议
                4. 包含每日练习题量建议
                5. 考前一天安排总复习
                6. 给出时间分配建议

                请使用Markdown格式输出，包含表格。
                """, daysUntilExam, courseName, avgMastery, masteryInfo.toString());

        return aiService.callAI("你是学习规划专家", prompt);
    }

    /**
     * 获取学习统计数据
     */
    public ObjectNode getLearningStats(String studentId, String courseName) {
        ObjectNode stats = objectMapper.createObjectNode();

        // 总答题数
        long totalQuestions = questionRecordRepository.countTotalQuestions(studentId);
        stats.put("totalQuestions", totalQuestions);

        // 正确数
        long correctQuestions = questionRecordRepository.countCorrectQuestions(studentId);
        stats.put("correctQuestions", correctQuestions);

        // 正确率
        double accuracy = totalQuestions > 0 ? (double) correctQuestions / totalQuestions * 100 : 0;
        stats.put("accuracy", Math.round(accuracy * 10) / 10.0);

        // 平均掌握度
        Double avgMastery = getAverageMastery(studentId, courseName);
        stats.put("averageMastery", avgMastery != null ? Math.round(avgMastery * 10) / 10.0 : 0);

        // 待复习题数
        List<QuestionRecord> reviewQuestions = getQuestionsForReview(studentId);
        stats.put("reviewCount", reviewQuestions.size());

        // 错题数
        List<QuestionRecord> wrongQuestions = getWrongQuestions(studentId, courseName);
        stats.put("wrongCount", wrongQuestions.size());

        return stats;
    }
}
