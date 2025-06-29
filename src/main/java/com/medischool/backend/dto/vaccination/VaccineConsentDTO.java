package com.medischool.backend.dto.vaccination;

import com.medischool.backend.model.UserProfile;
import com.medischool.backend.model.enums.ConsentStatus;
import com.medischool.backend.model.parentstudent.Student;
import com.medischool.backend.model.vaccine.VaccineEvent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VaccineConsentDTO {
    private Long id;
    private VaccineEvent event;
    private UserProfile parent;
    private Student student;
    private String note;
    private LocalDateTime createdAt;
    private ConsentStatus  consentStatus;
}