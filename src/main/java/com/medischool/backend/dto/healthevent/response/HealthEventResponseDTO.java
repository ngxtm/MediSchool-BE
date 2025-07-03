package com.medischool.backend.dto.healthevent.response;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import com.medischool.backend.model.UserProfile;
import com.medischool.backend.model.healthevent.EventMedicine;
import com.medischool.backend.model.parentstudent.Student;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class HealthEventResponseDTO {
    private Long id;
    private Integer studentId;
    private Student student;
    private String problem;
    private String description;
    private String solution;
    private String location;
    private OffsetDateTime eventTime;
    private UUID recordBy;
    private UserProfile recordByUser;
    private String extent;
    private List<EventMedicine> eventMedicines;
} 