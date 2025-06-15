package com.medischool.backend.controller;


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
}
