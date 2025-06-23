package com.medischool.backend.repository;

import com.medischool.backend.model.vaccine.VaccinationHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VaccinationHistoryRepository extends JpaRepository<VaccinationHistory, Integer> {
    List<VaccinationHistory> findByEventId(Long eventId);
}