package com.medischool.backend.dto.checkup;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CheckupOverallResultDTO {
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate eventDate;

    private String status;
    private String note;
}
