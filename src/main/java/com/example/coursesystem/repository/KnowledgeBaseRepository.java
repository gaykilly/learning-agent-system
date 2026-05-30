package com.example.coursesystem.repository;

import com.example.coursesystem.entity.KnowledgeBase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface KnowledgeBaseRepository extends JpaRepository<KnowledgeBase, Long> {

    List<KnowledgeBase> findByCourseNameOrderByChapterNoAscKnowledgeNoAsc(String courseName);

    List<KnowledgeBase> findByCourseNameAndChapterNoOrderByKnowledgeNoAsc(String courseName, Integer chapterNo);

    @Query("SELECT k FROM KnowledgeBase k WHERE k.courseName = :courseName AND " +
           "(k.knowledgeName LIKE %:keyword% OR k.keywords LIKE %:keyword%)")
    List<KnowledgeBase> searchByKeyword(@Param("courseName") String courseName, @Param("keyword") String keyword);

    @Query("SELECT DISTINCT k.courseName FROM KnowledgeBase k")
    List<String> findAllCourseNames();
}
