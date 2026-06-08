package com.example.coursesystem.controller;

import com.example.coursesystem.agent.PathAgent;
import com.example.coursesystem.entity.LearningPath;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;

/**
 * 学习路径控制器
 */
@RestController
@RequestMapping("/api/path")
@CrossOrigin(origins = "*")
public class LearningPathController {

    @Autowired
    private PathAgent pathAgent;

    /**
     * 生成学习路径
     */
    @PostMapping("/generate")
    public LearningPath generatePath(@RequestBody Map<String, String> request) {
        String studentId = request.getOrDefault("studentId", "default");
        String courseName = request.get("courseName");
        return pathAgent.generatePath(studentId, courseName);
    }

    /**
     * 流式生成学习路径
     */
    @GetMapping(value = "/generate/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> generatePathStream(
            @RequestParam String studentId,
            @RequestParam String courseName) {
        String context = "studentId:" + studentId + ";courseName:" + courseName;
        return pathAgent.processStream("请为我规划「" + courseName + "」的学习路径", context);
    }

    /**
     * 获取学生的学习路径列表
     */
    @GetMapping("/list")
    public List<LearningPath> getPaths(@RequestParam String studentId) {
        return pathAgent.getStudentPaths(studentId);
    }

    /**
     * 获取学生的活跃学习路径
     */
    @GetMapping("/active")
    public List<LearningPath> getActivePaths(@RequestParam String studentId) {
        return pathAgent.getActivePaths(studentId);
    }
}
