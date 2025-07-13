package com.medischool.backend.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.medischool.backend.dto.LoginHistoryDTO;
import com.medischool.backend.model.LoginHistory.LoginStatus;
import com.medischool.backend.service.LoginHistoryService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin/login-history")
@RequiredArgsConstructor
public class LoginHistoryController {
    
    private final LoginHistoryService loginHistoryService;
    
    @GetMapping
    public ResponseEntity<Page<LoginHistoryDTO>> getLoginHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Page<LoginHistoryDTO> loginHistoryPage = loginHistoryService.getLoginHistoryWithPagination(page, size);
        return ResponseEntity.ok(loginHistoryPage);
    }
    
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<LoginHistoryDTO>> getLoginHistoryByUser(
            @PathVariable UUID userId,
            @RequestParam(defaultValue = "10") int limit) {
        
        List<LoginHistoryDTO> loginHistory = loginHistoryService.getLoginHistoryByUser(userId, limit);
        return ResponseEntity.ok(loginHistory);
    }
    
    @GetMapping("/username/{username}")
    public ResponseEntity<List<LoginHistoryDTO>> getLoginHistoryByUsername(
            @PathVariable String username,
            @RequestParam(defaultValue = "10") int limit) {
        
        List<LoginHistoryDTO> loginHistory = loginHistoryService.getLoginHistoryByUsername(username, limit);
        return ResponseEntity.ok(loginHistory);
    }
    
    @GetMapping("/status/{status}")
    public ResponseEntity<List<LoginHistoryDTO>> getLoginHistoryByStatus(
            @PathVariable LoginStatus status,
            @RequestParam(defaultValue = "10") int limit) {
        
        List<LoginHistoryDTO> loginHistory = loginHistoryService.getLoginHistoryByStatus(status, limit);
        return ResponseEntity.ok(loginHistory);
    }
    
    @GetMapping("/search")
    public ResponseEntity<List<LoginHistoryDTO>> searchLoginHistory(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "10") int limit) {
        
        List<LoginHistoryDTO> loginHistory = loginHistoryService.searchLoginHistory(keyword, limit);
        return ResponseEntity.ok(loginHistory);
    }
    
    @GetMapping("/date-range")
    public ResponseEntity<List<LoginHistoryDTO>> getLoginHistoryByDateRange(
            @RequestParam String startDate,
            @RequestParam String endDate,
            @RequestParam(defaultValue = "10") int limit) {
        
        LocalDateTime start = LocalDateTime.parse(startDate);
        LocalDateTime end = LocalDateTime.parse(endDate);
        
        List<LoginHistoryDTO> loginHistory = loginHistoryService.getLoginHistoryByDateRange(start, end, limit);
        return ResponseEntity.ok(loginHistory);
    }
    
    @GetMapping("/active-sessions")
    public ResponseEntity<List<LoginHistoryDTO>> getActiveSessions() {
        List<LoginHistoryDTO> activeSessions = loginHistoryService.getActiveSessions();
        return ResponseEntity.ok(activeSessions);
    }
    
    @GetMapping("/stats/total")
    public ResponseEntity<Long> getTotalLoginCount() {
        long totalCount = loginHistoryService.getTotalLoginCount();
        return ResponseEntity.ok(totalCount);
    }
    
    @GetMapping("/stats/user/{userId}")
    public ResponseEntity<Long> getLoginCountByUser(@PathVariable UUID userId) {
        long count = loginHistoryService.getLoginCountByUser(userId);
        return ResponseEntity.ok(count);
    }
    
    @GetMapping("/stats/status/{status}")
    public ResponseEntity<Long> getLoginCountByStatus(@PathVariable LoginStatus status) {
        long count = loginHistoryService.getLoginCountByStatus(status);
        return ResponseEntity.ok(count);
    }
    
    @GetMapping("/stats/successful-today")
    public ResponseEntity<Long> getSuccessfulLoginsToday() {
        long count = loginHistoryService.getSuccessfulLoginsToday();
        return ResponseEntity.ok(count);
    }
    
    @GetMapping("/stats/failed-today")
    public ResponseEntity<Long> getFailedLoginsToday() {
        return ResponseEntity.ok(loginHistoryService.getFailedLoginsToday());
    }
    

} 