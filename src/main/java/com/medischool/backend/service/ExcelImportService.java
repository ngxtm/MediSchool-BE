package com.medischool.backend.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.medischool.backend.dto.UserImportResponseDTO;
import com.medischool.backend.dto.student.StudentImportResponseDTO;
import com.medischool.backend.model.UserProfile;
import com.medischool.backend.model.parentstudent.Student;

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
    
    /**
     * Import students from Excel file
     * @param file Excel file containing student data
     * @return Import result with success/failure counts and detailed errors
     */
    StudentImportResponseDTO importStudentsFromExcel(MultipartFile file);

    /**
     * Export all students to Excel file for backup or analysis
     * @param students List of students to export
     * @return Byte array of Excel file
     */
    byte[] generateStudentListExcel(List<Student> students);
} 