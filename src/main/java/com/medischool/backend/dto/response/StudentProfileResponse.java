package com.medischool.backend.dto.response;


import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StudentProfileResponse {
    String studentCode;
    String studentName;
    String classCode;;
}
