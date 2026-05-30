package com.example.coursesystem.repository;

import com.example.coursesystem.entity.ResourceHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ResourceHistoryRepository extends JpaRepository<ResourceHistory, Long> {
    // 按创建时间升序查询所有历史（最早的记录在最前面）
    List<ResourceHistory> findAllByOrderByCreateTimeAsc();
}