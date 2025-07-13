package com.medischool.backend.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.medischool.backend.model.UserProfile;

public interface UserProfileRepository extends JpaRepository<UserProfile, UUID> {
    UserProfile findSingleById(UUID id);
    Optional<UserProfile> findByEmail(String email);
    long countByDeletedAtIsNull();
}
