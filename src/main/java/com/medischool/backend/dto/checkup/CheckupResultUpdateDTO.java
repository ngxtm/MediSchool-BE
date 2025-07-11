package com.medischool.backend.dto.checkup;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CheckupResultUpdateDTO {
    private String value;
    private String status;
}
