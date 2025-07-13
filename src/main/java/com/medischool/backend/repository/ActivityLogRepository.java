package com.medischool.backend.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.medischool.backend.model.ActivityLog;
import com.medischool.backend.model.ActivityLog.ActivityType;
import com.medischool.backend.model.ActivityLog.EntityType;

@Repository
public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {
    
    List<ActivityLog> findTop10ByOrderByCreatedAtDesc();
    
    List<ActivityLog> findByUserIdOrderByCreatedAtDesc(UUID userId);
    
    List<ActivityLog> findByActionTypeOrderByCreatedAtDesc(ActivityType actionType);
    
    List<ActivityLog> findByEntityTypeOrderByCreatedAtDesc(EntityType entityType);
    

    
    long countByUserId(UUID userId);
    
    long countByActionType(ActivityType actionType);
    
    long countByEntityType(EntityType entityType);
    
    Page<ActivityLog> findAllByOrderByCreatedAtDesc(Pageable pageable);
    
    @Query("SELECT a FROM ActivityLog a WHERE LOWER(a.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(a.userName) LIKE LOWER(CONCAT('%', :keyword, '%')) ORDER BY a.createdAt DESC")
    List<ActivityLog> searchByKeyword(@Param("keyword") String keyword);
    
    @Query("SELECT a FROM ActivityLog a ORDER BY a.createdAt DESC")
    List<ActivityLog> findRecentActivities(Pageable pageable);
    

} 