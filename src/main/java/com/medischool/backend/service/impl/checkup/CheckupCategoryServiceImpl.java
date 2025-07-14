package com.medischool.backend.service.impl.checkup;

import com.medischool.backend.model.checkup.CheckupCategory;
import com.medischool.backend.repository.checkup.CheckupCategoryRepository;
import com.medischool.backend.service.checkup.CheckupCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CheckupCategoryServiceImpl implements CheckupCategoryService {
    private final CheckupCategoryRepository checkupCategoryRepository;

    @Override
    public CheckupCategory createCategory(CheckupCategory category) {
        return checkupCategoryRepository.save(category);
    }

    @Override
    public List<CheckupCategory> getAllCategories() {
        return checkupCategoryRepository.findAll();
    }

    @Override
    public CheckupCategory getCategoryById(Long id) {
        return checkupCategoryRepository.findById(id).orElse(null);
    }

    @Override
    public CheckupCategory updateCategory(Long id, CheckupCategory category) {
        category.setId(id);
        return checkupCategoryRepository.save(category);
    }

    @Override
    public void deleteCategory(Long id) {
        checkupCategoryRepository.deleteById(id);
    }
} 