package com.medischool.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "medication_request")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MedicationRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "request_id")
    private Integer requestId;

    @Column(name = "student_id")
    private Integer studentId;

    @Column(name = "parent_id")
    private UUID parentId;

    private String status; // PENDING, APPROVED, REJECTED, COMPLETED
    private String reason;

    @Column(name = "reject_reason")
    private String rejectReason;

    @Column(name = "confirm_by")
    private UUID confirmBy;

    @Column(name = "create_at")
    private OffsetDateTime createdAt;

    @Column(name = "update_at")
    private OffsetDateTime updateAt;

    @Column(name = "is_final_dose")
    private Boolean isFinalDose;
}
