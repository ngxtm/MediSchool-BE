package com.medischool.backend.model.vaccine;


import jakarta.persistence.*;
import lombok.Data;
import java.io.Serializable;

@Entity
@Table(name = "vaccine_event_class")
@IdClass(VaccineEventClass.PK.class)
@Data
public class VaccineEventClass {
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