package com.medischool.backend.repository;


import com.medischool.backend.model.PeriodicCheckup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface PeriodicCheckupRepository extends JpaRepository<PeriodicCheckup,Long>, JpaSpecificationExecutor<PeriodicCheckup> {
}
