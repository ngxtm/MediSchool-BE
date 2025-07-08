package com.medischool.backend.dto.vaccination;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SelectiveEmailRequestDTO {
    private List<Long> consentIds;
} 