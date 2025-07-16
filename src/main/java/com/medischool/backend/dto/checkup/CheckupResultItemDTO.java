package com.medischool.backend.dto.checkup;

import com.medischool.backend.model.checkup.CheckupResultItem;
import com.medischool.backend.model.enums.ResultStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class CheckupResultItemDTO {
    private String name;
    private String value;
    private String status;

    public CheckupResultItemDTO(CheckupResultItem entity) {
        this.name = entity.getEventCategory().getCategory().getName();
        this.value = entity.getValue();
        ResultStatus status = entity.getStatus();
        this.status = status.name();
    }
}