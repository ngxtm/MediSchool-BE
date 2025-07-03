package com.medischool.backend.controller;


import com.medischool.backend.dto.request.HealthProfileRequest;
import com.medischool.backend.dto.response.HealthProfileResponse;
import com.medischool.backend.service.HealthProfileService;
import com.medischool.backend.util.format.error.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/health-profile")
@RequiredArgsConstructor
public class HealthProfileController{
    private final HealthProfileService healthProfileService;

    @PostMapping
    public ResponseEntity<HealthProfileResponse> saveHealthProfile(@RequestBody HealthProfileRequest healthProfileRequest)throws CustomException {
        return ResponseEntity.ok().body(this.healthProfileService.saveHealthProfile(healthProfileRequest));
    }
}

import com.medischool.backend.dto.healthprofile.HealthProfileRequestDTO;
import com.medischool.backend.dto.healthprofile.HealthProfileResponseDTO;
import com.medischool.backend.service.HealthProfileService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/health-profile")
@RequiredArgsConstructor
@Tag(name = "Health Profile", description = "Health Profile management APIs")
public class HealthProfileController {
    
    private final HealthProfileService healthProfileService;
    
    @PostMapping
    @Operation(summary = "Create a new health profile")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Health profile created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "409", description = "Health profile already exists for this student")
    })
    public ResponseEntity<HealthProfileResponseDTO> createHealthProfile(
            @RequestBody HealthProfileRequestDTO requestDTO) {
        try {
            HealthProfileResponseDTO response = healthProfileService.createHealthProfile(requestDTO);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(null, HttpStatus.CONFLICT);
        }
    }
    
    @GetMapping("/student/{studentId}")
    @Operation(summary = "Get health profile by student ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Health profile found"),
            @ApiResponse(responseCode = "404", description = "Health profile not found")
    })
    public ResponseEntity<HealthProfileResponseDTO> getHealthProfileByStudentId(
            @Parameter(description = "Student ID") @PathVariable Integer studentId) {
        Optional<HealthProfileResponseDTO> response = healthProfileService.getHealthProfileByStudentId(studentId);
        return response.map(profile -> ResponseEntity.ok(profile))
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/{healthProfileId}")
    @Operation(summary = "Get health profile by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Health profile found"),
            @ApiResponse(responseCode = "404", description = "Health profile not found")
    })
    public ResponseEntity<HealthProfileResponseDTO> getHealthProfileById(
            @Parameter(description = "Health Profile ID") @PathVariable UUID healthProfileId) {
        Optional<HealthProfileResponseDTO> response = healthProfileService.getHealthProfileById(healthProfileId);
        return response.map(profile -> ResponseEntity.ok(profile))
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping
    @Operation(summary = "Get all health profiles")
    @ApiResponse(responseCode = "200", description = "List of all health profiles")
    public ResponseEntity<List<HealthProfileResponseDTO>> getAllHealthProfiles() {
        List<HealthProfileResponseDTO> response = healthProfileService.getAllHealthProfiles();
        return ResponseEntity.ok(response);
    }
    
    @PutMapping("/{healthProfileId}")
    @Operation(summary = "Update health profile by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Health profile updated successfully"),
            @ApiResponse(responseCode = "404", description = "Health profile not found")
    })
    public ResponseEntity<HealthProfileResponseDTO> updateHealthProfile(
            @Parameter(description = "Health Profile ID") @PathVariable UUID healthProfileId,
            @RequestBody HealthProfileRequestDTO requestDTO) {
        try {
            HealthProfileResponseDTO response = healthProfileService.updateHealthProfile(healthProfileId, requestDTO);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @PutMapping("/student/{studentId}")
    @Operation(summary = "Update or create health profile by student ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Health profile updated successfully"),
            @ApiResponse(responseCode = "201", description = "Health profile created successfully")
    })
    public ResponseEntity<HealthProfileResponseDTO> updateHealthProfileByStudentId(
            @Parameter(description = "Student ID") @PathVariable Integer studentId,
            @RequestBody HealthProfileRequestDTO requestDTO) {
        boolean exists = healthProfileService.existsByStudentId(studentId);
        HealthProfileResponseDTO response = healthProfileService.updateHealthProfileByStudentId(studentId, requestDTO);
        
        if (exists) {
            return ResponseEntity.ok(response);
        } else {
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        }
    }
    
    @DeleteMapping("/{healthProfileId}")
    @Operation(summary = "Delete health profile by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Health profile deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Health profile not found")
    })
    public ResponseEntity<Void> deleteHealthProfile(
            @Parameter(description = "Health Profile ID") @PathVariable UUID healthProfileId) {
        try {
            healthProfileService.deleteHealthProfile(healthProfileId);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @DeleteMapping("/student/{studentId}")
    @Operation(summary = "Delete health profile by student ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Health profile deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Health profile not found")
    })
    public ResponseEntity<Void> deleteHealthProfileByStudentId(
            @Parameter(description = "Student ID") @PathVariable Integer studentId) {
        try {
            healthProfileService.deleteHealthProfileByStudentId(studentId);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/student/{studentId}/exists")
    @Operation(summary = "Check if health profile exists for student")
    @ApiResponse(responseCode = "200", description = "Returns true if health profile exists, false otherwise")
    public ResponseEntity<Boolean> checkHealthProfileExists(
            @Parameter(description = "Student ID") @PathVariable Integer studentId) {
        boolean exists = healthProfileService.existsByStudentId(studentId);
        return ResponseEntity.ok(exists);
    }
} 

