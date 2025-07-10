package com.medischool.backend.dto.checkup;

import com.medischool.backend.model.checkup.CheckupEventConsent;
import com.medischool.backend.model.enums.CheckupConsentStatus;
import com.medischool.backend.model.enums.ConsentStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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

    private String consentStatus;
    private String note;

    public CheckupConsentDTO(CheckupEventConsent entity) {
        this.id = entity.getId();
        this.eventId = entity.getEvent().getId();
        this.studentId = entity.getStudent().getStudentId();
        this.studentCode = entity.getStudent().getStudentCode();
        this.studentName = entity.getStudent().getFullName();
        this.classCode = entity.getStudent().getClassCode();

        this.parentName = entity.getParent().getFullName();
        this.contactPhone = entity.getParent().getPhone();
        this.contactEmail = entity.getParent().getEmail();

        CheckupConsentStatus status = entity.getConsentStatus();
        this.consentStatus = status != null ? status.name() : "NOT_SENT";
        this.note = entity.getNote();
    }
}