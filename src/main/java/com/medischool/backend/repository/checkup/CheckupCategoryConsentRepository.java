package com.medischool.backend.repository.checkup;

import com.medischool.backend.model.checkup.CheckupCategory;
import com.medischool.backend.model.checkup.CheckupCategoryConsent;
import com.medischool.backend.model.checkup.CheckupEventCategory;
import com.medischool.backend.model.checkup.CheckupEventConsent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CheckupCategoryConsentRepository extends JpaRepository<CheckupCategoryConsent, Long> {
    List<CheckupCategoryConsent> findByConsentId(Long consentId);
    boolean existsByConsentAndEventCategory(CheckupEventConsent consent, CheckupEventCategory category);

    List<CheckupCategoryConsent> findAllByConsentId(Long id);
}
