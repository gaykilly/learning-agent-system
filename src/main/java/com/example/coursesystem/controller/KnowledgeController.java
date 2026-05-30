package com.example.coursesystem.controller;

import com.example.coursesystem.entity.KnowledgeBase;
import com.example.coursesystem.service.KnowledgeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/knowledge")
@CrossOrigin(origins = "*")
public class KnowledgeController {

    @Autowired
    private KnowledgeService knowledgeService;

    /**
     * 获取所有课程名称
     */
    @GetMapping("/courses")
    public List<String> getAllCourseNames() {
        return knowledgeService.getAllCourseNames();
    }

    /**
     * 获取课程的所有知识点
     */
    @GetMapping("/list")
    public List<KnowledgeBase> getKnowledgeByCourse(@RequestParam String courseName) {
        return knowledgeService.getKnowledgeByCourse(courseName);
    }

    /**
     * 搜索知识点
     */
    @GetMapping("/search")
    public List<KnowledgeBase> searchKnowledge(
            @RequestParam String courseName,
            @RequestParam String keyword) {
        return knowledgeService.searchKnowledge(courseName, keyword);
    }

    /**
     * 添加知识点
     */
    @PostMapping("/add")
    public KnowledgeBase addKnowledge(@RequestBody KnowledgeBase knowledge) {
        return knowledgeService.save(knowledge);
    }

    /**
     * 批量添加知识点
     */
    @PostMapping("/batch-add")
    public List<KnowledgeBase> batchAddKnowledge(@RequestBody List<KnowledgeBase> knowledgeList) {
        return knowledgeService.saveAll(knowledgeList);
    }
}
