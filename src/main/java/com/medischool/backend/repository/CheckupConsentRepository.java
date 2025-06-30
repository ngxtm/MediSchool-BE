package com.medischool.backend.repository;

import com.medischool.backend.model.CheckUpConsent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface CheckupConsentRepository extends JpaRepository<CheckUpConsent,Long>, JpaSpecificationExecutor<CheckUpConsent> {
}
