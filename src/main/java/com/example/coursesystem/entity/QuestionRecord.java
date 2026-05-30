package com.example.coursesystem.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 答题记录实体类
 */
@Data
@Entity
@Table(name = "question_record")
public class QuestionRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "student_id")
    private String studentId;

    /**
     * 题目内容
     */
    @Column(name = "question", columnDefinition = "TEXT")
    private String question;

    /**
     * 题目类型：choice/fill/short_answer
     */
    @Column(name = "question_type")
    private String questionType;

    /**
     * 学生答案
     */
    @Column(name = "student_answer", columnDefinition = "TEXT")
    private String studentAnswer;

    /**
     * 正确答案
     */
    @Column(name = "correct_answer", columnDefinition = "TEXT")
    private String correctAnswer;

    /**
     * 是否正确
     */
    @Column(name = "is_correct")
    private Boolean isCorrect;

    /**
     * 知识点标签（逗号分隔）
     */
    @Column(name = "knowledge_tags")
    private String knowledgeTags;

    /**
     * 难度等级：1-5
     */
    @Column(name = "difficulty")
    private Integer difficulty;

    /**
     * 所属课程
     */
    @Column(name = "course_name")
    private String courseName;

    /**
     * 答题时间
     */
    @Column(name = "answer_time")
    private LocalDateTime answerTime;

    /**
     * 复习次数
     */
    @Column(name = "review_count")
    private Integer reviewCount;

    /**
     * 下次复习时间（间隔重复）
     */
    @Column(name = "next_review_time")
    private LocalDateTime nextReviewTime;

    @PrePersist
    public void prePersist() {
        this.answerTime = LocalDateTime.now();
        this.reviewCount = 0;
        this.nextReviewTime = LocalDateTime.now().plusDays(1);
    }
}
