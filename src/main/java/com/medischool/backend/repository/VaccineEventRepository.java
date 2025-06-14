package com.medischool.backend.repository;

import com.medischool.backend.model.VaccineEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VaccineEventRepository extends JpaRepository<VaccineEvent, Long> {
}