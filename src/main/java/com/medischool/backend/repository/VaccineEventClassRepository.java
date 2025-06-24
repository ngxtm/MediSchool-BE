package com.medischool.backend.repository;

import com.medischool.backend.model.vaccine.VaccineEventClass;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VaccineEventClassRepository extends JpaRepository<VaccineEventClass, VaccineEventClass.PK> {
    boolean existsByEventIdAndClassCode(Long eventId, String classCode);
    List<VaccineEventClass> findAllByEventId(Long eventId);
}