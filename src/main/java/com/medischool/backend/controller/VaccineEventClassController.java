package com.medischool.backend.controller;

import com.medischool.backend.model.vaccine.VaccineEventClass;
import com.medischool.backend.repository.VaccineEventClassRepository;
import com.medischool.backend.service.impl.VaccineEventClassServiceImpl;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/vaccine-event-class")
@RequiredArgsConstructor
@Tag(name = "Vaccine Event Class Controller")
public class VaccineEventClassController {
    private final VaccineEventClassServiceImpl vaccineEventClassService;
    @GetMapping("{eventId}")
    public List<VaccineEventClass> getAllClassInEventScopeClass(@PathVariable("eventId") Long eventId) {
        return vaccineEventClassService.getAllClassInEventScopeClass(eventId);
    }
}
