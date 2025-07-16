package com.medischool.backend.dto.checkup;

import com.medischool.backend.model.checkup.CheckupCategoryConsent;
import com.medischool.backend.model.enums.CheckupConsentStatus;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CheckupCategoryConsentDTO {
    private Long id;
    private Long categoryId;
    private String categoryName;
    private CheckupConsentStatus categoryConsentStatus;

    public CheckupCategoryConsentDTO(CheckupCategoryConsent entity) {
        this.id = entity.getId();
        this.categoryId = entity.getEventCategory().getId();
        this.categoryName = entity.getEventCategory().getCategory().getName();
        this.categoryConsentStatus = entity.getCategoryConsentStatus();
    }
}