package com.medischool.backend.service.impl;

import com.medischool.backend.model.vaccine.VaccineEventClass;
import com.medischool.backend.repository.vaccination.VaccineEventClassRepository;
import com.medischool.backend.service.vaccination.VaccineEventClassService;
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
