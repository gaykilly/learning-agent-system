package com.example.coursesystem.service;

import com.example.coursesystem.entity.KnowledgeBase;
import com.example.coursesystem.repository.KnowledgeBaseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class KnowledgeService {

    @Autowired
    private KnowledgeBaseRepository knowledgeRepository;

    /**
     * 获取所有课程名称
     */
    public List<String> getAllCourseNames() {
        return knowledgeRepository.findAllCourseNames();
    }

    /**
     * 获取课程的所有知识点
     */
    public List<KnowledgeBase> getKnowledgeByCourse(String courseName) {
        return knowledgeRepository.findByCourseNameOrderByChapterNoAscKnowledgeNoAsc(courseName);
    }

    /**
     * 获取指定章节的知识点
     */
    public List<KnowledgeBase> getKnowledgeByChapter(String courseName, Integer chapterNo) {
        return knowledgeRepository.findByCourseNameAndChapterNoOrderByKnowledgeNoAsc(courseName, chapterNo);
    }

    /**
     * 搜索知识点
     */
    public List<KnowledgeBase> searchKnowledge(String courseName, String keyword) {
        return knowledgeRepository.searchByKeyword(courseName, keyword);
    }

    /**
     * 获取课程知识上下文（用于AI对话）
     */
    public String getCourseContext(String courseName) {
        List<KnowledgeBase> knowledgeList = getKnowledgeByCourse(courseName);
        if (knowledgeList.isEmpty()) {
            return "暂无该课程的知识库数据";
        }

        StringBuilder context = new StringBuilder();
        context.append("【").append(courseName).append("】课程知识库：\n\n");

        int currentChapter = -1;
        for (KnowledgeBase k : knowledgeList) {
            if (k.getChapterNo() != currentChapter) {
                currentChapter = k.getChapterNo();
                context.append("第").append(currentChapter).append("章：").append(k.getChapterName()).append("\n");
            }
            context.append("  - ").append(k.getKnowledgeNo()).append(" ").append(k.getKnowledgeName());
            if (k.getType() != null) {
                context.append(" [").append(k.getType()).append("]");
            }
            context.append("\n");
        }

        return context.toString();
    }

    /**
     * 获取知识点详情上下文
     */
    public String getKnowledgeDetailContext(String courseName, String keyword) {
        List<KnowledgeBase> results = searchKnowledge(courseName, keyword);
        if (results.isEmpty()) {
            return "未找到相关知识点";
        }

        StringBuilder context = new StringBuilder();
        for (KnowledgeBase k : results) {
            context.append("【").append(k.getKnowledgeName()).append("】\n");
            context.append("类型：").append(k.getType()).append("\n");
            context.append("难度：").append(k.getDifficulty()).append("/5\n");
            context.append("内容：").append(k.getContent()).append("\n\n");
        }

        return context.toString();
    }

    /**
     * 保存知识点
     */
    public KnowledgeBase save(KnowledgeBase knowledge) {
        return knowledgeRepository.save(knowledge);
    }

    /**
     * 批量保存知识点
     */
    public List<KnowledgeBase> saveAll(List<KnowledgeBase> knowledgeList) {
        return knowledgeRepository.saveAll(knowledgeList);
    }
}
