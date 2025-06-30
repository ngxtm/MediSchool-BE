package com.medischool.backend.dto.request;


import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CheckupItemEntryRequest {
    Long checkupId;

    List<Long> itemIds;
}
