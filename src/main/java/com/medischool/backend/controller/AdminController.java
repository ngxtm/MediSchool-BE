package com.medischool.backend.controller;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.medischool.backend.annotation.LogActivity;
import com.medischool.backend.dto.ActivityLogDTO;
import com.medischool.backend.dto.CreateUserRequestDTO;
import com.medischool.backend.dto.SoftDeleteRequestDTO;
import com.medischool.backend.dto.UserImportResponseDTO;
import com.medischool.backend.model.ActivityLog.ActivityType;
import com.medischool.backend.model.ActivityLog.EntityType;
import com.medischool.backend.model.UserProfile;
import com.medischool.backend.model.parentstudent.Student;
import com.medischool.backend.repository.ParentStudentLinkRepository;
import com.medischool.backend.repository.StudentRepository;
import com.medischool.backend.repository.UserProfileRepository;
import com.medischool.backend.service.ActivityLogService;
import com.medischool.backend.service.AsyncEmailService;
import com.medischool.backend.service.EmailService;
import com.medischool.backend.service.ExcelImportService;
import com.medischool.backend.service.UserManagementService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Admin", description = "Admin management endpoints")
public class AdminController {
    private final UserProfileRepository userProfileRepository;
    private final StudentRepository studentRepository;
    private final ParentStudentLinkRepository parentStudentLinkRepository;
    private final ExcelImportService excelImportService;
    private final UserManagementService userManagementService;
    private final AsyncEmailService asyncEmailService;
    private final EmailService emailService;
    private final ActivityLogService activityLogService;

    @GetMapping("/users/count")
    @Operation(summary = "Get total count of users")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User count retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid parameters")
    })
    public ResponseEntity<Long> getUserCount(
            @Parameter(description = "Include deleted users") @RequestParam(defaultValue = "false") boolean includeDeleted
    ) {
        try {
            long count;
            if (includeDeleted) {
                count = userManagementService.getAllUsers(true).size();
            } else {
                count = userManagementService.getAllActiveUsers().size();
            }
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/students/count")
    @Operation(summary = "Get total count of students")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Student count retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid parameters")
    })
    public ResponseEntity<Long> getStudentCount() {
        try {
            long count = studentRepository.count();
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/users")
    @Operation(summary = "Get all active users with search")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Users retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid parameters")
    })
    public ResponseEntity<List<UserProfile>> getUsers(
            @Parameter(description = "Search keyword for name or email") @RequestParam(required = false) String keyword,
            @Parameter(description = "Include deleted users") @RequestParam(defaultValue = "false") boolean includeDeleted
    ) {
        try {
            log.info("Get users request - keyword: {}, includeDeleted: {}", keyword, includeDeleted);
            
            List<UserProfile> users;
            
            if (includeDeleted) {
                users = userManagementService.getAllUsers(true);
                log.info("Retrieved {} users (including deleted)", users.size());
            } else {
                users = userManagementService.getAllActiveUsers();
                log.info("Retrieved {} active users", users.size());
            }
            
            if (keyword != null && !keyword.trim().isEmpty()) {
                String searchTerm = keyword.toLowerCase().trim();
                int beforeFilter = users.size();
                users = users.stream()
                        .filter(user -> 
                            (user.getFullName() != null && user.getFullName().toLowerCase().contains(searchTerm)) ||
                            (user.getEmail() != null && user.getEmail().toLowerCase().contains(searchTerm))
                        )
                        .toList();
                log.info("Filtered users from {} to {} with keyword: {}", beforeFilter, users.size(), keyword);
            }
            
            log.info("Returning {} users", users.size());
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            log.error("Error getting users: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/users")
    @Operation(summary = "Create a new user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    @LogActivity(
        actionType = ActivityType.CREATE,
        entityType = EntityType.USER,
        description = "Tạo người dùng mới: {fullName} ({email})"
    )
    public ResponseEntity<UserProfile> createUser(@RequestBody UserProfile user) {
        try {
            UserProfile saved = userManagementService.createUser(user);
            return new ResponseEntity<>(saved, HttpStatus.CREATED);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/users/with-password")
    @Operation(summary = "Create a new user with password")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    @LogActivity(
        actionType = ActivityType.CREATE,
        entityType = EntityType.USER,
        description = "Tạo người dùng mới với mật khẩu: {fullName} ({email})"
    )
    public ResponseEntity<UserProfile> createUserWithPassword(@RequestBody CreateUserRequestDTO createUserRequest) {
        try {
            UserProfile saved = userManagementService.createUserWithPassword(createUserRequest);
            return new ResponseEntity<>(saved, HttpStatus.CREATED);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/users/{id}")
    @Operation(summary = "Update user by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User updated successfully"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @LogActivity(
        actionType = ActivityType.UPDATE,
        entityType = EntityType.USER,
        description = "Cập nhật thông tin người dùng",
        entityIdParam = "id"
    )
    public ResponseEntity<UserProfile> updateUser(
            @Parameter(description = "User ID") @PathVariable UUID id, 
            @RequestBody UserProfile user) {
        try {
            UserProfile updated = userManagementService.updateUser(id, user);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/users/{id}")
    @Operation(summary = "Soft delete user by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User soft deleted successfully"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "400", description = "Bad request")
    })
    @LogActivity(
        actionType = ActivityType.DELETE,
        entityType = EntityType.USER,
        description = "Xóa mềm người dùng",
        entityIdParam = "id"
    )
    public ResponseEntity<Map<String, Object>> softDeleteUser(
            @Parameter(description = "User ID") @PathVariable UUID id,
            @RequestBody SoftDeleteRequestDTO request,
            Authentication authentication) {
        try {
            String currentUserEmail = authentication.getName();
            log.info("Soft delete user request - User ID: {}, Requested by: {}", id, currentUserEmail);
            
            boolean result = userManagementService.softDeleteUser(id, id, request.getReason());
            
            if (result) {
                log.info("User {} soft deleted successfully by {}", id, currentUserEmail);
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "User soft deleted successfully"
                ));
            } else {
                log.warn("Failed to soft delete user {} by {}: {}", id, currentUserEmail, "User not found or already deleted");
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "User not found or already deleted"
                ));
            }
        } catch (Exception e) {
            log.error("Error soft deleting user {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "Internal server error: " + e.getMessage()
            ));
        }
    }

    @PostMapping("/users/{id}/restore")
    @Operation(summary = "Restore soft deleted user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User restored successfully"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "400", description = "Bad request")
    })
    @LogActivity(
        actionType = ActivityType.UPDATE,
        entityType = EntityType.USER,
        description = "Khôi phục người dùng đã xóa",
        entityIdParam = "id"
    )
    public ResponseEntity<Map<String, Object>> restoreUser(
            @Parameter(description = "User ID") @PathVariable UUID id) {
        try {
            log.info("Restore user request - User ID: {}", id);
            
            boolean result = userManagementService.restoreUser(id);
            
            if (result) {
                log.info("User {} restored successfully", id);
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "User restored successfully"
                ));
            } else {
                log.warn("Failed to restore user {}: {}", id, "User not found or already active");
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "User not found or already active"
                ));
            }
        } catch (Exception e) {
            log.error("Error restoring user {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "Internal server error: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/users/{id}/supabase-status")
    @Operation(summary = "Check if user exists in Supabase auth.users table")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Status checked successfully"),
            @ApiResponse(responseCode = "404", description = "User not found in local database"),
            @ApiResponse(responseCode = "500", description = "Error checking Supabase status")
    })
    public ResponseEntity<Map<String, Object>> checkUserSupabaseStatus(
            @Parameter(description = "User ID") @PathVariable UUID id) {
        try {
            log.info("Check Supabase status request - User ID: {}", id);
            
            Optional<UserProfile> userOpt = userProfileRepository.findById(id);
            if (userOpt.isEmpty()) {
                log.warn("User {} not found in local database", id);
                return ResponseEntity.status(404).body(Map.of(
                    "success", false,
                    "message", "User not found in local database"
                ));
            }
            
            UserProfile user = userOpt.get();
            boolean existsInSupabase = userManagementService.checkUserExistsInSupabase(user.getId());
            
            Map<String, Object> response = Map.of(
                "success", true,
                "userEmail", user.getEmail(),
                "existsInSupabase", existsInSupabase,
                "localUserActive", user.getIsActive()
            );
            
            log.info("Supabase status check completed for user {}: existsInSupabase={}", id, existsInSupabase);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error checking Supabase status for user {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "Error checking Supabase status: " + e.getMessage()
            ));
        }
    }

    @DeleteMapping("/users/{id}/hard")
    @Operation(summary = "Hard delete user permanently (DANGEROUS)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "User permanently deleted"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Supabase deletion failed")
    })
    @LogActivity(
        actionType = ActivityType.DELETE,
        entityType = EntityType.USER,
        description = "Xóa vĩnh viễn người dùng",
        entityIdParam = "id"
    )
    public ResponseEntity<Map<String, Object>> hardDeleteUser(
            @Parameter(description = "User ID") @PathVariable UUID id,
            Authentication authentication) {
        try {
            String currentUserEmail = authentication.getName();
            log.info("Hard delete user request - User ID: {}, Requested by: {}", id, currentUserEmail);
            
            boolean result = userManagementService.hardDeleteUser(id);
            
            if (result) {
                log.info("User {} hard deleted successfully by {}", id, currentUserEmail);
                return ResponseEntity.status(204).build();
            } else {
                log.warn("Failed to hard delete user {} by {}: {}", id, currentUserEmail, "User not found or deletion failed");
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "User not found or deletion failed"
                ));
            }
        } catch (Exception e) {
            log.error("Error hard deleting user {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "Internal server error: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/students")
    @Operation(summary = "Get all students with pagination and search")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Students retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid parameters")
    })
    public ResponseEntity<List<Student>> getStudents(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size
    ) {
        try {
            List<Student> students = studentRepository.findAll();
            return ResponseEntity.ok(students);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/students")
    @Operation(summary = "Create a new student")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Student created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    @LogActivity(
        actionType = ActivityType.CREATE,
        entityType = EntityType.STUDENT,
        description = "Tạo học sinh mới"
    )
    public ResponseEntity<Student> createStudent(@RequestBody Student student) {
        try {
            Student saved = studentRepository.save(student);
            return new ResponseEntity<>(saved, HttpStatus.CREATED);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/students/{id}")
    @Operation(summary = "Update student by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Student updated successfully"),
            @ApiResponse(responseCode = "404", description = "Student not found")
    })
    @LogActivity(
        actionType = ActivityType.UPDATE,
        entityType = EntityType.STUDENT,
        description = "Cập nhật thông tin học sinh",
        entityIdParam = "id"
    )
    public ResponseEntity<Student> updateStudent(
            @Parameter(description = "Student ID") @PathVariable Integer id, 
            @RequestBody Student student) {
        try {
            if (studentRepository.existsById(id)) {
                Student updated = studentRepository.save(student);
                return ResponseEntity.ok(updated);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/students/{id}")
    @Operation(summary = "Delete student by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Student deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Student not found")
    })
    @LogActivity(
        actionType = ActivityType.DELETE,
        entityType = EntityType.STUDENT,
        description = "Xóa học sinh",
        entityIdParam = "id"
    )
    public ResponseEntity<Void> deleteStudent(@Parameter(description = "Student ID") @PathVariable Integer id) {
        try {
            if (studentRepository.existsById(id)) {
                studentRepository.deleteById(id);
                return ResponseEntity.noContent().build();
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/students/import")
    @Operation(summary = "Import students from Excel file")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Import completed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid file format or data"),
            @ApiResponse(responseCode = "500", description = "Server error during import")
    })
    @LogActivity(
        actionType = ActivityType.IMPORT,
        entityType = EntityType.STUDENT,
        description = "Nhập thành công {successCount} học sinh từ file {file}"
    )
    public ResponseEntity<UserImportResponseDTO> importStudentsFromExcel(
            @Parameter(description = "Excel file (.xlsx or .xls)") @RequestParam("file") MultipartFile file) {
        try {
            log.info("Import students request - File: {}, Size: {} bytes", file.getOriginalFilename(), file.getSize());
            
            UserImportResponseDTO response = excelImportService.importStudentsFromExcel(file);
            
            log.info("Import students completed - Success: {}", response.getSuccessCount());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error importing students: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(UserImportResponseDTO.builder()
                    .success(false)
                    .message("Error importing students: " + e.getMessage())
                    .build());
        }
    }

    @PostMapping("/users/import")
    @Operation(summary = "Import users from Excel file")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Import completed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid file format or data"),
            @ApiResponse(responseCode = "500", description = "Server error during import")
    })
    @LogActivity(
        actionType = ActivityType.IMPORT,
        entityType = EntityType.USER,
        description = "Nhập danh sách người dùng từ file Excel"
    )
    public ResponseEntity<UserImportResponseDTO> importUsersFromExcel(
            @Parameter(description = "Excel file (.xlsx or .xls)") @RequestParam("file") MultipartFile file) {
        try {
            log.info("Import users request - File: {}, Size: {} bytes", file.getOriginalFilename(), file.getSize());
            
            UserImportResponseDTO response = excelImportService.importUsersFromExcel(file);
            
            log.info("Import users completed - Success: {}", response.getSuccessCount());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error importing users: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(UserImportResponseDTO.builder()
                    .success(false)
                    .message("Error importing users: " + e.getMessage())
                    .build());
        }
    }

    @GetMapping("/users/import/template")
    @Operation(summary = "Download Excel template for user import")
    @ApiResponse(responseCode = "200", description = "Excel template downloaded successfully")
    @LogActivity(
        actionType = ActivityType.DOWNLOAD,
        entityType = EntityType.USER,
        description = "Tải xuống template Excel cho import người dùng"
    )
    public ResponseEntity<byte[]> downloadUserImportTemplate() {
        try {
            byte[] templateBytes = excelImportService.generateUserImportTemplate();
            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=user_import_template.xlsx")
                    .header("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                    .body(templateBytes);
        } catch (Exception e) {
            log.error("Error downloading user import template: {}", e.getMessage(), e);
            return ResponseEntity.status(500).build();
        }
    }



    @GetMapping("/users/export")
    @Operation(summary = "Download Excel file of all users in database")
    @ApiResponse(responseCode = "200", description = "Excel file of all users downloaded successfully")
    @LogActivity(
        actionType = ActivityType.EXPORT,
        entityType = EntityType.USER,
        description = "Xuất danh sách người dùng ra file Excel"
    )
    public ResponseEntity<byte[]> downloadUserListExcel() {
        try {
            List<UserProfile> allUsers = userProfileRepository.findAll();
            byte[] excelBytes = excelImportService.exportUsersToExcel(allUsers);
            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=users_list.xlsx")
                    .header("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                    .body(excelBytes);
        } catch (Exception e) {
            log.error("Error downloading user list Excel: {}", e.getMessage(), e);
            return ResponseEntity.status(500).build();
        }
    }

    @PostMapping("/emails/send")
    @Operation(summary = "Send bulk emails to selected users")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Emails sent successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "500", description = "Server error during email sending")
    })
    @LogActivity(
        actionType = ActivityType.SEND_EMAIL,
        entityType = EntityType.EMAIL,
        description = "Gửi email thông báo cho {recipientCount} người nhận"
    )
    public ResponseEntity<Map<String, Object>> sendBulkEmails(
            @RequestBody Map<String, Object> request) {
        try {
            @SuppressWarnings("unchecked")
            List<String> recipientEmails = (List<String>) request.get("recipientEmails");
            String subject = (String) request.get("subject");
            String content = (String) request.get("content");
            
            if (recipientEmails == null || recipientEmails.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "No recipients specified"
                ));
            }
            
            if (subject == null || subject.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Subject is required"
                ));
            }
            
            if (content == null || content.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Content is required"
                ));
            }
            
            log.info("Send bulk emails request - Recipients: {}, Subject: {}", recipientEmails.size(), subject);
            
            log.info("Bulk email sending requested for {} recipients", recipientEmails.size());
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Emails queued for sending",
                "recipientCount", recipientEmails.size()
            ));
        } catch (Exception e) {
            log.error("Error sending bulk emails: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "Error sending emails: " + e.getMessage()
            ));
        }
    }

    @PostMapping("/emails/send-single")
    @Operation(summary = "Send single email to a user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Email sent successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "500", description = "Server error during email sending")
    })
    @LogActivity(
        actionType = ActivityType.SEND_EMAIL,
        entityType = EntityType.EMAIL,
        description = "Gửi email đơn lẻ"
    )
    public ResponseEntity<Map<String, Object>> sendSingleEmail(
            @RequestBody Map<String, Object> request) {
        try {
            String recipientEmail = (String) request.get("recipientEmail");
            String subject = (String) request.get("subject");
            String content = (String) request.get("content");
            
            if (recipientEmail == null || recipientEmail.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Recipient email is required"
                ));
            }
            
            if (subject == null || subject.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Subject is required"
                ));
            }
            
            if (content == null || content.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Content is required"
                ));
            }
            
            UserProfile user = userProfileRepository.findByEmail(recipientEmail).orElse(null);
            if (user == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "User not found"
                ));
            }

            if (!user.getIsActive()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "User is not active"
                ));
            }

            emailService.sendCustomEmail(user.getEmail(), subject, content);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Email sent successfully",
                "recipient", user.getEmail()
            ));

        } catch (Exception e) {
            log.error("Error sending single email: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "Error sending email: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/activities/recent")
    @Operation(summary = "Get recent activities for admin dashboard")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Recent activities retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid parameters"),
            @ApiResponse(responseCode = "500", description = "Server error")
    })
    @LogActivity(
        actionType = ActivityType.VIEW,
        entityType = EntityType.SYSTEM,
        description = "Xem hoạt động gần đây"
    )
    public ResponseEntity<Map<String, Object>> getRecentActivities(
            @Parameter(description = "Number of activities to retrieve") @RequestParam(defaultValue = "10") int limit,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page) {
        try {
            log.info("Getting recent activities - limit: {}, page: {}", limit, page);
            
            List<ActivityLogDTO> activities = activityLogService.getRecentActivitiesWithPagination(page, limit);
            long totalActivities = activityLogService.getTotalActivityCount();
            int totalPages = (int) Math.ceil((double) totalActivities / limit);
            
            log.info("Returning {} activities, total: {}, totalPages: {}", activities.size(), totalActivities, totalPages);
            
            Map<String, Object> response = Map.of(
                "activities", activities,
                "currentPage", page,
                "totalPages", totalPages,
                "totalActivities", totalActivities,
                "pageSize", limit
            );
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error getting recent activities: {}", e.getMessage(), e);
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/activities/user/{userId}")
    @Operation(summary = "Get activities by user ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User activities retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid parameters"),
            @ApiResponse(responseCode = "500", description = "Server error")
    })
    @LogActivity(
        actionType = ActivityType.VIEW,
        entityType = EntityType.SYSTEM,
        description = "Xem hoạt động theo người dùng",
        entityIdParam = "userId"
    )
    public ResponseEntity<List<ActivityLogDTO>> getUserActivities(
            @Parameter(description = "User ID") @PathVariable UUID userId,
            @Parameter(description = "Number of activities to retrieve") @RequestParam(defaultValue = "20") int limit) {
        try {
            List<ActivityLogDTO> activities = activityLogService.getActivitiesByUser(userId, limit);
            return ResponseEntity.ok(activities);
        } catch (Exception e) {
            log.error("Error getting user activities: {}", e.getMessage(), e);
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/activities/search")
    @Operation(summary = "Search activities by keyword")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Activities found successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid parameters"),
            @ApiResponse(responseCode = "500", description = "Server error")
    })
    @LogActivity(
        actionType = ActivityType.VIEW,
        entityType = EntityType.SYSTEM,
        description = "Tìm kiếm hoạt động"
    )
    public ResponseEntity<List<ActivityLogDTO>> searchActivities(
            @Parameter(description = "Search keyword") @RequestParam String keyword,
            @Parameter(description = "Number of activities to retrieve") @RequestParam(defaultValue = "20") int limit) {
        try {
            List<ActivityLogDTO> activities = activityLogService.searchActivities(keyword, limit);
            return ResponseEntity.ok(activities);
        } catch (Exception e) {
            log.error("Error searching activities: {}", e.getMessage(), e);
            return ResponseEntity.status(500).build();
        }
    }




}