package com.medischool.backend.model.healthevent;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.medischool.backend.model.parentstudent.Student;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "health_event")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HealthEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "event_id")
    private Long id;

    @Column(name = "student_id")
    private Integer studentId;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "student_id", insertable = false, updatable = false)
    private Student student;

    private String problem;
    private String description;
    private String solution;
    private String location;

    @Column(name = "event_time")
    private OffsetDateTime eventTime;

    @Column(name = "record_by")
    private UUID recordBy;

    private String extent;
}
