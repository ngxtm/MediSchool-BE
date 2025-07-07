package com.medischool.backend.controller.checkup;

import com.medischool.backend.model.checkup.CheckupCategory;
import com.medischool.backend.service.checkup.CheckupCategoryService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/checkup-categories")
@RequiredArgsConstructor
public class CheckupCategoryController {
    private final CheckupCategoryService checkupCategoryService;

    @PostMapping
    @Operation(summary = "Create a new checkup category")
    public ResponseEntity<CheckupCategory> createCategory(@RequestBody CheckupCategory category) {
        return ResponseEntity.ok(checkupCategoryService.createCategory(category));
    }

    @GetMapping
    @Operation(summary = "Get all checkup categories")
    public ResponseEntity<List<CheckupCategory>> getAllCategories() {
        return ResponseEntity.ok(checkupCategoryService.getAllCategories());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get checkup category by ID")
    public ResponseEntity<CheckupCategory> getCategoryById(@PathVariable Long id) {
        CheckupCategory category = checkupCategoryService.getCategoryById(id);
        if (category == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(category);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update checkup category")
    public ResponseEntity<CheckupCategory> updateCategory(@PathVariable Long id, @RequestBody CheckupCategory category) {
        return ResponseEntity.ok(checkupCategoryService.updateCategory(id, category));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete checkup category")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        checkupCategoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }
} 