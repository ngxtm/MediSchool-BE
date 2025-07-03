package com.medischool.backend.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.medischool.backend.dto.healthprofile.HealthProfileRequestDTO;
import com.medischool.backend.dto.healthprofile.HealthProfileResponseDTO;

public interface HealthProfileService {
    
    HealthProfileResponseDTO createHealthProfile(HealthProfileRequestDTO requestDTO);
    
    Optional<HealthProfileResponseDTO> getHealthProfileByStudentId(Integer studentId);
    
    Optional<HealthProfileResponseDTO> getHealthProfileById(UUID healthProfileId);
    
    List<HealthProfileResponseDTO> getAllHealthProfiles();
    
    HealthProfileResponseDTO updateHealthProfile(UUID healthProfileId, HealthProfileRequestDTO requestDTO);
    
    HealthProfileResponseDTO updateHealthProfileByStudentId(Integer studentId, HealthProfileRequestDTO requestDTO);
    
    void deleteHealthProfile(UUID healthProfileId);
    
    void deleteHealthProfileByStudentId(Integer studentId);
    
    boolean existsByStudentId(Integer studentId);
} 