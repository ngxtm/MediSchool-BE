package com.medischool.backend.controller;

import com.medischool.backend.model.UserProfile;
import com.medischool.backend.repository.UserProfileRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@Tag(name = "Personal", description = "Information about user")
public class MeController {

    @Autowired
    private UserProfileRepository userProfileRepository;

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
}
