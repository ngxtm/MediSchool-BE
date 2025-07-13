package com.medischool.backend.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.medischool.backend.model.LoginHistory;
import com.medischool.backend.model.LoginHistory.LoginStatus;

@Repository
public interface LoginHistoryRepository extends JpaRepository<LoginHistory, Long> {

    @Query("SELECT lh FROM LoginHistory lh ORDER BY lh.loginTime DESC")
    Page<LoginHistory> findRecentLoginHistory(Pageable pageable);

    List<LoginHistory> findByUserIdOrderByLoginTimeDesc(UUID userId);

    List<LoginHistory> findByUsernameOrderByLoginTimeDesc(String username);

    List<LoginHistory> findByStatusOrderByLoginTimeDesc(LoginStatus status);

    @Query("SELECT lh FROM LoginHistory lh WHERE lh.loginTime BETWEEN :startDate AND :endDate ORDER BY lh.loginTime DESC")
    List<LoginHistory> findByLoginTimeBetween(@Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT lh FROM LoginHistory lh WHERE LOWER(lh.username) LIKE LOWER(CONCAT('%', :keyword, '%')) OR lh.ipAddress LIKE CONCAT('%', :keyword, '%') OR LOWER(lh.location) LIKE LOWER(CONCAT('%', :keyword, '%')) ORDER BY lh.loginTime DESC")
    List<LoginHistory> searchByKeyword(@Param("keyword") String keyword);

    long countByUserId(UUID userId);

    long countByStatus(LoginStatus status);

    @Query("SELECT COUNT(lh) FROM LoginHistory lh WHERE lh.status = 'SUCCESS' AND CAST(lh.loginTime AS DATE) = CURRENT_DATE")
    long countSuccessfulLoginsToday();

    @Query("SELECT COUNT(lh) FROM LoginHistory lh WHERE lh.status = 'FAILED' AND CAST(lh.loginTime AS DATE) = CURRENT_DATE")
    long countFailedLoginsToday();

    @Query("SELECT lh FROM LoginHistory lh WHERE lh.logoutTime IS NULL AND lh.status = 'SUCCESS' ORDER BY lh.loginTime DESC")
    List<LoginHistory> findActiveSessions();
}