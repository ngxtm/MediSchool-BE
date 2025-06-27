package com.medischool.backend.service.impl;

import com.medischool.backend.model.vaccine.VaccineEventClass;
import com.medischool.backend.repository.VaccineEventClassRepository;
import com.medischool.backend.service.VaccineEventClassService;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VaccineEventClassServiceImpl implements VaccineEventClassService {
    private final VaccineEventClassRepository vaccineEventClassRepository;

    public List<VaccineEventClass> getAllClassInEventScopeClass(Long eventId) {
        return vaccineEventClassRepository.findAllByEventId(eventId);
    }
}
