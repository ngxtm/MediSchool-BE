package com.medischool.backend.dto.vaccination;

import com.medischool.backend.model.vaccine.VaccinationHistory;
import com.medischool.backend.model.parentstudent.Student;
import lombok.Data;

@Data
public class VaccinationHistoryWithStudentDTO {
    private VaccinationHistory history;
    private Student student;
}