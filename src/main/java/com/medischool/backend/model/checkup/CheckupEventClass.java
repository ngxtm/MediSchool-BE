package com.medischool.backend.model.checkup;

import jakarta.persistence.*;
import lombok.Data;

import java.io.Serializable;

@Entity
@Table(name = "checkup_event_class")
@IdClass(CheckupEventClass.PK.class)
@Data
public class CheckupEventClass {

    @Id
    @Column(name = "event_id")
    private Long eventId;

    @Id
    @Column(name = "class_code")
    private String classCode;

    @Data
    public static class PK implements Serializable {
        private Long eventId;
        private String classCode;
    }
}