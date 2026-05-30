package com.example.coursesystem.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 知识点掌握度实体类
 */
@Data
@Entity
@Table(name = "knowledge_mastery")
public class KnowledgeMastery {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "student_id")
    private String studentId;

    @Column(name = "course_name")
    private String courseName;

    /**
     * 知识点名称
     */
    @Column(name = "knowledge_name")
    private String knowledgeName;

    /**
     * 知识点标签
     */
    @Column(name = "knowledge_tag")
    private String knowledgeTag;

    /**
     * 掌握度（0-100）
     */
    @Column(name = "mastery_level")
    private Integer masteryLevel;

    /**
     * 答题总数
     */
    @Column(name = "total_attempts")
    private Integer totalAttempts;

    /**
     * 正确数
     */
    @Column(name = "correct_count")
    private Integer correctCount;

    /**
     * 最后答题时间
     */
    @Column(name = "last_attempt_time")
    private LocalDateTime lastAttemptTime;

    /**
     * 连续正确次数
     */
    @Column(name = "streak")
    private Integer streak;

    @PrePersist
    public void prePersist() {
        this.masteryLevel = 0;
        this.totalAttempts = 0;
        this.correctCount = 0;
        this.streak = 0;
    }
}
