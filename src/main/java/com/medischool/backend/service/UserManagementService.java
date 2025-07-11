package com.medischool.backend.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.medischool.backend.model.UserProfile;

public interface UserManagementService {
    
    List<UserProfile> getAllActiveUsers();
    
    Page<UserProfile> getActiveUsers(String keyword, Pageable pageable);
    
    List<UserProfile> getAllUsers(boolean includeDeleted);
    
    Optional<UserProfile> getActiveUserById(UUID id);
    
    boolean softDeleteUser(UUID userId, UUID deletedBy, String reason);
    
    boolean restoreUser(UUID userId);
    
    boolean hardDeleteUser(UUID userId);
    
    boolean checkUserExistsInSupabase(UUID userId);
    
    boolean isUserActive(UUID userId);
    
    UserProfile createUser(UserProfile user);
    
    UserProfile updateUser(UUID id, UserProfile user);
} 