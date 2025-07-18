package com.medischool.backend.model.parentstudent;

import java.io.Serializable;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ParentStudentLinkId implements Serializable {
    private UUID parentId;
    private Integer studentId;
}
