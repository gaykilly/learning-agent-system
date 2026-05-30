package com.example.coursesystem.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 知识库实体类 - 存储课程知识点
 */
@Data
@Entity
@Table(name = "knowledge_base")
public class KnowledgeBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 课程名称
     */
    @Column(name = "course_name")
    private String courseName;

    /**
     * 章节编号
     */
    @Column(name = "chapter_no")
    private Integer chapterNo;

    /**
     * 章节名称
     */
    @Column(name = "chapter_name")
    private String chapterName;

    /**
     * 知识点编号
     */
    @Column(name = "knowledge_no")
    private String knowledgeNo;

    /**
     * 知识点名称
     */
    @Column(name = "knowledge_name")
    private String knowledgeName;

    /**
     * 知识点内容（详细描述）
     */
    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    /**
     * 知识点类型：概念/算法/应用/案例
     */
    @Column(name = "type")
    private String type;

    /**
     * 难度等级：1-5
     */
    @Column(name = "difficulty")
    private Integer difficulty;

    /**
     * 关键词（逗号分隔）
     */
    @Column(name = "keywords")
    private String keywords;

    /**
     * 创建时间
     */
    @Column(name = "create_time")
    private LocalDateTime createTime;

    @PrePersist
    public void prePersist() {
        this.createTime = LocalDateTime.now();
    }
}
