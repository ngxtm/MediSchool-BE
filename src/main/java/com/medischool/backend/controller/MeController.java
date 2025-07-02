package com.medischool.backend.controller;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.medischool.backend.model.UserProfile;
import com.medischool.backend.model.parentstudent.ParentStudentLink;
import com.medischool.backend.repository.ParentStudentLinkRepository;
import com.medischool.backend.repository.StudentRepository;
import com.medischool.backend.repository.UserProfileRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api")
@Tag(name = "Personal", description = "Information about user")
public class MeController {

    @Autowired
    private UserProfileRepository userProfileRepository;
    
    @Autowired
    private ParentStudentLinkRepository parentStudentLinkRepository;
    
    @Autowired
    private StudentRepository studentRepository;

    @GetMapping("/me")
    @Operation(summary = "Get user information")
    public ResponseEntity<?> getCurrentUser(Authentication authentication)
    {
        try {
            String userIdStr = authentication.getName();
            UUID userId = UUID.fromString(userIdStr);
            Optional<UserProfile> profileOpt = userProfileRepository.findById(userId);
            if (profileOpt.isPresent()) {
                return ResponseEntity.ok(profileOpt.get());
            } else {
                return ResponseEntity.status(404).body("User not found in user_profile");
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error retrieving user: " + e.getMessage());
        }
    }
    
    @GetMapping("/me/students")
    @Operation(summary = "Get students of current parent")
    public ResponseEntity<?> getMyStudents(Authentication authentication) {
        try {
            String userIdStr = authentication.getName();
            UUID parentId = UUID.fromString(userIdStr);

            List<ParentStudentLink> links = parentStudentLinkRepository.findByParentId(parentId);

            List<Integer> studentIds = links.stream()
                    .map(ParentStudentLink::getStudentId)
                    .collect(Collectors.toList());
            
            if (studentIds.isEmpty()) {
                return ResponseEntity.ok(List.of());
            }
            
            List<?> students = studentRepository.findAllById(studentIds);
            return ResponseEntity.ok(students);
            
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error retrieving students: " + e.getMessage());
        }
    }
}
