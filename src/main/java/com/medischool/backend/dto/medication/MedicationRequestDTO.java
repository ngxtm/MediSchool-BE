package com.medischool.backend.dto.medication;

import com.medischool.backend.model.UserProfile;
import com.medischool.backend.model.parentstudent.Student;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MedicationRequestDTO {
    private Integer requestId;
    private Integer studentId;
    private String title;
    private String reason;
    private LocalDate startDate;
    private LocalDate endDate;
    private String note;
    private OffsetDateTime createAt;
    private OffsetDateTime updateAt;
    private String medicationStatus;
    private UserProfile parent;
    private Student student;
    private List<MedicationRequestItemDTO> items;
    private List<MedicationDispensationDTO> dispensations;
    private String rejectReason;
    private String nurseName;
    private String managerName;
}