package com.medischool.backend.dto.vaccination;

import com.medischool.backend.model.UserProfile;
import com.medischool.backend.model.enums.ConsentStatus;
import com.medischool.backend.model.parentstudent.Student;
import com.medischool.backend.model.vaccine.VaccinationConsent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VaccineConsentInEvent {
    private Long consentId;
    private String studentName;
    private String classCode;
    private String parentName;
    private String email;
    private String phoneNumber;
    private ConsentStatus status;

    public static VaccineConsentInEvent converToDTO(Student student, UserProfile parent, VaccinationConsent consent) {
        return VaccineConsentInEvent.builder()
                .consentId(consent.getId())
                .studentName(student.getFullName())
                .classCode(student.getClassCode())
                .parentName(parent.getFullName())
                .email(parent.getEmail())
                .phoneNumber(parent.getPhone())
                .status(consent.getConsentStatus())
                .build();
    }
}
