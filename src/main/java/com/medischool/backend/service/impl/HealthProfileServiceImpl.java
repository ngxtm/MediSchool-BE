package com.medischool.backend.service.impl;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.medischool.backend.dto.healthprofile.HealthProfileRequestDTO;
import com.medischool.backend.dto.healthprofile.HealthProfileResponseDTO;
import com.medischool.backend.model.parentstudent.HealthProfile;
import com.medischool.backend.repository.HealthProfileRepository;
import com.medischool.backend.service.HealthProfileService;

@Service
@RequiredArgsConstructor
@Transactional
public class HealthProfileServiceImpl implements HealthProfileService {
    
    private final HealthProfileRepository healthProfileRepository;
    
    @Override
    public HealthProfileResponseDTO createHealthProfile(HealthProfileRequestDTO requestDTO) {
        if (healthProfileRepository.existsByStudentId(requestDTO.getStudentId())) {
            throw new RuntimeException("Health profile already exists for student ID: " + requestDTO.getStudentId());
        }
        
        HealthProfile healthProfile = HealthProfile.builder()
                .studentId(requestDTO.getStudentId())
                .height(requestDTO.getHeight())
                .weight(requestDTO.getWeight())
                .bloodType(requestDTO.getBloodType())
                .bloodPressure(requestDTO.getBloodPressure())
                .rightEye(requestDTO.getRightEye())
                .leftEye(requestDTO.getLeftEye())
                .ear(requestDTO.getEar())
                .nose(requestDTO.getNose())
                .throat(requestDTO.getThroat())
                .allergies(requestDTO.getAllergies())
                .chronicConditions(requestDTO.getChronicConditions())
                .treatmentHistory(requestDTO.getTreatmentHistory())
                .visionGrade(requestDTO.getVisionGrade())
                .hearingGrade(requestDTO.getHearingGrade())
                .build();
        
        HealthProfile savedProfile = healthProfileRepository.save(healthProfile);
        return mapToResponseDTO(savedProfile);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<HealthProfileResponseDTO> getHealthProfileByStudentId(Integer studentId) {
        return healthProfileRepository.findByStudentId(studentId)
                .map(this::mapToResponseDTO);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<HealthProfileResponseDTO> getHealthProfileById(UUID healthProfileId) {
        return healthProfileRepository.findById(healthProfileId)
                .map(this::mapToResponseDTO);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<HealthProfileResponseDTO> getAllHealthProfiles() {
        return healthProfileRepository.findAll().stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    public HealthProfileResponseDTO updateHealthProfile(UUID healthProfileId, HealthProfileRequestDTO requestDTO) {
        HealthProfile healthProfile = healthProfileRepository.findById(healthProfileId)
                .orElseThrow(() -> new RuntimeException("Health profile not found with ID: " + healthProfileId));
        
        updateHealthProfileFields(healthProfile, requestDTO);
        HealthProfile updatedProfile = healthProfileRepository.save(healthProfile);
        return mapToResponseDTO(updatedProfile);
    }
    
    @Override
    public HealthProfileResponseDTO updateHealthProfileByStudentId(Integer studentId, HealthProfileRequestDTO requestDTO) {
        Optional<HealthProfile> existingProfile = healthProfileRepository.findByStudentId(studentId);
        
        if (existingProfile.isPresent()) {
            // Update existing profile
            HealthProfile healthProfile = existingProfile.get();
            updateHealthProfileFields(healthProfile, requestDTO);
            HealthProfile updatedProfile = healthProfileRepository.save(healthProfile);
            return mapToResponseDTO(updatedProfile);
        } else {
            // Create new profile if it doesn't exist
            requestDTO.setStudentId(studentId);
            return createHealthProfile(requestDTO);
        }
    }
    
    @Override
    public void deleteHealthProfile(UUID healthProfileId) {
        if (!healthProfileRepository.existsById(healthProfileId)) {
            throw new RuntimeException("Health profile not found with ID: " + healthProfileId);
        }
        healthProfileRepository.deleteById(healthProfileId);
    }
    
    @Override
    public void deleteHealthProfileByStudentId(Integer studentId) {
        HealthProfile healthProfile = healthProfileRepository.findByStudentId(studentId)
                .orElseThrow(() -> new RuntimeException("Health profile not found for student ID: " + studentId));
        healthProfileRepository.delete(healthProfile);
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean existsByStudentId(Integer studentId) {
        return healthProfileRepository.existsByStudentId(studentId);
    }
    
    private void updateHealthProfileFields(HealthProfile healthProfile, HealthProfileRequestDTO requestDTO) {
        if (requestDTO.getHeight() != null) {
            healthProfile.setHeight(requestDTO.getHeight());
        }
        if (requestDTO.getWeight() != null) {
            healthProfile.setWeight(requestDTO.getWeight());
        }
        if (requestDTO.getBloodType() != null) {
            healthProfile.setBloodType(requestDTO.getBloodType());
        }
        if (requestDTO.getBloodPressure() != null) {
            healthProfile.setBloodPressure(requestDTO.getBloodPressure());
        }
        if (requestDTO.getRightEye() != null) {
            healthProfile.setRightEye(requestDTO.getRightEye());
        }
        if (requestDTO.getLeftEye() != null) {
            healthProfile.setLeftEye(requestDTO.getLeftEye());
        }
        if (requestDTO.getEar() != null) {
            healthProfile.setEar(requestDTO.getEar());
        }
        if (requestDTO.getNose() != null) {
            healthProfile.setNose(requestDTO.getNose());
        }
        if (requestDTO.getThroat() != null) {
            healthProfile.setThroat(requestDTO.getThroat());
        }
        if (requestDTO.getAllergies() != null) {
            healthProfile.setAllergies(requestDTO.getAllergies());
        }
        if (requestDTO.getChronicConditions() != null) {
            healthProfile.setChronicConditions(requestDTO.getChronicConditions());
        }
        if (requestDTO.getTreatmentHistory() != null) {
            healthProfile.setTreatmentHistory(requestDTO.getTreatmentHistory());
        }
        if (requestDTO.getVisionGrade() != null) {
            healthProfile.setVisionGrade(requestDTO.getVisionGrade());
        }
        if (requestDTO.getHearingGrade() != null) {
            healthProfile.setHearingGrade(requestDTO.getHearingGrade());
        }
    }
    
    private HealthProfileResponseDTO mapToResponseDTO(HealthProfile healthProfile) {
        return HealthProfileResponseDTO.builder()
                .healthProfileId(healthProfile.getHealthProfileId())
                .studentId(healthProfile.getStudentId())
                .height(healthProfile.getHeight())
                .weight(healthProfile.getWeight())
                .bloodType(healthProfile.getBloodType())
                .bloodPressure(healthProfile.getBloodPressure())
                .rightEye(healthProfile.getRightEye())
                .leftEye(healthProfile.getLeftEye())
                .ear(healthProfile.getEar())
                .nose(healthProfile.getNose())
                .throat(healthProfile.getThroat())
                .allergies(healthProfile.getAllergies())
                .chronicConditions(healthProfile.getChronicConditions())
                .treatmentHistory(healthProfile.getTreatmentHistory())
                .visionGrade(healthProfile.getVisionGrade())
                .hearingGrade(healthProfile.getHearingGrade())
                .createdAt(healthProfile.getCreatedAt())
                .updatedAt(healthProfile.getUpdatedAt())
                .build();
    }
} 