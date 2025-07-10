package com.medischool.backend.dto.checkup;

import com.medischool.backend.model.checkup.CheckupCategoryConsent;
import com.medischool.backend.model.enums.CheckupConsentStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CheckupCategoryConsentDTO {
    private Long id;
    private Long consentId;
    private Long eventCategoryId;
    private CheckupConsentStatus categoryConsentStatus;
    private String note;

    public CheckupCategoryConsentDTO(CheckupCategoryConsent consent) {
        this.id = consent.getId();
        this.consentId = consent.getConsent().getId();
        this.eventCategoryId = consent.getEventCategory().getId();
        this.categoryConsentStatus = consent.getCategoryConsentStatus();
        this.note = consent.getNote();
    }
}

