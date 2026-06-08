package com.example.coursesystem.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 学习历史实体类 - 记录学生学习行为
 */
@Data
@Entity
@Table(name = "learning_history")
public class LearningHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "student_id")
    private String studentId;

    /**
     * 学习类型：chat/resource/quiz/path
     */
    @Column(name = "type")
    private String type;

    /**
     * 学习内容（问题/资源名称）
     */
    @Column(name = "content")
    private String content;

    /**
     * AI响应内容
     */
    @Column(name = "response")
    private String response;

    /**
     * 知识点标签
     */
    @Column(name = "knowledge_tags")
    private String knowledgeTags;

    /**
     * 学习时长（秒）
     */
    @Column(name = "duration")
    private Integer duration;

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
