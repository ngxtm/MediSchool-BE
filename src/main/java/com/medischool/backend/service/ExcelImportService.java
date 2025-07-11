package com.medischool.backend.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.medischool.backend.dto.UserImportResponseDTO;
import com.medischool.backend.model.UserProfile;

/**
 * Service interface for Excel import/export operations related to user management
 * Handles both Supabase auth.users and local user_profile synchronization
 */
public interface ExcelImportService {
    
    /**
     * Import users from Excel file and create accounts in both Supabase auth and local database
     * @param file Excel file containing user data
     * @return Import result with success/failure counts and detailed errors
     */
    UserImportResponseDTO importUsersFromExcel(MultipartFile file);
    
    /**
     * Generate Excel template for user import with proper format and examples
     * @return Byte array of Excel template file
     */
    byte[] generateUserImportTemplate();
    
    /**
     * Export all users to Excel file for backup or analysis
     * @param users List of users to export
     * @return Byte array of Excel file
     */
    byte[] exportUsersToExcel(List<UserProfile> users);
    
    /**
     * Validate Excel file format and headers
     * @param file Excel file to validate
     * @return true if valid, false otherwise
     */
    boolean validateExcelFormat(MultipartFile file);
} 