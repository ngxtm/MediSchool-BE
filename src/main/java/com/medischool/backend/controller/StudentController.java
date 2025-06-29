package com.medischool.backend.controller;

 checkup

import com.medischool.backend.dto.response.StudentProfileResponse;
import com.medischool.backend.model.StudentProfile;
import com.medischool.backend.service.StudentProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
public class StudentController {
    private final StudentProfileService  studentProfileService;

    @GetMapping("/find/{studentCode}")
    public ResponseEntity<StudentProfileResponse> test(@PathVariable String studentCode){
        return  ResponseEntity.ok(studentProfileService.findByStudentCode(studentCode));
    }

import com.medischool.backend.dto.student.StudentDetailDTO;
import com.medischool.backend.service.StudentService;
import com.medischool.backend.service.vaccination.VaccineService;
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


 main
}
