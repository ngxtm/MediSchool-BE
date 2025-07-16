package com.medischool.backend.service.checkup;

import com.medischool.backend.model.checkup.CheckupCategory;

import java.util.List;

public interface CheckupCategoryService {
    CheckupCategory createCategory(CheckupCategory category);
    List<CheckupCategory> getAllCategories();
    CheckupCategory getCategoryById(Long id);
    CheckupCategory updateCategory(Long id, CheckupCategory category);
    void deleteCategory(Long id);
} 