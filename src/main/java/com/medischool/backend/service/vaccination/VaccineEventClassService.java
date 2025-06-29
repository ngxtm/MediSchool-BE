package com.medischool.backend.service.vaccination;

import com.medischool.backend.model.vaccine.VaccineEventClass;

import java.util.List;

public interface VaccineEventClassService {
    List<VaccineEventClass> getAllClassInEventScopeClass(Long eventId);
}
