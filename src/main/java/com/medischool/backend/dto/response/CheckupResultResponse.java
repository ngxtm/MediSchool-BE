package com.medischool.backend.dto.response;


import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CheckupResultResponse {
     String periodicName;

     String studentName;

     String studentCode;

     String result;

     String note;

     Boolean isNormal;
}
