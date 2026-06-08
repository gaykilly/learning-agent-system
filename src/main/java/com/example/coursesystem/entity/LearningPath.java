package com.example.coursesystem.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 学习路径实体类 - 存储个性化学习路径
 */
@Data
@Entity
@Table(name = "learning_path")
public class LearningPath {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "student_id")
    private String studentId;

    /**
     * 课程名称
     */
    @Column(name = "course_name")
    private String courseName;

    /**
     * 路径标题
     */
    @Column(name = "title")
    private String title;

    /**
     * 路径描述
     */
    @Column(name = "description", length = 2000)
    private String description;

    /**
     * 路径步骤（JSON格式）
     */
    @Column(name = "steps", columnDefinition = "TEXT")
    private String steps;

    /**
     * 推荐资源（JSON格式）
     */
    @Column(name = "recommended_resources", columnDefinition = "TEXT")
    private String recommendedResources;

    /**
     * 当前进度（0-100）
     */
    @Column(name = "progress")
    private Integer progress;

    /**
     * 状态：active/completed/paused
     */
    @Column(name = "status")
    private String status;

    /**
     * 创建时间
     */
    @Column(name = "create_time")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @Column(name = "update_time")
    private LocalDateTime updateTime;

    @PrePersist
    public void prePersist() {
        this.createTime = LocalDateTime.now();
        this.updateTime = LocalDateTime.now();
        this.progress = 0;
        this.status = "active";
    }

    @PreUpdate
    public void preUpdate() {
        this.updateTime = LocalDateTime.now();
    }
}
