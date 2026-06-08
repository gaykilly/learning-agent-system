package com.example.coursesystem.controller;

import com.example.coursesystem.entity.StudentProfile;
import com.example.coursesystem.service.ProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 画像控制器 - 管理学生画像
 */
@RestController
@RequestMapping("/api/profile")
@CrossOrigin(origins = "*")
public class ProfileController {

    @Autowired
    private ProfileService profileService;

    /**
     * 获取或创建学生画像
     */
    @GetMapping("/get")
    public StudentProfile getProfile(
            @RequestParam String studentId,
            @RequestParam(defaultValue = "学生") String studentName) {
        return profileService.getOrCreateProfile(studentId, studentName);
    }

    /**
     * 更新学生画像
     */
    @PostMapping("/update")
    public StudentProfile updateProfile(@RequestBody Map<String, String> request) {
        String studentId = request.get("studentId");
        String chatMessage = request.get("message");
        return profileService.updateProfileFromChat(studentId, chatMessage);
    }

    /**
     * 手动更新画像字段
     */
    @PostMapping("/updateField")
    public StudentProfile updateField(@RequestBody Map<String, String> request) {
        String studentId = request.get("studentId");
        String field = request.get("field");
        String value = request.get("value");

        StudentProfile profile = profileService.getOrCreateProfile(studentId, "学生");

        switch (field) {
            case "major" -> profile.setMajor(value);
            case "knowledgeLevel" -> profile.setKnowledgeLevel(value);
            case "cognitiveStyle" -> profile.setCognitiveStyle(value);
            case "learningPreference" -> profile.setLearningPreference(value);
            case "weakPoints" -> profile.setWeakPoints(value);
            case "learningGoal" -> profile.setLearningGoal(value);
            case "learningProgress" -> profile.setLearningProgress(value);
            case "grade" -> profile.setGrade(value);
        }

        return profileService.updateProfileFromChat(studentId, "手动更新" + field + "为" + value);
    }

    /**
     * 获取画像上下文
     */
    @GetMapping("/context")
    public Map<String, String> getProfileContext(@RequestParam String studentId) {
        return Map.of("context", profileService.getProfileContext(studentId));
    }
}
