package com.medischool.backend.dto.checkup;

import com.medischool.backend.model.checkup.CheckupCategoryConsent;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CheckupCategoryConsentDTO {
    private Long eventCategoryId;
    private String categoryName;
    private String status;
    private String note;

    public CheckupCategoryConsentDTO(CheckupCategoryConsent entity) {
        this.eventCategoryId = entity.getEventCategory().getId();
        this.categoryName = entity.getEventCategory().getCategory().getName(); // nếu có CheckupCategory trong EventCategory
        this.status = entity.getCategoryConsentStatus() != null ? entity.getCategoryConsentStatus().name() : "PENDING";
        this.note = entity.getNote();
    }
}