package com.medischool.backend.controller.vaccination;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.medischool.backend.annotation.LogActivity;
import com.medischool.backend.dto.vaccination.VaccineDTO;
import com.medischool.backend.model.ActivityLog.ActivityType;
import com.medischool.backend.model.ActivityLog.EntityType;
import com.medischool.backend.model.vaccine.VaccineCategory;
import com.medischool.backend.repository.vaccination.VaccineCategoryRepository;
import com.medischool.backend.service.vaccination.VaccineService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/vaccines")
@RequiredArgsConstructor
@Tag(name = "Vaccine Management")
public class VaccineController {

    private final VaccineService vaccineService;
    
    @Autowired
    private VaccineCategoryRepository categoryRepository;

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
    @LogActivity(
        actionType = ActivityType.CREATE,
        entityType = EntityType.VACCINE,
        description = "Tạo vaccine mới: {vaccineName}"
    )
    public ResponseEntity<VaccineDTO> createVaccine(@RequestBody VaccineDTO vaccineDTO) {
        VaccineDTO created = vaccineService.createVaccine(vaccineDTO);
        return ResponseEntity.ok(created);
    }

    @DeleteMapping("{id}")
    @Operation(summary = "Delete a vaccine by id")
    @LogActivity(
        actionType = ActivityType.DELETE,
        entityType = EntityType.VACCINE,
        description = "Xóa vaccine: {id}",
        entityIdParam = "id"
    )
    public ResponseEntity<Void> deleteVaccine(@PathVariable int id) {
        boolean deleted = vaccineService.deleteVaccine(id);
        if (deleted) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }


    @PutMapping("{id}")
    @Operation(summary = "Update a vaccine by id")
    @LogActivity(
        actionType = ActivityType.UPDATE,
        entityType = EntityType.VACCINE,
        description = "Cập nhật vaccine: {vaccineName}",
        entityIdParam = "id"
    )
    public ResponseEntity<VaccineDTO> updateVaccine(
            @PathVariable int id,
            @RequestBody VaccineDTO vaccineDTO) {
        VaccineDTO updated = vaccineService.updateVaccine(id, vaccineDTO);
        if (updated != null) {
            return ResponseEntity.ok(updated);
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/categories")
    public List<VaccineCategory> getAllCategories() {
        return categoryRepository.findAll();
    }

    @PutMapping("/categories/{categoryId}")
    @LogActivity(
        actionType = ActivityType.UPDATE,
        entityType = EntityType.VACCINE,
        description = "Cập nhật danh mục vaccine: {categoryId}",
        entityIdParam = "categoryId"
    )
    public VaccineCategory updateCategory(@PathVariable Integer categoryId, @RequestBody VaccineCategory update) {
        Optional<VaccineCategory> opt = categoryRepository.findById(categoryId);
        if (opt.isEmpty()) throw new RuntimeException("Category not found");
        VaccineCategory category = opt.get();
        category.setCategoryName(update.getCategoryName());
        category.setDoseRequired(update.getDoseRequired());
        return categoryRepository.save(category);
    }
}