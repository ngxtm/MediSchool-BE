package com.medischool.backend.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.medischool.backend.dto.LoginHistoryDTO;
import com.medischool.backend.model.LoginHistory;
import com.medischool.backend.model.LoginHistory.LoginStatus;
import com.medischool.backend.repository.LoginHistoryRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LoginHistoryService {
    
    private final LoginHistoryRepository loginHistoryRepository;
    
    public LoginHistory createLoginRecord(UUID userId, String username, String ipAddress, 
                                        String userAgent, String location, LoginStatus status, String failureReason) {
        
        LocalDateTime now = LocalDateTime.now();
        
        LoginHistory loginHistory = LoginHistory.builder()
                .userId(userId)
                .username(username)
                .loginTime(now)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .location(location)
                .status(status)
                .failureReason(failureReason)
                .build();
        
        return loginHistoryRepository.save(loginHistory);
    }
    
    public LoginHistory updateLogoutTime(Long loginHistoryId) {
        LoginHistory loginHistory = loginHistoryRepository.findById(loginHistoryId)
                .orElseThrow(() -> new RuntimeException("Login history not found"));
        
        LocalDateTime logoutTime = LocalDateTime.now();
        LocalDateTime loginTime = loginHistory.getLoginTime();
        
        long sessionDuration = java.time.Duration.between(loginTime, logoutTime).getSeconds();
        
        loginHistory.setLogoutTime(logoutTime);
        loginHistory.setSessionDuration(sessionDuration);
        
        return loginHistoryRepository.save(loginHistory);
    }
    
    public Page<LoginHistoryDTO> getLoginHistoryWithPagination(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<LoginHistory> loginHistoryPage = loginHistoryRepository.findRecentLoginHistory(pageable);
        
        return loginHistoryPage.map(LoginHistoryDTO::fromLoginHistory);
    }
    
    public List<LoginHistoryDTO> getLoginHistoryByUser(UUID userId, int limit) {
        List<LoginHistory> loginHistoryList = loginHistoryRepository.findByUserIdOrderByLoginTimeDesc(userId);
        
        return loginHistoryList.stream()
                .limit(limit)
                .map(LoginHistoryDTO::fromLoginHistory)
                .toList();
    }
    
    public List<LoginHistoryDTO> getLoginHistoryByUsername(String username, int limit) {
        List<LoginHistory> loginHistoryList = loginHistoryRepository.findByUsernameOrderByLoginTimeDesc(username);
        
        return loginHistoryList.stream()
                .limit(limit)
                .map(LoginHistoryDTO::fromLoginHistory)
                .toList();
    }
    
    public List<LoginHistoryDTO> getLoginHistoryByStatus(LoginStatus status, int limit) {
        List<LoginHistory> loginHistoryList = loginHistoryRepository.findByStatusOrderByLoginTimeDesc(status);
        
        return loginHistoryList.stream()
                .limit(limit)
                .map(LoginHistoryDTO::fromLoginHistory)
                .toList();
    }
    
    public List<LoginHistoryDTO> searchLoginHistory(String keyword, int limit) {
        List<LoginHistory> loginHistoryList = loginHistoryRepository.searchByKeyword(keyword);
        
        return loginHistoryList.stream()
                .limit(limit)
                .map(LoginHistoryDTO::fromLoginHistory)
                .toList();
    }
    
    public List<LoginHistoryDTO> getLoginHistoryByDateRange(LocalDateTime startDate, LocalDateTime endDate, int limit) {
        List<LoginHistory> loginHistoryList = loginHistoryRepository.findByLoginTimeBetween(startDate, endDate);
        
        return loginHistoryList.stream()
                .limit(limit)
                .map(LoginHistoryDTO::fromLoginHistory)
                .toList();
    }
    
    public List<LoginHistoryDTO> getActiveSessions() {
        List<LoginHistory> activeSessions = loginHistoryRepository.findActiveSessions();
        
        return activeSessions.stream()
                .map(LoginHistoryDTO::fromLoginHistory)
                .toList();
    }
    
    public List<LoginHistory> getActiveSessionsRaw() {
        return loginHistoryRepository.findActiveSessions();
    }
    
    public long getTotalLoginCount() {
        return loginHistoryRepository.count();
    }
    
    public long getLoginCountByUser(UUID userId) {
        return loginHistoryRepository.countByUserId(userId);
    }
    
    public long getLoginCountByStatus(LoginStatus status) {
        return loginHistoryRepository.countByStatus(status);
    }
    
    public long getSuccessfulLoginsToday() {
        return loginHistoryRepository.countSuccessfulLoginsToday();
    }
    
    public long getFailedLoginsToday() {
        return loginHistoryRepository.countFailedLoginsToday();
    }
} 