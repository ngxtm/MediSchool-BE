package com.medischool.backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "parent")
public class Parent {
    @Id
    @Column(name = "parent_id")
    private UUID parentId;

    private String job;

    @Column(name = "job_place")
    private String jobPlace;
}