package com.example.coursesystem.repository;

import com.example.coursesystem.entity.LearningPath;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LearningPathRepository extends JpaRepository<LearningPath, Long> {

    List<LearningPath> findByStudentIdOrderByCreateTimeDesc(String studentId);

    List<LearningPath> findByStudentIdAndStatusOrderByCreateTimeDesc(String studentId, String status);
}
