package com.medischool.backend.repository;

import com.medischool.backend.model.Vaccine.VaccineEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface VaccineEventRepository extends JpaRepository<VaccineEvent, Long> {
    List<VaccineEvent> findAllByEventDateBetween(LocalDate startDate, LocalDate endDate);
}