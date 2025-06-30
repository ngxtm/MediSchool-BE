package com.medischool.backend.repository;

import com.medischool.backend.model.ParentProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ParentProfileRepository extends JpaRepository<ParentProfile,Long> {
}
