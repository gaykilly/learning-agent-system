package com.example.coursesystem.controller;

import com.example.coursesystem.entity.KnowledgeMastery;
import com.example.coursesystem.entity.QuestionRecord;
import com.example.coursesystem.service.ExamService;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;

/**
 * 考试冲刺控制器
 */
@RestController
@RequestMapping("/api/exam")
@CrossOrigin(origins = "*")
public class ExamController {

    @Autowired
    private ExamService examService;

    /**
     * 保存答题记录
     */
    @PostMapping("/answer")
    public QuestionRecord saveAnswer(@RequestBody QuestionRecord record) {
        return examService.saveAnswerRecord(record);
    }

    /**
     * 获取薄弱知识点
     */
    @GetMapping("/weak-points")
    public List<KnowledgeMastery> getWeakPoints(
            @RequestParam String studentId,
            @RequestParam String courseName,
            @RequestParam(defaultValue = "5") int limit) {
        return examService.getWeakestKnowledge(studentId, courseName, limit);
    }

    /**
     * 获取知识点掌握度列表
     */
    @GetMapping("/mastery")
    public List<KnowledgeMastery> getKnowledgeMastery(
            @RequestParam String studentId,
            @RequestParam String courseName) {
        return examService.getKnowledgeMasteryList(studentId, courseName);
    }

    /**
     * 获取错题列表
     */
    @GetMapping("/wrong-questions")
    public List<QuestionRecord> getWrongQuestions(
            @RequestParam String studentId,
            @RequestParam(required = false) String courseName) {
        return examService.getWrongQuestions(studentId, courseName);
    }

    /**
     * 获取待复习题目
     */
    @GetMapping("/review")
    public List<QuestionRecord> getReviewQuestions(@RequestParam String studentId) {
        return examService.getQuestionsForReview(studentId);
    }

    /**
     * 生成模拟试卷（流式）
     */
    @GetMapping(value = "/mock-exam/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> generateMockExamStream(
            @RequestParam String studentId,
            @RequestParam String courseName,
            @RequestParam(defaultValue = "20") int questionCount) {
        // 使用ResourceAgent的流式能力
        return Flux.fromCallable(() -> examService.generateMockExam(studentId, courseName, questionCount))
            .flatMap(content -> Flux.fromArray(content.split("(?<=\\n)")));
    }

    /**
     * 生成模拟试卷（非流式）
     */
    @PostMapping("/mock-exam")
    public Map<String, String> generateMockExam(@RequestBody Map<String, Object> request) {
        String studentId = (String) request.get("studentId");
        String courseName = (String) request.get("courseName");
        int questionCount = request.containsKey("questionCount") ?
            ((Number) request.get("questionCount")).intValue() : 20;

        String content = examService.generateMockExam(studentId, courseName, questionCount);
        return Map.of("content", content);
    }

    /**
     * 生成考前冲刺计划
     */
    @PostMapping("/study-plan")
    public Map<String, String> generateStudyPlan(@RequestBody Map<String, Object> request) {
        String studentId = (String) request.get("studentId");
        String courseName = (String) request.get("courseName");
        int days = request.containsKey("days") ? ((Number) request.get("days")).intValue() : 7;

        String plan = examService.generateStudyPlan(studentId, courseName, days);
        return Map.of("plan", plan);
    }

    /**
     * 获取学习统计数据
     */
    @GetMapping("/stats")
    public ObjectNode getLearningStats(
            @RequestParam String studentId,
            @RequestParam String courseName) {
        return examService.getLearningStats(studentId, courseName);
    }
}
