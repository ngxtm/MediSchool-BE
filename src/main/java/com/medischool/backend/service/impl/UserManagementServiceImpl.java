package com.medischool.backend.service.impl;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.medischool.backend.dto.CreateUserRequestDTO;
import com.medischool.backend.model.UserProfile;
import com.medischool.backend.repository.UserProfileRepository;
import com.medischool.backend.service.EmailService;
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
    private final EmailService emailService;
    
    @Override
    public List<UserProfile> getAllActiveUsers() {
        return userProfileRepository.findAll().stream()
                .filter(user -> user.getIsActive() != null && user.getIsActive() && user.getDeletedAt() == null)
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
                .filter(user -> user.getIsActive() != null && user.getIsActive() && user.getDeletedAt() == null);
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
            
            if (user.getDeletedAt() != null) {
                log.warn("User already soft deleted: {} (deleted at: {})", userId, user.getDeletedAt());
                return false;
            }
            
            user.setDeletedAt(LocalDateTime.now());
            user.setDeletedBy(deletedBy);
            user.setDeleteReason(reason);
            user.setIsActive(false);
            user.setUpdatedAt(LocalDateTime.now());
            
            userProfileRepository.save(user);
            
            log.info("User soft deleted successfully: {} by {} with reason: {}", userId, deletedBy, reason);
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
            
            if (user.getDeletedAt() == null) {
                log.warn("User is not soft deleted: {}", userId);
                return false;
            }
            
            user.setDeletedAt(null);
            user.setDeletedBy(null);
            user.setDeleteReason(null);
            user.setIsActive(true);
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
                .map(user -> user.getIsActive() != null && user.getIsActive() && user.getDeletedAt() == null)
                .orElse(false);
    }
    
    @Override
    @Transactional
    public UserProfile createUser(UserProfile user) {
        try {
            UUID userId = UUID.randomUUID();
            user.setId(userId);
            user.setIsActive(true);
            user.setCreatedAt(LocalDateTime.now());
            user.setUpdatedAt(LocalDateTime.now());
            
            Map<String, Object> userMetadata = new HashMap<>();
            userMetadata.put("full_name", user.getFullName());
            userMetadata.put("role", user.getRole());
            userMetadata.put("phone", user.getPhone());
            userMetadata.put("address", user.getAddress());
            userMetadata.put("gender", user.getGender());
            if (user.getDateOfBirth() != null) {
                userMetadata.put("date_of_birth", user.getDateOfBirth().toString());
            }
            
            String password = generateTemporaryPassword();
            log.info("Generated temporary password for user: {}", user.getEmail());
            
            UUID supabaseUserId = supabaseAuthService.createUserInSupabase(user.getEmail(), password, userMetadata);
            
            if (!supabaseUserId.equals(userId)) {
                log.warn("Supabase returned different user ID: expected={}, actual={}", userId, supabaseUserId);
                user.setId(supabaseUserId);
            }
            
            UserProfile savedUser = userProfileRepository.save(user);
            
            try {
                sendTemporaryPasswordEmail(user.getEmail(), user.getFullName(), password);
                log.info("Temporary password email sent successfully to: {}", user.getEmail());
            } catch (Exception e) {
                log.error("Failed to send temporary password email to: {} - {}", user.getEmail(), e.getMessage());
            }
            
            log.info("Successfully created user: {} with ID: {}", user.getEmail(), savedUser.getId());
            return savedUser;
            
        } catch (Exception e) {
            log.error("Error creating user: {} - {}", user.getEmail(), e.getMessage());
            throw new RuntimeException("Failed to create user: " + e.getMessage(), e);
        }
    }
    
    @Override
    @Transactional
    public UserProfile createUserWithPassword(CreateUserRequestDTO createUserRequest) {
        try {
            UUID userId = UUID.randomUUID();
            
            UserProfile user = new UserProfile();
            user.setId(userId);
            user.setFullName(createUserRequest.getFullName());
            user.setPhone(createUserRequest.getPhone());
            user.setEmail(createUserRequest.getEmail());
            user.setAddress(createUserRequest.getAddress());
            user.setDateOfBirth(createUserRequest.getDateOfBirth());
            user.setGender(createUserRequest.getGender());
            user.setRole(createUserRequest.getRole());
            user.setIsActive(createUserRequest.getIsActive() != null ? createUserRequest.getIsActive() : true);
            user.setCreatedAt(LocalDateTime.now());
            user.setUpdatedAt(LocalDateTime.now());
            
            Map<String, Object> userMetadata = new HashMap<>();
            userMetadata.put("full_name", user.getFullName());
            userMetadata.put("role", user.getRole());
            userMetadata.put("phone", user.getPhone());
            userMetadata.put("address", user.getAddress());
            userMetadata.put("gender", user.getGender());
            if (user.getDateOfBirth() != null) {
                userMetadata.put("date_of_birth", user.getDateOfBirth().toString());
            }
            
            String password = createUserRequest.getPassword();
            if (password == null || password.trim().isEmpty()) {
                password = generateTemporaryPassword();
                log.info("Generated temporary password for user: {}", user.getEmail());
            }
            
            UUID supabaseUserId = supabaseAuthService.createUserInSupabase(user.getEmail(), password, userMetadata);
            
            if (!supabaseUserId.equals(userId)) {
                log.warn("Supabase returned different user ID: expected={}, actual={}", userId, supabaseUserId);
                user.setId(supabaseUserId);
            }
            
            UserProfile savedUser = userProfileRepository.save(user);
            
            try {
                if (createUserRequest.getPassword() == null || createUserRequest.getPassword().trim().isEmpty()) {
                    sendTemporaryPasswordEmail(user.getEmail(), user.getFullName(), password);
                    log.info("Temporary password email sent successfully to: {}", user.getEmail());
                } else {
                    sendWelcomeEmail(user.getEmail(), user.getFullName(), password);
                    log.info("Welcome email sent successfully to: {}", user.getEmail());
                }
            } catch (Exception e) {
                log.error("Failed to send email to: {} - {}", user.getEmail(), e.getMessage());
            }
            
            log.info("Successfully created user: {} with ID: {}", user.getEmail(), savedUser.getId());
            return savedUser;
            
        } catch (Exception e) {
            log.error("Error creating user: {} - {}", createUserRequest.getEmail(), e.getMessage());
            throw new RuntimeException("Failed to create user: " + e.getMessage(), e);
        }
    }
    
    private void sendTemporaryPasswordEmail(String email, String fullName, String password) {
        String subject = "Tài khoản MediSchool - Mật khẩu tạm thời";
        String content = String.format("""
            <html>
            <body>
                <h2>Chào mừng bạn đến với hệ thống MediSchool!</h2>
                <p>Xin chào <strong>%s</strong>,</p>
                <p>Tài khoản của bạn đã được tạo thành công trong hệ thống MediSchool.</p>
                <p><strong>Thông tin đăng nhập:</strong></p>
                <ul>
                    <li><strong>Email:</strong> %s</li>
                    <li><strong>Mật khẩu tạm thời:</strong> <span style="background-color: #f0f0f0; padding: 5px; border-radius: 3px; font-family: monospace;">%s</span></li>
                </ul>
                <p><strong>Lưu ý quan trọng:</strong></p>
                <ul>
                    <li>Vui lòng đăng nhập và thay đổi mật khẩu ngay lập tức</li>
                    <li>Mật khẩu này chỉ có hiệu lực tạm thời</li>
                    <li>Để bảo mật, hãy sử dụng mật khẩu mạnh khi thay đổi</li>
                </ul>
                <p>Nếu bạn có bất kỳ câu hỏi nào, vui lòng liên hệ với quản trị viên hệ thống.</p>
                <p>Trân trọng,<br>Đội ngũ MediSchool</p>
            </body>
            </html>
            """, fullName, email, password);
        
        emailService.sendCustomEmail(email, subject, content);
    }
    
    private void sendWelcomeEmail(String email, String fullName, String password) {
        String subject = "Tài khoản MediSchool - Thông tin đăng nhập";
        String content = String.format("""
            <html>
            <body>
                <h2>Chào mừng bạn đến với hệ thống MediSchool!</h2>
                <p>Xin chào <strong>%s</strong>,</p>
                <p>Tài khoản của bạn đã được tạo thành công trong hệ thống MediSchool.</p>
                <p><strong>Thông tin đăng nhập:</strong></p>
                <ul>
                    <li><strong>Email:</strong> %s</li>
                    <li><strong>Mật khẩu:</strong> <span style="background-color: #f0f0f0; padding: 5px; border-radius: 3px; font-family: monospace;">%s</span></li>
                </ul>
                <p><strong>Lưu ý bảo mật:</strong></p>
                <ul>
                    <li>Vui lòng giữ bí mật thông tin đăng nhập</li>
                    <li>Để bảo mật, hãy thay đổi mật khẩu sau khi đăng nhập lần đầu</li>
                    <li>Không chia sẻ thông tin đăng nhập với người khác</li>
                </ul>
                <p>Nếu bạn có bất kỳ câu hỏi nào, vui lòng liên hệ với quản trị viên hệ thống.</p>
                <p>Trân trọng,<br>Đội ngũ MediSchool</p>
            </body>
            </html>
            """, fullName, email, password);
        
        emailService.sendCustomEmail(email, subject, content);
    }
    
    private String generateTemporaryPassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder password = new StringBuilder();
        for (int i = 0; i < 12; i++) {
            password.append(chars.charAt((int) (Math.random() * chars.length())));
        }
        return password.toString();
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