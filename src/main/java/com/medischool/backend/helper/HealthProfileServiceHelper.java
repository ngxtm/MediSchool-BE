package com.medischool.backend.helper;


import com.medischool.backend.dto.request.HealthProfileRequest;
import com.medischool.backend.dto.response.HealthProfileResponse;
import com.medischool.backend.model.HealthProfile;
import com.medischool.backend.repository.HealthProfileRepository;
import com.medischool.backend.util.format.error.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class HealthProfileServiceHelper {
    private final HealthProfileRepository healthProfileRepository;

    public void isValidInfoSaveHealthProfile(HealthProfileRequest healthProfileRequest) throws CustomException {
        if(healthProfileRequest.getStudentId()==null){
            throw new CustomException("Student Id is required");
        }
        if(healthProfileRequest.getBloodType()==null){
            throw new CustomException("Blood Type is required");
        }
        if(healthProfileRequest.getAllergies()==null){
            throw new CustomException("Allergies is required");
        }
        if(healthProfileRequest.getHearingGrade()==null){
            throw new CustomException("Hearing Grade is required");
        }
        if(healthProfileRequest.getChronicConditions()==null){
            throw new CustomException("Chronic Conditions is required");
        }
        if(healthProfileRequest.getTreatmentHistory()==null){
            throw new CustomException("Treatment History is required");
        }
    }

    public HealthProfileResponse convertToHealthProfileResponse(HealthProfile healthProfile) throws CustomException {
        HealthProfileResponse healthProfileResponse = new HealthProfileResponse();
        healthProfileResponse.setStudentCode(healthProfile.getStudentProfile().getStudentCode());
        healthProfileResponse.setStudentName(healthProfile.getStudentProfile().getName());
        healthProfileResponse.setBloodType(healthProfile.getBloodType());
        healthProfileResponse.setAllergies(healthProfile.getAllergies());
        healthProfileResponse.setHearingGrade(healthProfile.getHearingGrade());
        healthProfileResponse.setChronicConditions(healthProfile.getChronicConditions());
        healthProfileResponse.setTreatmentHistory(healthProfile.getTreatmentHistory());
        healthProfileResponse.setCreatedAt(healthProfile.getCreatedAt());
        healthProfileResponse.setUpdatedAt(healthProfile.getUpdatedAt());
        healthProfileResponse.setProfileId(healthProfile.getHealthProfileId());
        return healthProfileResponse;
    }
}
