package com.medischool.backend.controller;

import com.medischool.backend.dto.VaccineDTO;
import com.medischool.backend.dto.student.VaccinationGroupDTO;
import com.medischool.backend.service.VaccineService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/vaccines")
@RequiredArgsConstructor
@Tag(name = "Vaccine Management")
public class VaccineController {

    private final VaccineService vaccineService;

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

    @GetMapping("/{id}/vaccinations")
    public ResponseEntity<List<VaccinationGroupDTO>> get(@PathVariable Long id) {
        return ResponseEntity.ok(vaccineService.getHistoryByStudent(id));
    }
}