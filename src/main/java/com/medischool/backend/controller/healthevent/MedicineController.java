package com.medischool.backend.controller.healthevent;

import com.medischool.backend.model.healthevent.Medicine;
import com.medischool.backend.service.healthevent.MedicineService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/medicines")
@RequiredArgsConstructor
@Tag(name = "Medicine Controller", description = "Medicine management endpoints")
public class MedicineController {

    private final MedicineService medicineService;

    @GetMapping
    @Operation(summary = "Get all medicines", description = "Retrieve a list of all available medicines")
    public ResponseEntity<List<Medicine>> getAllMedicines() {
        try {
            List<Medicine> medicines = medicineService.getAllMedicines();
            return ResponseEntity.ok(medicines);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get medicine by ID", description = "Retrieve a specific medicine by its ID")
    public ResponseEntity<Medicine> getMedicineById(@PathVariable Long id) {
        try {
            Medicine medicine = medicineService.getMedicineById(id);
            if (medicine != null) {
                return ResponseEntity.ok(medicine);
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping
    @Operation(summary = "Create new medicine", description = "Create a new medicine record")
    public ResponseEntity<Medicine> createMedicine(@RequestBody Medicine medicine) {
        try {
            Medicine createdMedicine = medicineService.createMedicine(medicine);
            return ResponseEntity.ok(createdMedicine);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update medicine", description = "Update an existing medicine record")
    public ResponseEntity<Medicine> updateMedicine(@PathVariable Long id, @RequestBody Medicine medicine) {
        try {
            Medicine updatedMedicine = medicineService.updateMedicine(id, medicine);
            if (updatedMedicine != null) {
                return ResponseEntity.ok(updatedMedicine);
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete medicine", description = "Delete a medicine record")
    public ResponseEntity<Void> deleteMedicine(@PathVariable Long id) {
        try {
            medicineService.deleteMedicine(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
} 