package com.example.coursesystem.repository;

import com.example.coursesystem.entity.LearningHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LearningHistoryRepository extends JpaRepository<LearningHistory, Long> {

    List<LearningHistory> findByStudentIdOrderByCreateTimeDesc(String studentId);

    List<LearningHistory> findByStudentIdAndTypeOrderByCreateTimeDesc(String studentId, String type);
}
