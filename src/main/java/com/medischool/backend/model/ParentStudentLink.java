package com.medischool.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "parent_student_link")
@IdClass(ParentStudentLinkId.class)
public class ParentStudentLink {
    @Id
    @Column(name = "parent_id")
    private UUID parentId;

    @Id
    @Column(name = "student_id")
    private Integer studentId;

    private String relationship;
}