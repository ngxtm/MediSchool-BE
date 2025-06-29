package com.medischool.backend.dto.response;


import com.medischool.backend.model.CheckUpConsent;
import com.medischool.backend.util.constant.CheckupScopeType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PeriodicCheckupResponse {
    Long checkUpId;

    String checkUpTitle;

    Instant scheduleDate;

    String schoolYear;

    CheckupScopeType  checkupScopeType;

    String text;

    Long createdBy;

    Instant createdAt;

    String status;

    List<String> classCode;

    Boolean isDeleted;

    List<String> itemsName;





}
