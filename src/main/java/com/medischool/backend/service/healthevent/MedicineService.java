package com.medischool.backend.service.healthevent;

import java.util.List;

import com.medischool.backend.model.healthevent.Medicine;

public interface MedicineService {
    List<Medicine> getAllMedicines();
    Medicine getMedicineById(Long id);
    Medicine createMedicine(Medicine medicine);
    Medicine updateMedicine(Long id, Medicine medicine);
    void deleteMedicine(Long id);
    List<Medicine> searchMedicinesByName(String name);
    List<Medicine> getMedicinesWithLowStock();
    boolean updateMedicineQuantity(Long medicineId, Integer quantityUsed);
} 