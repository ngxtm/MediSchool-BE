package com.medischool.backend.dto.healthevent.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TotalHealthEventStatusResDTO {
    private Integer totalHealthEvent;
    private Integer totalNormalCase;
    private Integer totalDangerousCase;
}
