package com.medischool.backend.repository;

import com.medischool.backend.model.Vaccine.VaccinationHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VaccinationHistoryRepository extends JpaRepository<VaccinationHistory, Integer> {
}