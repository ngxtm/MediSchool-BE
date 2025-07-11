package com.medischool.backend.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.medischool.backend.model.UserProfile;
import com.medischool.backend.repository.UserProfileRepository;
import com.medischool.backend.service.SupabaseAuthService;
import com.medischool.backend.service.UserManagementService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserManagementServiceImpl implements UserManagementService {
    
    private final UserProfileRepository userProfileRepository;
    private final SupabaseAuthService supabaseAuthService;
    
    @Override
    public List<UserProfile> getAllActiveUsers() {
        return userProfileRepository.findAll().stream()
                .filter(user -> user.getIsActive() == null || user.getIsActive())
                .toList();
    }
    
    @Override
    public Page<UserProfile> getActiveUsers(String keyword, Pageable pageable) {
        List<UserProfile> users = getAllActiveUsers();
        
        if (keyword != null && !keyword.trim().isEmpty()) {
            String searchTerm = keyword.toLowerCase().trim();
            users = users.stream()
                    .filter(user -> 
                        (user.getFullName() != null && user.getFullName().toLowerCase().contains(searchTerm)) ||
                        (user.getEmail() != null && user.getEmail().toLowerCase().contains(searchTerm)) ||
                        (user.getPhone() != null && user.getPhone().toLowerCase().contains(searchTerm))
                    )
                    .toList();
        }
        
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), users.size());
        
        List<UserProfile> pageContent = start < users.size() ? users.subList(start, end) : List.of();
        
        return new PageImpl<>(pageContent, pageable, users.size());
    }
    
    @Override
    public List<UserProfile> getAllUsers(boolean includeDeleted) {
        if (includeDeleted) {
            return userProfileRepository.findAll();
        }
        return getAllActiveUsers();
    }
    
    @Override
    public Optional<UserProfile> getActiveUserById(UUID id) {
        return userProfileRepository.findById(id)
                .filter(user -> user.getIsActive() == null || user.getIsActive());
    }
    
    @Override
    @Transactional
    public boolean softDeleteUser(UUID userId, UUID deletedBy, String reason) {
        try {
            Optional<UserProfile> userOpt = userProfileRepository.findById(userId);
            if (userOpt.isEmpty()) {
                log.warn("User not found for soft delete: {}", userId);
                return false;
            }
            
            UserProfile user = userOpt.get();
            
            if (user.getIsActive() != null && !user.getIsActive()) {
                log.warn("User already soft deleted: {}", userId);
                return false;
            }
            
            user.setIsActive(false);
            user.setDeletedAt(LocalDateTime.now());
            user.setDeletedBy(deletedBy);
            user.setDeleteReason(reason);
            user.setUpdatedAt(LocalDateTime.now());
            
            userProfileRepository.save(user);
            
            log.info("User soft deleted successfully: {} by {}", userId, deletedBy);
            return true;
            
        } catch (Exception e) {
            log.error("Error soft deleting user: {}", userId, e);
            return false;
        }
    }
    
    @Override
    @Transactional
    public boolean restoreUser(UUID userId) {
        try {
            Optional<UserProfile> userOpt = userProfileRepository.findById(userId);
            if (userOpt.isEmpty()) {
                log.warn("User not found for restore: {}", userId);
                return false;
            }
            
            UserProfile user = userOpt.get();
            
            if (user.getIsActive() == null || user.getIsActive()) {
                log.warn("User is not soft deleted: {}", userId);
                return false;
            }
            
            user.setIsActive(true);
            user.setDeletedAt(null);
            user.setDeletedBy(null);
            user.setDeleteReason(null);
            user.setUpdatedAt(LocalDateTime.now());
            
            userProfileRepository.save(user);
            
            log.info("User restored successfully: {}", userId);
            return true;
            
        } catch (Exception e) {
            log.error("Error restoring user: {}", userId, e);
            return false;
        }
    }
    
    @Override
    @Transactional
    public boolean hardDeleteUser(UUID userId) {
        try {
            if (!userProfileRepository.existsById(userId)) {
                log.warn("User not found for hard delete: {}", userId);
                return false;
            }
            
            boolean supabaseDeleted = false;
            String supabaseError = null;
            
            try {
                supabaseDeleted = supabaseAuthService.deleteUserFromSupabase(userId);
                log.info("User deleted from Supabase: {} - Success: {}", userId, supabaseDeleted);
            } catch (Exception e) {
                supabaseError = e.getMessage();
                log.error("Failed to delete user from Supabase: {} - Error: {}", userId, supabaseError);

                throw new RuntimeException("Cannot delete user from Supabase: " + supabaseError + 
                    ". User will not be deleted from local database to maintain consistency.");
            }
            
            if (supabaseDeleted) {
                userProfileRepository.deleteById(userId);
                log.info("User hard deleted successfully from both Supabase and local database: {}", userId);
                return true;
            } else {
                log.error("Supabase deletion returned false for user: {}. Local deletion aborted.", userId);
                throw new RuntimeException("Supabase deletion failed. Local deletion aborted to maintain consistency.");
            }
            
        } catch (Exception e) {
            log.error("Error hard deleting user: {} - {}", userId, e.getMessage());
            throw new RuntimeException("Hard delete failed: " + e.getMessage());
        }
    }
    
    @Override
    public boolean checkUserExistsInSupabase(UUID userId) {
        try {
            return supabaseAuthService.checkUserExistsInSupabase(userId);
        } catch (Exception e) {
            log.error("Error checking if user exists in Supabase: {} - {}", userId, e.getMessage());
            throw new RuntimeException("Failed to check user existence in Supabase: " + e.getMessage());
        }
    }
    
    @Override
    public boolean isUserActive(UUID userId) {
        return userProfileRepository.findById(userId)
                .map(user -> user.getIsActive() == null || user.getIsActive())
                .orElse(false);
    }
    
    @Override
    @Transactional
    public UserProfile createUser(UserProfile user) {
        user.setId(UUID.randomUUID());
        user.setIsActive(true);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        
        return userProfileRepository.save(user);
    }
    
    @Override
    @Transactional
    public UserProfile updateUser(UUID id, UserProfile user) {
        Optional<UserProfile> existingUser = getActiveUserById(id);
        if (existingUser.isEmpty()) {
            throw new RuntimeException("User not found or inactive: " + id);
        }
        
        user.setId(id);
        user.setUpdatedAt(LocalDateTime.now());
        
        UserProfile existing = existingUser.get();
        user.setIsActive(existing.getIsActive());
        user.setDeletedAt(existing.getDeletedAt());
        user.setDeletedBy(existing.getDeletedBy());
        user.setDeleteReason(existing.getDeleteReason());
        user.setCreatedAt(existing.getCreatedAt());
        
        return userProfileRepository.save(user);
    }
} 