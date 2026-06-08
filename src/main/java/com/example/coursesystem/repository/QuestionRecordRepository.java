package com.example.coursesystem.repository;

import com.example.coursesystem.entity.QuestionRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface QuestionRecordRepository extends JpaRepository<QuestionRecord, Long> {

    List<QuestionRecord> findByStudentIdOrderByAnswerTimeDesc(String studentId);

    List<QuestionRecord> findByStudentIdAndIsCorrectOrderByAnswerTimeDesc(String studentId, Boolean isCorrect);

    List<QuestionRecord> findByStudentIdAndCourseNameOrderByAnswerTimeDesc(String studentId, String courseName);

    @Query("SELECT q FROM QuestionRecord q WHERE q.studentId = :studentId AND q.isCorrect = false " +
           "AND q.knowledgeTags LIKE %:tag% ORDER BY q.answerTime DESC")
    List<QuestionRecord> findWrongQuestionsByTag(@Param("studentId") String studentId, @Param("tag") String tag);

    @Query("SELECT q FROM QuestionRecord q WHERE q.studentId = :studentId AND q.nextReviewTime <= :now " +
           "ORDER BY q.nextReviewTime ASC")
    List<QuestionRecord> findQuestionsForReview(@Param("studentId") String studentId, @Param("now") LocalDateTime now);

    @Query("SELECT COUNT(q) FROM QuestionRecord q WHERE q.studentId = :studentId AND q.isCorrect = true")
    long countCorrectQuestions(@Param("studentId") String studentId);

    @Query("SELECT COUNT(q) FROM QuestionRecord q WHERE q.studentId = :studentId")
    long countTotalQuestions(@Param("studentId") String studentId);
}
