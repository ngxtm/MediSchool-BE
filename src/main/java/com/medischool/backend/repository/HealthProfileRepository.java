package com.medischool.backend.repository;


import com.medischool.backend.model.HealthProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface HealthProfileRepository extends JpaRepository<HealthProfile, UUID>, JpaSpecificationExecutor<HealthProfile> {

}

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.medischool.backend.model.parentstudent.HealthProfile;

@Repository
public interface HealthProfileRepository extends JpaRepository<HealthProfile, UUID> {
    
    Optional<HealthProfile> findByStudentId(Integer studentId);
    
    @Query("SELECT hp FROM HealthProfile hp WHERE hp.studentId = :studentId")
    Optional<HealthProfile> findHealthProfileByStudentId(@Param("studentId") Integer studentId);
    
    boolean existsByStudentId(Integer studentId);
} 

