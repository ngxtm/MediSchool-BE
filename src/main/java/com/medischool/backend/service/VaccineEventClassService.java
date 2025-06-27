package com.medischool.backend.service;

import com.medischool.backend.model.vaccine.VaccineEventClass;
import com.medischool.backend.repository.VaccineEventClassRepository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VaccineEventClassService {
    List<VaccineEventClass> getAllClassInEventScopeClass(Long eventId);
}
