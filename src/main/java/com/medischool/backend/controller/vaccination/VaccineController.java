package com.medischool.backend.controller.vaccination;

import com.medischool.backend.dto.vaccination.VaccineDTO;
import com.medischool.backend.service.vaccination.VaccinationConsentService;
import com.medischool.backend.service.vaccination.VaccineService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vaccines")
@RequiredArgsConstructor
@Tag(name = "Vaccine Management")
public class VaccineController {

    private final VaccineService vaccineService;
    private final VaccinationConsentService vaccinationConsentService;

    @GetMapping
    public ResponseEntity<List<VaccineDTO>> getAllVaccines() {
        return ResponseEntity.ok(vaccineService.getAllVaccines());
    }

    @GetMapping("{id}")
    public ResponseEntity<VaccineDTO> getVaccineById(@PathVariable int id) {
        if(vaccineService.getVaccineById(id) != null) {
            return ResponseEntity.ok(vaccineService.getVaccineById(id));
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping
    @Operation(summary = "Create a new vaccine")
    public ResponseEntity<VaccineDTO> createVaccine(@RequestBody VaccineDTO vaccineDTO) {
        VaccineDTO created = vaccineService.createVaccine(vaccineDTO);
        return ResponseEntity.ok(created);
    }

    @DeleteMapping("{id}")
    @Operation(summary = "Delete a vaccine by id")
    public ResponseEntity<Void> deleteVaccine(@PathVariable int id) {
        boolean deleted = vaccineService.deleteVaccine(id);
        if (deleted) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }


    @PutMapping("{id}")
    @Operation(summary = "Update a vaccine by id")
    public ResponseEntity<VaccineDTO> updateVaccine(
            @PathVariable int id,
            @RequestBody VaccineDTO vaccineDTO) {
        VaccineDTO updated = vaccineService.updateVaccine(id, vaccineDTO);
        if (updated != null) {
            return ResponseEntity.ok(updated);
        }
        return ResponseEntity.notFound().build();
    }
}