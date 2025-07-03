package com.medischool.backend.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.medischool.backend.dto.ParentResponseDTO;
import com.medischool.backend.dto.student.StudentDetailDTO;
import com.medischool.backend.model.UserProfile;
import com.medischool.backend.model.parentstudent.Parent;
import com.medischool.backend.model.parentstudent.ParentStudentLink;
import com.medischool.backend.model.parentstudent.Student;
import com.medischool.backend.repository.ParentRepository;
import com.medischool.backend.repository.ParentStudentLinkRepository;
import com.medischool.backend.repository.UserProfileRepository;
import com.medischool.backend.service.StudentService;
import com.medischool.backend.service.vaccination.VaccineService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/students")
@Tag(name = "Student", description = "Student endpoints")
public class StudentController {

    private final VaccineService service;
    private final StudentService studentService;
    
    @Autowired
    private ParentStudentLinkRepository parentStudentLinkRepository;
    
    @Autowired
    private UserProfileRepository userProfileRepository;
    
    @Autowired
    private ParentRepository parentRepository;

    @GetMapping("/{id}")
    @Operation(summary = "Get student information")
    public ResponseEntity<StudentDetailDTO> getStudentDetail(@PathVariable Integer id) {
        return ResponseEntity.ok(studentService.getStudentDetail(id));
    }

    @GetMapping
    @Operation(summary = "Get student list")
    public ResponseEntity<List<Student>> getAllStudents() {
        try {
            return ResponseEntity.ok(studentService.getAllStudents());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/{studentId}/parents")
    @Operation(summary = "Get parent information by student ID")
    public ResponseEntity<?> getParentsByStudentId(@PathVariable Integer studentId) {
        try {
            List<ParentStudentLink> links = parentStudentLinkRepository.findByStudentId(studentId);
            
            if (links.isEmpty()) {
                return ResponseEntity.status(404).body("No parents found for student ID: " + studentId);
            }
            
            List<ParentResponseDTO> parentResponses = new ArrayList<>();
            
            for (ParentStudentLink link : links) {
                Optional<UserProfile> userProfileOpt = userProfileRepository.findById(link.getParentId());
                
                Optional<Parent> parentOpt = parentRepository.findByParentId(link.getParentId());
                
                if (userProfileOpt.isPresent()) {
                    UserProfile userProfile = userProfileOpt.get();
                    Parent parent = parentOpt.orElse(null);
                    
                    ParentResponseDTO parentResponse = new ParentResponseDTO();
                    parentResponse.setParentId(link.getParentId());
                    parentResponse.setFullName(userProfile.getFullName());
                    parentResponse.setPhone(userProfile.getPhone());
                    parentResponse.setEmail(userProfile.getEmail());
                    parentResponse.setAddress(userProfile.getAddress());
                    parentResponse.setDateOfBirth(userProfile.getDateOfBirth());
                    parentResponse.setGender(userProfile.getGender());
                    parentResponse.setRelationship(link.getRelationship());
                    
                    if (parent != null) {
                        parentResponse.setJob(parent.getJob());
                        parentResponse.setJobPlace(parent.getJobPlace());
                    }
                    
                    parentResponses.add(parentResponse);
                }
            }
            
            return ResponseEntity.ok(parentResponses);
            
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error retrieving parents: " + e.getMessage());
        }
    }
}
