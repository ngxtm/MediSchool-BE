package com.medischool.backend.service.impl;

import com.medischool.backend.dto.student.StudentImportDTO;
import com.medischool.backend.dto.student.StudentImportResponseDTO;
import com.medischool.backend.model.enums.Gender;
import com.medischool.backend.model.enums.StudentStatus;
import com.medischool.backend.model.parentstudent.Student;
import com.medischool.backend.repository.StudentRepository;
import com.medischool.backend.service.ExcelImportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExcelImportServiceImpl implements ExcelImportService {

    private final StudentRepository studentRepository;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATE_FORMATTER_2 = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Override
    @Transactional
    public StudentImportResponseDTO importStudentsFromExcel(MultipartFile file) {
        List<StudentImportDTO> errors = new ArrayList<>();
        int successCount = 0;
        int totalRows = 0;

        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            
            // Skip header row
            for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row == null) continue;
                
                totalRows++;
                StudentImportDTO studentImport = parseRow(row, rowIndex + 1);
                
                if (studentImport.isValid()) {
                    try {
                        Student student = convertToStudent(studentImport);
                        studentRepository.save(student);
                        successCount++;
                        log.info("Successfully imported student: {}", student.getFullName());
                    } catch (Exception e) {
                        studentImport.setValid(false);
                        studentImport.setErrorMessage("Database error: " + e.getMessage());
                        errors.add(studentImport);
                        log.error("Failed to save student at row {}: {}", rowIndex + 1, e.getMessage());
                    }
                } else {
                    errors.add(studentImport);
                }
            }
            
            String message = String.format("Import completed. Success: %d, Errors: %d", successCount, errors.size());
            return StudentImportResponseDTO.builder()
                    .success(errors.isEmpty())
                    .totalRows(totalRows)
                    .successCount(successCount)
                    .errorCount(errors.size())
                    .errors(errors)
                    .message(message)
                    .build();
                    
        } catch (IOException e) {
            log.error("Error reading Excel file: {}", e.getMessage());
            return StudentImportResponseDTO.builder()
                    .success(false)
                    .totalRows(0)
                    .successCount(0)
                    .errorCount(0)
                    .errors(new ArrayList<>())
                    .message("Error reading Excel file: " + e.getMessage())
                    .build();
        }
    }

    private StudentImportDTO parseRow(Row row, int rowNumber) {
        StudentImportDTO studentImport = StudentImportDTO.builder()
                .rowNumber(rowNumber)
                .isValid(true)
                .build();

        try {
            // Parse each column
            studentImport.setStudentCode(getStringCellValue(row.getCell(0), "Student Code"));
            studentImport.setFullName(getStringCellValue(row.getCell(1), "Full Name"));
            studentImport.setClassCode(getStringCellValue(row.getCell(2), "Class Code"));
            studentImport.setGrade(getIntegerCellValue(row.getCell(3), "Grade"));
            studentImport.setDateOfBirth(getDateCellValue(row.getCell(4), "Date of Birth"));
            studentImport.setAddress(getStringCellValue(row.getCell(5), "Address"));
            studentImport.setGender(getStringCellValue(row.getCell(6), "Gender"));
            studentImport.setEnrollmentDate(getDateCellValue(row.getCell(7), "Enrollment Date"));
            studentImport.setEmergencyContact(getStringCellValue(row.getCell(8), "Emergency Contact"));
            studentImport.setEmergencyPhone(getStringCellValue(row.getCell(9), "Emergency Phone"));
            studentImport.setStatus(getStringCellValue(row.getCell(10), "Status"));
            studentImport.setAvatar(getStringCellValue(row.getCell(11), "Avatar"));

            // Validate required fields
            validateStudentImport(studentImport);

        } catch (Exception e) {
            studentImport.setValid(false);
            studentImport.setErrorMessage("Parsing error: " + e.getMessage());
        }

        return studentImport;
    }

    private String getStringCellValue(Cell cell, String fieldName) {
        if (cell == null) return null;
        
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                return String.valueOf((int) cell.getNumericCellValue());
            default:
                return null;
        }
    }

    private Integer getIntegerCellValue(Cell cell, String fieldName) {
        if (cell == null) return null;
        
        switch (cell.getCellType()) {
            case NUMERIC:
                return (int) cell.getNumericCellValue();
            case STRING:
                try {
                    return Integer.parseInt(cell.getStringCellValue().trim());
                } catch (NumberFormatException e) {
                    return null;
                }
            default:
                return null;
        }
    }

    private LocalDate getDateCellValue(Cell cell, String fieldName) {
        if (cell == null) return null;
        switch (cell.getCellType()) {
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getLocalDateTimeCellValue().toLocalDate();
                }
                return null;
            case STRING:
                String value = cell.getStringCellValue().trim();
                try {
                    return LocalDate.parse(value, DATE_FORMATTER);
                } catch (DateTimeParseException e1) {
                    try {
                        return LocalDate.parse(value, DATE_FORMATTER_2);
                    } catch (DateTimeParseException e2) {
                        return null;
                    }
                }
            default:
                return null;
        }
    }

    private void validateStudentImport(StudentImportDTO studentImport) {
        List<String> errors = new ArrayList<>();

        // Required fields validation
        if (studentImport.getStudentCode() == null || studentImport.getStudentCode().isEmpty()) {
            errors.add("Student Code is required");
        }
        if (studentImport.getFullName() == null || studentImport.getFullName().isEmpty()) {
            errors.add("Full Name is required");
        }
        if (studentImport.getClassCode() == null || studentImport.getClassCode().isEmpty()) {
            errors.add("Class Code is required");
        }

        // Gender validation
        if (studentImport.getGender() != null && !studentImport.getGender().isEmpty()) {
            try {
                Gender.valueOf(studentImport.getGender().toUpperCase());
            } catch (IllegalArgumentException e) {
                errors.add("Invalid gender value. Must be MALE, FEMALE, or OTHER");
            }
        }

        // Status validation
        if (studentImport.getStatus() != null && !studentImport.getStatus().isEmpty()) {
            try {
                StudentStatus.valueOf(studentImport.getStatus().toUpperCase());
            } catch (IllegalArgumentException e) {
                errors.add("Invalid status value. Must be ACTIVE, INACTIVE, or SUSPENDED");
            }
        }

        if (!errors.isEmpty()) {
            studentImport.setValid(false);
            studentImport.setErrorMessage(String.join("; ", errors));
        }
    }

    private Student convertToStudent(StudentImportDTO studentImport) {
        Student student = new Student();
        student.setStudentCode(studentImport.getStudentCode());
        student.setFullName(studentImport.getFullName());
        student.setClassCode(studentImport.getClassCode());
        student.setGrade(studentImport.getGrade());
        student.setDateOfBirth(studentImport.getDateOfBirth());
        student.setAddress(studentImport.getAddress());
        
        if (studentImport.getGender() != null && !studentImport.getGender().isEmpty()) {
            student.setGender(Gender.valueOf(studentImport.getGender().toUpperCase()));
        }
        
        student.setEnrollmentDate(studentImport.getEnrollmentDate());
        student.setEmergencyContact(studentImport.getEmergencyContact());
        student.setEmergencyPhone(studentImport.getEmergencyPhone());
        
        if (studentImport.getStatus() != null && !studentImport.getStatus().isEmpty()) {
            student.setStatus(StudentStatus.valueOf(studentImport.getStatus().toUpperCase()));
        } else {
            student.setStatus(StudentStatus.ACTIVE); // Default status
        }
        
        student.setAvatar(studentImport.getAvatar());
        
        return student;
    }


    public byte[] generateStudentImportTemplate() {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            
            Sheet sheet = workbook.createSheet("Students");
            
            // Create header row
            Row headerRow = sheet.createRow(0);
            String[] headers = {
                "Student Code*", "Full Name*", "Class Code*", "Grade", "Date of Birth (dd/MM/yyyy)", 
                "Address", "Gender (MALE/FEMALE/OTHER)", "Enrollment Date (dd/MM/yyyy)", 
                "Emergency Contact", "Emergency Phone", "Status (ACTIVE/INACTIVE/SUSPENDED)", "Avatar"
            };
            
            // Create header style
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setFontHeightInPoints((short) 12);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            
            // Add headers
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
                sheet.setColumnWidth(i, 20 * 256); // Set column width
            }
            
            // Add sample data row
            Row sampleRow = sheet.createRow(1);
            sampleRow.createCell(0).setCellValue("ST001");
            sampleRow.createCell(1).setCellValue("Nguyễn Văn A");
            sampleRow.createCell(2).setCellValue("10A1");
            sampleRow.createCell(3).setCellValue(10);
            sampleRow.createCell(4).setCellValue("15/03/2008");
            sampleRow.createCell(5).setCellValue("123 Đường ABC, Quận 1, TP.HCM");
            sampleRow.createCell(6).setCellValue("MALE");
            sampleRow.createCell(7).setCellValue("01/09/2023");
            sampleRow.createCell(8).setCellValue("Nguyễn Văn B");
            sampleRow.createCell(9).setCellValue("0901234567");
            sampleRow.createCell(10).setCellValue("ACTIVE");
            sampleRow.createCell(11).setCellValue("avatar1.jpg");
            
            // Add instruction row
            Row instructionRow = sheet.createRow(3);
            Cell instructionCell = instructionRow.createCell(0);
            instructionCell.setCellValue("HƯỚNG DẪN:");
            CellStyle instructionStyle = workbook.createCellStyle();
            Font instructionFont = workbook.createFont();
            instructionFont.setBold(true);
            instructionFont.setColor(IndexedColors.RED.getIndex());
            instructionStyle.setFont(instructionFont);
            instructionCell.setCellStyle(instructionStyle);
            
            Row instructionRow2 = sheet.createRow(4);
            Cell instructionCell2 = instructionRow2.createCell(0);
            instructionCell2.setCellValue("- Các cột có dấu * là bắt buộc");
            instructionCell2.setCellStyle(instructionStyle);
            
            Row instructionRow3 = sheet.createRow(5);
            Cell instructionCell3 = instructionRow3.createCell(0);
            instructionCell3.setCellValue("- Định dạng ngày tháng: dd/MM/yyyy (ví dụ: 15/03/2008)");
            instructionCell3.setCellStyle(instructionStyle);
            
            Row instructionRow4 = sheet.createRow(6);
            Cell instructionCell4 = instructionRow4.createCell(0);
            instructionCell4.setCellValue("- Gender: MALE, FEMALE, OTHER");
            instructionCell4.setCellStyle(instructionStyle);
            
            Row instructionRow5 = sheet.createRow(7);
            Cell instructionCell5 = instructionRow5.createCell(0);
            instructionCell5.setCellValue("- Status: ACTIVE, INACTIVE, SUSPENDED");
            instructionCell5.setCellStyle(instructionStyle);
            
            // Write to output stream
            workbook.write(outputStream);
            outputStream.flush();
            
            log.info("Excel template generated successfully");
            return outputStream.toByteArray();
            
        } catch (IOException e) {
            log.error("Error generating Excel template: {}", e.getMessage());
            throw new RuntimeException("Failed to generate Excel template", e);
        }
    }

    @Override
    public byte[] generateStudentListExcel(List<Student> students) {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Students");
            // Header
            Row headerRow = sheet.createRow(0);
            String[] headers = {
                "Student Code", "Full Name", "Class Code", "Grade", "Date of Birth", 
                "Address", "Gender", "Enrollment Date", "Emergency Contact", "Emergency Phone", "Status", "Avatar"
            };
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setFontHeightInPoints((short) 12);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
                sheet.setColumnWidth(i, 20 * 256);
            }
            // Data
            int rowIdx = 1;
            for (Student s : students) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(s.getStudentCode() != null ? s.getStudentCode() : "");
                row.createCell(1).setCellValue(s.getFullName() != null ? s.getFullName() : "");
                row.createCell(2).setCellValue(s.getClassCode() != null ? s.getClassCode() : "");
                row.createCell(3).setCellValue(s.getGrade() != null ? s.getGrade() : 0);
                row.createCell(4).setCellValue(s.getDateOfBirth() != null ? s.getDateOfBirth().toString() : "");
                row.createCell(5).setCellValue(s.getAddress() != null ? s.getAddress() : "");
                row.createCell(6).setCellValue(s.getGender() != null ? s.getGender().name() : "");
                row.createCell(7).setCellValue(s.getEnrollmentDate() != null ? s.getEnrollmentDate().toString() : "");
                row.createCell(8).setCellValue(s.getEmergencyContact() != null ? s.getEmergencyContact() : "");
                row.createCell(9).setCellValue(s.getEmergencyPhone() != null ? s.getEmergencyPhone() : "");
                row.createCell(10).setCellValue(s.getStatus() != null ? s.getStatus().name() : "");
                row.createCell(11).setCellValue(s.getAvatar() != null ? s.getAvatar() : "");
            }
            workbook.write(outputStream);
            outputStream.flush();
            return outputStream.toByteArray();
        } catch (IOException e) {
            log.error("Error generating Excel student list: {}", e.getMessage());
            throw new RuntimeException("Failed to generate Excel student list", e);
        }
    }
} 