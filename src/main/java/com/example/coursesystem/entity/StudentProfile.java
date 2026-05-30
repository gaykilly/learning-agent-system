package com.example.coursesystem.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 学生画像实体类 - 包含6个维度
 */
@Data
@Entity
@Table(name = "student_profile")
public class StudentProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "student_id", unique = true)
    private String studentId;

    @Column(name = "student_name")
    private String studentName;

    /**
     * 维度1：专业
     */
    @Column(name = "major")
    private String major;

    /**
     * 维度2：知识基础（初级/中级/高级）
     */
    @Column(name = "knowledge_level")
    private String knowledgeLevel;

    /**
     * 维度3：认知风格（视觉型/听觉型/实践型）
     */
    @Column(name = "cognitive_style")
    private String cognitiveStyle;

    /**
     * 维度4：学习偏好（喜欢的学习方式）
     */
    @Column(name = "learning_preference")
    private String learningPreference;

    /**
     * 维度5：易错点（常犯错误类型）
     */
    @Column(name = "weak_points")
    private String weakPoints;

    /**
     * 维度6：学习目标（短期/长期）
     */
    @Column(name = "learning_goal")
    private String learningGoal;

    /**
     * 维度7：学习进度（当前学习阶段）
     */
    @Column(name = "learning_progress")
    private String learningProgress;

    /**
     * 维度8：年级
     */
    @Column(name = "grade")
    private String grade;

    /**
     * 画像更新时间
     */
    @Column(name = "update_time")
    private LocalDateTime updateTime;

    @PrePersist
    public void prePersist() {
        this.updateTime = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updateTime = LocalDateTime.now();
    }
}
