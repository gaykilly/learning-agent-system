package com.example.coursesystem.service;

import com.example.coursesystem.entity.StudentProfile;
import com.example.coursesystem.repository.StudentProfileRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 画像服务 - 处理学生画像相关业务
 */
@Service
public class ProfileService {

    @Autowired
    private StudentProfileRepository profileRepository;

    @Autowired
    private AIService aiService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 获取或创建学生画像
     */
    public StudentProfile getOrCreateProfile(String studentId, String studentName) {
        Optional<StudentProfile> existing = profileRepository.findByStudentId(studentId);
        if (existing.isPresent()) {
            return existing.get();
        }

        StudentProfile profile = new StudentProfile();
        profile.setStudentId(studentId);
        profile.setStudentName(studentName);
        profile.setKnowledgeLevel("待评估");
        profile.setCognitiveStyle("待分析");
        profile.setLearningPreference("待了解");
        profile.setWeakPoints("待发现");
        profile.setLearningGoal("待设定");
        profile.setLearningProgress("刚开始");
        return profileRepository.save(profile);
    }

    /**
     * 通过对话更新画像
     */
    public StudentProfile updateProfileFromChat(String studentId, String chatMessage) {
        Optional<StudentProfile> opt = profileRepository.findByStudentId(studentId);
        if (opt.isEmpty()) {
            return null;
        }

        StudentProfile profile = opt.get();

        // 构建提示词，让AI从对话中提取画像信息
        String prompt = String.format("""
                请分析以下学生对话，提取学习画像信息。

                当前画像：
                - 专业：%s
                - 知识基础：%s
                - 认知风格：%s
                - 学习偏好：%s
                - 易错点：%s
                - 学习目标：%s
                - 学习进度：%s
                - 年级：%s

                学生对话内容：
                %s

                请以JSON格式返回更新后的画像信息，只返回有变化的字段：
                {
                    "major": "专业",
                    "knowledgeLevel": "知识基础",
                    "cognitiveStyle": "认知风格",
                    "learningPreference": "学习偏好",
                    "weakPoints": "易错点",
                    "learningGoal": "学习目标",
                    "learningProgress": "学习进度",
                    "grade": "年级"
                }

                如果某个字段没有变化，不要包含在返回结果中。只返回JSON，不要其他内容。
                """,
                profile.getMajor(),
                profile.getKnowledgeLevel(),
                profile.getCognitiveStyle(),
                profile.getLearningPreference(),
                profile.getWeakPoints(),
                profile.getLearningGoal(),
                profile.getLearningProgress(),
                profile.getGrade(),
                chatMessage
        );

        String result = aiService.callAI("你是学习画像分析专家", prompt);

        try {
            // 解析JSON结果
            JsonNode json = objectMapper.readTree(result);

            if (json.has("major")) profile.setMajor(json.get("major").asText());
            if (json.has("knowledgeLevel")) profile.setKnowledgeLevel(json.get("knowledgeLevel").asText());
            if (json.has("cognitiveStyle")) profile.setCognitiveStyle(json.get("cognitiveStyle").asText());
            if (json.has("learningPreference")) profile.setLearningPreference(json.get("learningPreference").asText());
            if (json.has("weakPoints")) profile.setWeakPoints(json.get("weakPoints").asText());
            if (json.has("learningGoal")) profile.setLearningGoal(json.get("learningGoal").asText());
            if (json.has("learningProgress")) profile.setLearningProgress(json.get("learningProgress").asText());
            if (json.has("grade")) profile.setGrade(json.get("grade").asText());

            profile.setUpdateTime(LocalDateTime.now());
            return profileRepository.save(profile);
        } catch (Exception e) {
            // JSON解析失败，不更新画像
            e.printStackTrace();
            return profile;
        }
    }

    /**
     * 获取画像上下文（用于智能体）
     */
    public String getProfileContext(String studentId) {
        Optional<StudentProfile> opt = profileRepository.findByStudentId(studentId);
        if (opt.isEmpty()) {
            return "暂无学生画像信息";
        }

        StudentProfile p = opt.get();
        return String.format("""
                学生画像：
                - 姓名：%s
                - 专业：%s
                - 年级：%s
                - 知识基础：%s
                - 认知风格：%s
                - 学习偏好：%s
                - 易错点：%s
                - 学习目标：%s
                - 学习进度：%s
                """,
                p.getStudentName(),
                p.getMajor(),
                p.getGrade(),
                p.getKnowledgeLevel(),
                p.getCognitiveStyle(),
                p.getLearningPreference(),
                p.getWeakPoints(),
                p.getLearningGoal(),
                p.getLearningProgress()
        );
    }
}
