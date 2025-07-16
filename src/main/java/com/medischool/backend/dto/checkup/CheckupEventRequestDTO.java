package com.medischool.backend.dto.checkup;

import com.medischool.backend.model.enums.CheckupEventScope;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckupEventRequestDTO {
    private String eventTitle;
    private String schoolYear;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;
    private List<Long> categoryIds;
}

