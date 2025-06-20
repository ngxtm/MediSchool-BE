package com.medischool.backend.model.Checkup;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table(name = "checkup_class")
@NoArgsConstructor
@AllArgsConstructor
@IdClass(CheckupClassId.class)
public class CheckupClass {
    @Id
    @Column(name = "checkup_id")
    private Integer checkupId;

    @Id
    @Column(name = "class_code")
    private String classCode;
}
