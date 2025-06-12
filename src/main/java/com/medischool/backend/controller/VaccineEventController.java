package com.medischool.backend.controller;

import com.medischool.backend.dto.VaccineEventRequestDTO;
import com.medischool.backend.model.VaccineEvent;
import com.medischool.backend.service.VaccineEventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/vaccine-events")
@RequiredArgsConstructor
@Tag(name = "Vaccine Events")
public class VaccineEventController {

    private final VaccineEventService vaccineEventService;

    @PostMapping
    @Operation(summary = "Create a new vaccine event")
    public ResponseEntity<VaccineEvent> createVaccineEvent(@RequestBody VaccineEventRequestDTO requestDTO) {
        return ResponseEntity.ok(vaccineEventService.createVaccineEvent(requestDTO));
    }
}