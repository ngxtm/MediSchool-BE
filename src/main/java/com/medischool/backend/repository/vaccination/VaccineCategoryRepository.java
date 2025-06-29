package com.medischool.backend.repository.vaccination;

import com.medischool.backend.model.vaccine.VaccineCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VaccineCategoryRepository extends JpaRepository<VaccineCategory, Integer> {
} 