package com.medischool.backend.controller;

import com.medischool.backend.model.UserProfile;
import com.medischool.backend.model.parentstudent.Student;
import com.medischool.backend.repository.UserProfileRepository;
import com.medischool.backend.repository.StudentRepository;
import com.medischool.backend.dto.student.StudentImportResponseDTO;
import com.medischool.backend.service.ExcelImportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Tag(name = "Admin", description = "Admin management endpoints")
public class AdminController {
    private final UserProfileRepository userProfileRepository;
    private final StudentRepository studentRepository;
    private final ExcelImportService excelImportService;

    // -------- USER MANAGEMENT --------
    @GetMapping("/users")
    @Operation(summary = "Get all users with pagination and search")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Users retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid parameters")
    })
    public ResponseEntity<List<UserProfile>> getUsers(
            @Parameter(description = "Search keyword for name or email") @RequestParam(required = false) String keyword,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size
    ) {
        try {
            List<UserProfile> users;
            if (keyword != null && !keyword.isEmpty()) {
                users = userProfileRepository.findAll().stream()
                        .filter(user -> user.getFullName().toLowerCase().contains(keyword.toLowerCase()) ||
                                user.getEmail().toLowerCase().contains(keyword.toLowerCase()))
                        .toList();
            } else {
                users = userProfileRepository.findAll();
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
            user.setId(UUID.randomUUID());
            UserProfile saved = userProfileRepository.save(user);
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
            Optional<UserProfile> existing = userProfileRepository.findById(id);
            if (existing.isEmpty()) return ResponseEntity.notFound().build();
            user.setId(id);
            UserProfile saved = userProfileRepository.save(user);
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/users/{id}")
    @Operation(summary = "Delete user by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "User deleted successfully"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<Void> deleteUser(@Parameter(description = "User ID") @PathVariable UUID id) {
        try {
            if (!userProfileRepository.existsById(id)) return ResponseEntity.notFound().build();
            userProfileRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // -------- STUDENT MANAGEMENT --------
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

    // -------- EXCEL IMPORT --------
    @PostMapping("/students/import")
    @Operation(summary = "Import students from Excel file")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Import completed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid file format or data"),
            @ApiResponse(responseCode = "500", description = "Server error during import")
    })
    public ResponseEntity<StudentImportResponseDTO> importStudentsFromExcel(
            @Parameter(description = "Excel file (.xlsx or .xls)") @RequestParam("file") MultipartFile file) {
        try {
            StudentImportResponseDTO response = excelImportService.importStudentsFromExcel(file);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/students/import/template")
    @Operation(summary = "Download Excel file of all students in database")
    @ApiResponse(responseCode = "200", description = "Excel file of all students downloaded successfully")
    public ResponseEntity<byte[]> downloadStudentListExcel() {
        try {
            List<Student> students = studentRepository.findAll();
            byte[] excelBytes = excelImportService.generateStudentListExcel(students);
            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=student_list.xlsx")
                    .header("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                    .body(excelBytes);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}