//package com.medischool.backend.controller;
//
//import com.medischool.backend.model.UserProfile;
//import com.medischool.backend.model.parentstudent.Student;
//import com.medischool.backend.repository.UserProfileRepository;
//import com.medischool.backend.repository.StudentRepository;
//import io.swagger.v3.oas.annotations.tags.Tag;
//import lombok.RequiredArgsConstructor;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.data.domain.Pageable;
//import org.springframework.data.domain.Sort;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.access.prepost.PreAuthorize;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//import java.util.Optional;
//import java.util.UUID;
//
//@RestController
//@RequestMapping("/api/admin")
//@RequiredArgsConstructor
//@Tag(name = "Admin", description = "Admin management endpoints")
//public class AdminController {
//    private final UserProfileRepository userProfileRepository;
//    private final StudentRepository studentRepository;
//
//    // -------- USER --------
//    @GetMapping("/users")
//    @PreAuthorize("hasRole('ADMIN')")
//    public ResponseEntity<List<UserProfile>> getUsers(
//            @RequestParam(required = false) String keyword,
//            @RequestParam(defaultValue = "0") int page,
//            @RequestParam(defaultValue = "20") int size
//    ) {
//        Pageable pageable = PageRequest.of(page, size, Sort.by("fullName").ascending());
//        Page<UserProfile> result;
//        if (keyword != null && !keyword.isEmpty()) {
//            result = userProfileRepository.findAll((root, query, cb) ->
//                    cb.or(
//                            cb.like(cb.lower(root.get("fullName")), "%" + keyword.toLowerCase() + "%"),
//                            cb.like(cb.lower(root.get("email")), "%" + keyword.toLowerCase() + "%")
//                    ), pageable);
//        } else {
//            result = userProfileRepository.findAll(pageable);
//        }
//        return ResponseEntity.ok(result.getContent());
//    }
//
//    @PostMapping("/users")
//    @PreAuthorize("hasRole('ADMIN')")
//    public ResponseEntity<UserProfile> createUser(@RequestBody UserProfile user) {
//        user.setId(UUID.randomUUID());
//        UserProfile saved = userProfileRepository.save(user);
//        return ResponseEntity.ok(saved);
//    }
//
//    @PutMapping("/users/{id}")
//    @PreAuthorize("hasRole('ADMIN')")
//    public ResponseEntity<UserProfile> updateUser(@PathVariable UUID id, @RequestBody UserProfile user) {
//        Optional<UserProfile> existing = userProfileRepository.findById(id);
//        if (existing.isEmpty()) return ResponseEntity.notFound().build();
//        user.setId(id);
//        UserProfile saved = userProfileRepository.save(user);
//        return ResponseEntity.ok(saved);
//    }
//
//    @DeleteMapping("/users/{id}")
//    @PreAuthorize("hasRole('ADMIN')")
//    public ResponseEntity<Void> deleteUser(@PathVariable UUID id) {
//        if (!userProfileRepository.existsById(id)) return ResponseEntity.notFound().build();
//        userProfileRepository.deleteById(id);
//        return ResponseEntity.noContent().build();
//    }
//
//    // -------- STUDENT --------
//    @GetMapping("/students")
//    @PreAuthorize("hasRole('ADMIN')")
//    public ResponseEntity<List<Student>> getStudents(
//            @RequestParam(required = false) String keyword,
//            @RequestParam(defaultValue = "0") int page,
//            @RequestParam(defaultValue = "20") int size
//    ) {
//        Pageable pageable = PageRequest.of(page, size, Sort.by("fullName").ascending());
//        Page<Student> result;
//        if (keyword != null && !keyword.isEmpty()) {
//            result = studentRepository.findAll((root, query, cb) ->
//                    cb.or(
//                            cb.like(cb.lower(root.get("fullName")), "%" + keyword.toLowerCase() + "%"),
//                            cb.like(cb.lower(root.get("studentCode")), "%" + keyword.toLowerCase() + "%"),
//                            cb.like(cb.lower(root.get("classCode")), "%" + keyword.toLowerCase() + "%")
//                    ), pageable);
//        } else {
//            result = studentRepository.findAll(pageable);
//        }
//        return ResponseEntity.ok(result.getContent());
//    }
//
//    @PostMapping("/students")
//    @PreAuthorize("hasRole('ADMIN')")
//    public ResponseEntity<Student> createStudent(@RequestBody Student student) {
//        Student saved = studentRepository.save(student);
//        return ResponseEntity.ok(saved);
//    }
//
//    @PutMapping("/students/{id}")
//    @PreAuthorize("hasRole('ADMIN')")
//    public ResponseEntity<Student> updateStudent(@PathVariable Integer id, @RequestBody Student student) {
//        Optional<Student> existing = studentRepository.findById(id);
//        if (existing.isEmpty()) return ResponseEntity.notFound().build();
//        student.setStudentId(id);
//        Student saved = studentRepository.save(student);
//        return ResponseEntity.ok(saved);
//    }
//
//    @DeleteMapping("/students/{id}")
//    @PreAuthorize("hasRole('ADMIN')")
//    public ResponseEntity<Void> deleteStudent(@PathVariable Integer id) {
//        if (!studentRepository.existsById(id)) return ResponseEntity.notFound().build();
//        studentRepository.deleteById(id);
//        return ResponseEntity.noContent().build();
//    }
//}