package com.medischool.backend.controller;

import com.medischool.backend.dto.student.StudentDetailDTO;
import com.medischool.backend.service.StudentService;
import com.medischool.backend.service.VaccineService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/students")
@Tag(name = "Student", description = "Student endpoints")
public class StudentController {

    private final VaccineService service;
    private final StudentService studentService;

    @GetMapping("/{id}")
    @Operation(summary = "Get student information")
    public ResponseEntity<StudentDetailDTO> getStudentDetail(@PathVariable Integer id) {
        return ResponseEntity.ok(studentService.getStudentDetail(id));
    }


}
