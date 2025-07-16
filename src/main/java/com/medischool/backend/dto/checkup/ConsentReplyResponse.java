package com.medischool.backend.dto.checkup;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ConsentReplyResponse {
    private CheckupConsentDTO consent;
    private List<CheckupResultItemDTO> createdItems;
}
