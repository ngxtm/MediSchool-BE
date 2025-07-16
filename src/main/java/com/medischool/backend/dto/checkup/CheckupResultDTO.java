package com.medischool.backend.dto.checkup;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CheckupResultDTO {
    private String eventTitle;
    private String academicYear;
    private String createdDate;

    private String studentName;
    private String studentCode;
    private String classCode;
    private String gender;
    private String dob;

    private List<CheckupResultItemDTO> categoryResults;
}
