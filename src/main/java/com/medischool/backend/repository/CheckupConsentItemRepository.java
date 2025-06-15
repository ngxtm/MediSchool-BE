package com.medischool.backend.repository;


import com.medischool.backend.model.CheckUpConsentItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface CheckupConsentItemRepository extends JpaRepository<CheckUpConsentItem,Long>, JpaSpecificationExecutor<CheckUpConsentItem> {
}
