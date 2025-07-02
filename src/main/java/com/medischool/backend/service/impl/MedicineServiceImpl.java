package com.medischool.backend.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.medischool.backend.model.healthevent.Medicine;
import com.medischool.backend.repository.healthevent.MedicineRepository;
import com.medischool.backend.service.healthevent.MedicineService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MedicineServiceImpl implements MedicineService {

    private final MedicineRepository medicineRepository;

    @Override
    public List<Medicine> getAllMedicines() {
        return medicineRepository.findAll();
    }

    @Override
    public Medicine getMedicineById(Long id) {
        return medicineRepository.findById(id).orElse(null);
    }

    @Override
    public Medicine createMedicine(Medicine medicine) {
        return medicineRepository.save(medicine);
    }

    @Override
    public Medicine updateMedicine(Long id, Medicine medicine) {
        if (medicineRepository.existsById(id)) {
            medicine.setId(id);
            return medicineRepository.save(medicine);
        }
        return null;
    }

    @Override
    public void deleteMedicine(Long id) {
        medicineRepository.deleteById(id);
    }

    @Override
    public List<Medicine> searchMedicinesByName(String name) {
        return medicineRepository.findByNameContainingIgnoreCase(name);
    }

    @Override
    public List<Medicine> getMedicinesWithLowStock() {
        return medicineRepository.findAll().stream()
                .filter(medicine -> medicine.getQuantityOnHand() != null 
                        && medicine.getReorderThreshold() != null
                        && medicine.getQuantityOnHand() <= medicine.getReorderThreshold())
                .toList();
    }

    @Override
    public boolean updateMedicineQuantity(Long medicineId, Integer quantityUsed) {
        Medicine medicine = medicineRepository.findById(medicineId).orElse(null);
        if (medicine == null) {
            return false;
        }
        
        if (medicine.getQuantityOnHand() == null || medicine.getQuantityOnHand() < quantityUsed) {
            return false;
        }
        
        medicine.setQuantityOnHand(medicine.getQuantityOnHand() - quantityUsed);
        medicineRepository.save(medicine);
        
        return true;
    }
} 