package com.example.coursesystem.repository;

import com.example.coursesystem.entity.KnowledgeMastery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface KnowledgeMasteryRepository extends JpaRepository<KnowledgeMastery, Long> {

    List<KnowledgeMastery> findByStudentIdAndCourseNameOrderByMasteryLevelAsc(String studentId, String courseName);

    Optional<KnowledgeMastery> findByStudentIdAndKnowledgeTag(String studentId, String knowledgeTag);

    @Query("SELECT k FROM KnowledgeMastery k WHERE k.studentId = :studentId AND k.courseName = :courseName " +
           "ORDER BY k.masteryLevel ASC LIMIT :limit")
    List<KnowledgeMastery> findWeakestKnowledge(@Param("studentId") String studentId,
                                                 @Param("courseName") String courseName,
                                                 @Param("limit") int limit);

    @Query("SELECT AVG(k.masteryLevel) FROM KnowledgeMastery k WHERE k.studentId = :studentId AND k.courseName = :courseName")
    Double getAverageMastery(@Param("studentId") String studentId, @Param("courseName") String courseName);
}
