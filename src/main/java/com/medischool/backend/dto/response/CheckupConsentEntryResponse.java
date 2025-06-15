package com.medischool.backend.dto.response;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CheckupConsentEntryResponse {
    String studentCode;
    String studentName;
    List<String> checkUpItems;
}
