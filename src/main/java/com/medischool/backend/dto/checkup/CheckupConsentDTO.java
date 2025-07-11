package com.medischool.backend.dto.checkup;

import com.medischool.backend.model.checkup.CheckupCategoryConsent;
import com.medischool.backend.model.checkup.CheckupEventConsent;
import com.medischool.backend.model.enums.CheckupConsentStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CheckupConsentDTO {
    private Long id;
    private Long eventId;
    private Integer studentId;
    private String studentCode;
    private String studentName;
    private String classCode;

    private String parentName;
    private String contactPhone;
    private String contactEmail;
    private LocalDateTime createdAt;
    private String schoolYear;

    private String consentStatus;
    private String note;

    private List<CheckupCategoryConsentDTO> categoryConsents;

    public CheckupConsentDTO(CheckupEventConsent entity, List<CheckupCategoryConsent> categoryConsentList) {
        this.id = entity.getId();
        this.eventId = entity.getEvent().getId();
        this.studentId = entity.getStudent().getStudentId();
        this.studentCode = entity.getStudent().getStudentCode();
        this.studentName = entity.getStudent().getFullName();
        this.classCode = entity.getStudent().getClassCode();

        this.parentName = entity.getParent().getFullName();
        this.contactPhone = entity.getParent().getPhone();
        this.contactEmail = entity.getParent().getEmail();
        this.createdAt = entity.getCreatedAt();
        this.schoolYear = entity.getEvent().getSchoolYear();

        CheckupConsentStatus status = entity.getConsentStatus();
        this.consentStatus = status != null ? status.name() : "NOT_SENT";
        this.note = entity.getNote();

        this.categoryConsents = categoryConsentList.stream()
                .map(CheckupCategoryConsentDTO::new)
                .toList();
    }
}