package com.medischool.backend.controller;

import java.util.ArrayList;
import java.util.HashMap;
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

import com.medischool.backend.dto.SoftDeleteRequestDTO;
import com.medischool.backend.dto.UserImportResponseDTO;
import com.medischool.backend.model.UserProfile;
import com.medischool.backend.model.parentstudent.Student;
import com.medischool.backend.repository.StudentRepository;
import com.medischool.backend.repository.UserProfileRepository;
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
    private final ExcelImportService excelImportService;
    private final UserManagementService userManagementService;

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
            List<UserProfile> users;
            
            if (includeDeleted) {
                users = userManagementService.getAllUsers(true);
            } else {
                users = userManagementService.getAllActiveUsers();
            }
            
            if (keyword != null && !keyword.trim().isEmpty()) {
                String searchTerm = keyword.toLowerCase().trim();
                users = users.stream()
                        .filter(user -> 
                            (user.getFullName() != null && user.getFullName().toLowerCase().contains(searchTerm)) ||
                            (user.getEmail() != null && user.getEmail().toLowerCase().contains(searchTerm))
                        )
                        .toList();
            }
            
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/users")
    @Operation(summary = "Create a new user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    public ResponseEntity<UserProfile> createUser(@RequestBody UserProfile user) {
        try {
            UserProfile saved = userManagementService.createUser(user);
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
    public ResponseEntity<UserProfile> updateUser(
            @Parameter(description = "User ID") @PathVariable UUID id, 
            @RequestBody UserProfile user) {
        try {
            UserProfile saved = userManagementService.updateUser(id, user);
            return ResponseEntity.ok(saved);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.badRequest().build();
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
    public ResponseEntity<Map<String, Object>> softDeleteUser(
            @Parameter(description = "User ID") @PathVariable UUID id,
            @RequestBody SoftDeleteRequestDTO request,
            Authentication authentication) {
        try {
            UUID deletedBy = UUID.fromString(authentication.getName());
            
            boolean success = userManagementService.softDeleteUser(id, deletedBy, request.getReason());
            
            Map<String, Object> response = new HashMap<>();
            if (success) {
                response.put("success", true);
                response.put("message", "User soft deleted successfully");
                response.put("userId", id);
                response.put("deletedBy", deletedBy);
                response.put("reason", request.getReason());
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "User not found or already deleted");
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error deleting user: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/users/{id}/restore")
    @Operation(summary = "Restore soft deleted user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User restored successfully"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "400", description = "Bad request")
    })
    public ResponseEntity<Map<String, Object>> restoreUser(
            @Parameter(description = "User ID") @PathVariable UUID id) {
        try {
            boolean success = userManagementService.restoreUser(id);
            
            Map<String, Object> response = new HashMap<>();
            if (success) {
                response.put("success", true);
                response.put("message", "User restored successfully");
                response.put("userId", id);
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "User not found or not deleted");
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error restoring user: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
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
            Optional<UserProfile> userOpt = userProfileRepository.findById(id);
            if (userOpt.isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "User not found in local database");
                response.put("userId", id);
                return ResponseEntity.notFound().build();
            }
            
            UserProfile user = userOpt.get();
            Map<String, Object> response = new HashMap<>();
            response.put("userId", id);
            response.put("localUser", Map.of(
                "email", user.getEmail(),
                "fullName", user.getFullName(),
                "isActive", user.getIsActive(),
                "deletedAt", user.getDeletedAt()
            ));

            try {
                boolean existsInSupabase = userManagementService.checkUserExistsInSupabase(id);
                response.put("supabaseStatus", existsInSupabase ? "EXISTS" : "NOT_FOUND");
                response.put("canHardDelete", existsInSupabase);
                response.put("success", true);
                response.put("message", existsInSupabase ? 
                    "User exists in both local database and Supabase" : 
                    "User exists locally but not in Supabase - safe to hard delete");
                
                return ResponseEntity.ok(response);
                
            } catch (Exception e) {
                response.put("supabaseStatus", "ERROR");
                response.put("supabaseError", e.getMessage());
                response.put("canHardDelete", false);
                response.put("success", false);
                response.put("message", "Error checking Supabase status: " + e.getMessage());
                
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Unexpected error: " + e.getMessage());
            response.put("userId", id);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
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
    public ResponseEntity<Map<String, Object>> hardDeleteUser(
            @Parameter(description = "User ID") @PathVariable UUID id,
            Authentication authentication) {
        try {
            boolean success = userManagementService.hardDeleteUser(id);
            
            if (success) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "User permanently deleted from both Supabase and local database");
                response.put("userId", id);
                return ResponseEntity.ok(response);
            } else {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "User not found");
                return ResponseEntity.notFound().build();
            }
        } catch (RuntimeException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            response.put("userId", id);
            
            // Determine appropriate HTTP status based on error message
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.notFound().build();
            } else if (e.getMessage().contains("Supabase")) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            } else {
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Unexpected error during user deletion: " + e.getMessage());
            response.put("userId", id);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/students")
    @Operation(summary = "Get all students with pagination and search")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Students retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid parameters")
    })
    public ResponseEntity<List<Student>> getStudents(
            @Parameter(description = "Search keyword for name, student code, or class") @RequestParam(required = false) String keyword,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size
    ) {
        try {
            List<Student> students;
            if (keyword != null && !keyword.isEmpty()) {
                students = studentRepository.findAll().stream()
                        .filter(student -> student.getFullName().toLowerCase().contains(keyword.toLowerCase()) ||
                                student.getStudentCode().toLowerCase().contains(keyword.toLowerCase()) ||
                                student.getClassCode().toLowerCase().contains(keyword.toLowerCase()))
                        .toList();
            } else {
                students = studentRepository.findAll();
            }
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
    public ResponseEntity<Student> updateStudent(
            @Parameter(description = "Student ID") @PathVariable Integer id, 
            @RequestBody Student student) {
        try {
            Optional<Student> existing = studentRepository.findById(id);
            if (existing.isEmpty()) return ResponseEntity.notFound().build();
            student.setStudentId(id);
            Student saved = studentRepository.save(student);
            return ResponseEntity.ok(saved);
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
    public ResponseEntity<Void> deleteStudent(@Parameter(description = "Student ID") @PathVariable Integer id) {
        try {
            if (!studentRepository.existsById(id)) return ResponseEntity.notFound().build();
            studentRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }



    @PostMapping("/users/import")
    @Operation(summary = "Import users from Excel file")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Import completed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid file format or data"),
            @ApiResponse(responseCode = "500", description = "Server error during import")
    })
    public ResponseEntity<UserImportResponseDTO> importUsersFromExcel(
            @Parameter(description = "Excel file (.xlsx or .xls)") @RequestParam("file") MultipartFile file) {
        try {
            // Validate file
            if (file == null || file.isEmpty()) {
                UserImportResponseDTO errorResponse = UserImportResponseDTO.builder()
                        .success(false)
                        .totalRows(0)
                        .successCount(0)
                        .errorCount(0)
                        .errors(new ArrayList<>())
                        .message("No file provided or file is empty")
                        .build();
                return ResponseEntity.badRequest().body(errorResponse);
            }

            // Validate file type
            String contentType = file.getContentType();
            String originalFilename = file.getOriginalFilename();
            
            if (contentType == null || 
                (!contentType.equals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet") &&
                 !contentType.equals("application/vnd.ms-excel")) ||
                (originalFilename != null && 
                 !originalFilename.toLowerCase().endsWith(".xlsx") && 
                 !originalFilename.toLowerCase().endsWith(".xls"))) {
                
                UserImportResponseDTO errorResponse = UserImportResponseDTO.builder()
                        .success(false)
                        .totalRows(0)
                        .successCount(0)
                        .errorCount(0)
                        .errors(new ArrayList<>())
                        .message("Invalid file format. Please upload an Excel file (.xlsx or .xls)")
                        .build();
                return ResponseEntity.badRequest().body(errorResponse);
            }

            UserImportResponseDTO response = excelImportService.importUsersFromExcel(file);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Unexpected error during user import: {}", e.getMessage(), e);
            
            UserImportResponseDTO errorResponse = UserImportResponseDTO.builder()
                    .success(false)
                    .totalRows(0)
                    .successCount(0)
                    .errorCount(0)
                    .errors(new ArrayList<>())
                    .message("Server error during import: " + e.getMessage())
                    .build();
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    @GetMapping("/users/import/template")
    @Operation(summary = "Download Excel template for user import")
    @ApiResponse(responseCode = "200", description = "Excel template downloaded successfully")
    public ResponseEntity<byte[]> downloadUserImportTemplate() {
        try {
            byte[] excelBytes = excelImportService.generateUserImportTemplate();
            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=user_import_template.xlsx")
                    .header("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                    .body(excelBytes);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/users/import/test")
    @Operation(summary = "Test import endpoint")
    public ResponseEntity<String> testImport() {
        try {
            log.info("Test import endpoint called successfully");
            return ResponseEntity.ok("Import endpoint is working");
        } catch (Exception e) {
            log.error("Test import failed: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/users/export")
    @Operation(summary = "Download Excel file of all users in database")
    @ApiResponse(responseCode = "200", description = "Excel file of all users downloaded successfully")
    public ResponseEntity<byte[]> downloadUserListExcel() {
        try {
            List<UserProfile> users = userProfileRepository.findAll();
            byte[] excelBytes = excelImportService.exportUsersToExcel(users);
            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=user_list.xlsx")
                    .header("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                    .body(excelBytes);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}