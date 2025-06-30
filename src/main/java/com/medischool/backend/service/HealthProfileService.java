package com.medischool.backend.service;


import com.medischool.backend.dto.request.HealthProfileRequest;
import com.medischool.backend.dto.response.HealthProfileResponse;
import com.medischool.backend.helper.HealthProfileServiceHelper;
import com.medischool.backend.model.HealthProfile;
import com.medischool.backend.model.StudentProfile;
import com.medischool.backend.repository.HealthProfileRepository;
import com.medischool.backend.repository.StudentRepository;
import com.medischool.backend.util.format.error.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class HealthProfileService {
    private final HealthProfileRepository healthProfileRepository;
    private final HealthProfileServiceHelper healthProfileServiceHelper;
    private final StudentRepository studentRepository;
    public HealthProfileResponse saveHealthProfile(HealthProfileRequest healthProfileRequest) throws CustomException {
        this.healthProfileServiceHelper.isValidInfoSaveHealthProfile(healthProfileRequest);
        HealthProfile currentHealthProfile=new HealthProfile();
        if(healthProfileRequest.getProfileId()!=null){
            currentHealthProfile=healthProfileRepository.findById(healthProfileRequest.getProfileId()).get();
        }
        StudentProfile currentStudentProfile=this.studentRepository.findById(healthProfileRequest.getStudentId()).get();
        if(currentStudentProfile==null){
            throw new CustomException("Student not Found");
        }
        currentHealthProfile=currentStudentProfile.getHealthProfile();
        currentHealthProfile.setAllergies(healthProfileRequest.getAllergies());
        currentHealthProfile.setBloodType(healthProfileRequest.getBloodType());
        currentHealthProfile.setChronicConditions(healthProfileRequest.getChronicConditions());
        currentHealthProfile.setHearingGrade(healthProfileRequest.getHearingGrade());
        currentHealthProfile.setTreatmentHistory(healthProfileRequest.getTreatmentHistory());
        currentHealthProfile.setVisionGrade(healthProfileRequest.getVisionGrade());
        this.healthProfileRepository.save(currentHealthProfile);

        HealthProfileResponse healthProfileResponse=this.healthProfileServiceHelper.convertToHealthProfileResponse(currentHealthProfile);
        return healthProfileResponse;

    }

}
