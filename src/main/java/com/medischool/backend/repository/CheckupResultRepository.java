package com.medischool.backend.repository;

import com.medischool.backend.model.CheckupResult;
import jdk.jfr.Registered;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CheckupResultRepository extends JpaRepository<CheckupResult, Long> {
}
