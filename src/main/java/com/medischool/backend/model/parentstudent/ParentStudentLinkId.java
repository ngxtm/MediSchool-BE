package com.medischool.backend.model.parentstudent;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ParentStudentLinkId implements Serializable {
    private UUID parentId;
    private Integer studentId;
}
