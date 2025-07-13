package com.medischool.backend.dto.checkup;

import com.medischool.backend.model.checkup.CheckupCategoryConsent;
import com.medischool.backend.model.checkup.CheckupEventConsent;
import com.medischool.backend.model.enums.CheckupConsentStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CheckupConsentDTO {
    private Long id;
    private String eventTitle;
    private String schoolYear;
    private LocalDate startDate;
    private LocalDate endDate;
    private String consentStatus;
    private String note;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Integer studentId;
    private String studentName;
    private String studentCode;
    private String classCode;
    private String gender;
    private String dob;

    private String parentName;
    private String parentEmail;
    private String parentPhone;

    private boolean replied;
    private boolean accepted;

    private String eventStatus;

    private List<CheckupCategoryConsentDTO> categoryConsents;

    public CheckupConsentDTO(CheckupEventConsent entity, List<CheckupCategoryConsent> categories) {
        this.id = entity.getId();
        this.eventTitle = entity.getEvent().getEventTitle();
        this.schoolYear = entity.getEvent().getSchoolYear();
        this.startDate = entity.getEvent().getStartDate();
        this.endDate = entity.getEvent().getEndDate();
        this.consentStatus = entity.getConsentStatus().name();
        this.note = entity.getNote();
        this.createdAt = entity.getCreatedAt();
        this.updatedAt = entity.getUpdatedAt();

        this.studentId = entity.getStudent().getStudentId();
        this.studentName = entity.getStudent().getFullName();
        this.studentCode = entity.getStudent().getStudentCode();
        this.classCode = entity.getStudent().getClassCode();
        this.gender = entity.getStudent().getGender().name();
        this.dob = entity.getStudent().getDateOfBirth().toString();

        this.parentName = entity.getParent().getFullName();
        this.parentEmail = entity.getParent().getEmail();
        this.parentPhone = entity.getParent().getPhone();

        this.replied = entity.getConsentStatus() != CheckupConsentStatus.PENDING;
        this.accepted = entity.getConsentStatus() == CheckupConsentStatus.APPROVED;
        this.eventStatus = entity.getEvent().getStatus();

        this.categoryConsents = categories.stream()
                .map(CheckupCategoryConsentDTO::new)
                .toList();
    }
}