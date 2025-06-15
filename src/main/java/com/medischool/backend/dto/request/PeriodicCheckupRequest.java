package com.medischool.backend.dto.request;


import ch.qos.logback.core.rolling.helper.PeriodicityType;
import com.medischool.backend.util.constant.CheckupScopeType;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PeriodicCheckupRequest {
    Long periodicCheckupId;

    String periodicTitle;

    String periodicDate;

    String schoolYear;

    CheckupScopeType scope;

    String status;

    String text;

    List<String> classCode;

    List<Long> idsCheckupConsent;


}
