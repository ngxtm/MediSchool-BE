package com.medischool.backend.repository;

import com.medischool.backend.model.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, UUID>{
    Optional<UserProfile> findById(UUID id);
    UserProfile findSingleById(UUID id);
}
