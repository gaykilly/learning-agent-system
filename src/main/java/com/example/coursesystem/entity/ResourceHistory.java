package com.example.coursesystem.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "resource_history")
@Data
public class ResourceHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String type; // syllabus/ppt/exercise

    @Column(nullable = false, columnDefinition = "TEXT")
    private String inputText;

    @Column(nullable = false, columnDefinition = "LONGTEXT")
    private String outputText;

    @Column(updatable = false)
    private LocalDateTime createTime = LocalDateTime.now();
}