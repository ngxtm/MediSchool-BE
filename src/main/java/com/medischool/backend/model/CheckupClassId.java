package com.medischool.backend.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CheckupClassId implements Serializable {
    private Integer checkupId;
    private String classCode;
}
