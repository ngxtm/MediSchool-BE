package com.medischool.backend.controller;

import com.medischool.backend.dto.StudentDetailDTO;
import com.medischool.backend.service.StudentService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/students")
@Tag(name = "Student Controller")
public class StudentController {

    private final StudentService studentService;

    @GetMapping("/{id}")
    public ResponseEntity<StudentDetailDTO> getStudentDetail(@PathVariable Integer id) {
        return ResponseEntity.ok(studentService.getStudentDetail(id));
    }
}
