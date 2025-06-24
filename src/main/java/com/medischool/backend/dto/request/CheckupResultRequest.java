package com.medischool.backend.dto.request;


import com.medischool.backend.model.PeriodicCheckup;
import com.medischool.backend.model.StudentProfile;
import com.medischool.backend.model.UserProfile;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CheckupResultRequest {

    Long id;

    String result;

    String note;

    Boolean isNormal;

    Long nurseId;

    Long periodicCheckupId;

    Long studentProfileId;
}
