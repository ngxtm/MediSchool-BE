package com.medischool.backend.service.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.medischool.backend.dto.UserImportDTO;
import com.medischool.backend.dto.UserImportResponseDTO;
import com.medischool.backend.dto.student.StudentImportDTO;
import com.medischool.backend.dto.student.StudentImportResponseDTO;
import com.medischool.backend.model.UserProfile;
import com.medischool.backend.model.enums.Gender;
import com.medischool.backend.model.enums.StudentStatus;
import com.medischool.backend.model.parentstudent.Student;
import com.medischool.backend.model.parentstudent.Parent;
import com.medischool.backend.model.parentstudent.ParentStudentLink;
import com.medischool.backend.repository.UserProfileRepository;
import com.medischool.backend.repository.StudentRepository;
import com.medischool.backend.repository.ParentRepository;
import com.medischool.backend.repository.ParentStudentLinkRepository;
import com.medischool.backend.service.ExcelImportService;
import com.medischool.backend.service.SupabaseAuthService;
import com.medischool.backend.model.enums.Relationship;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementation of ExcelImportService for user management
 * Handles import/export with Supabase auth integration
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ExcelImportServiceImpl implements ExcelImportService {

    private final UserProfileRepository userProfileRepository;
    private final SupabaseAuthService supabaseAuthService;
    private final StudentRepository studentRepository;
    private final ParentRepository parentRepository;
    private final ParentStudentLinkRepository parentStudentLinkRepository;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATE_FORMATTER_ISO = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATE_FORMATTER_2 = DateTimeFormatter.ofPattern("d/M/yyyy");
    private static final DateTimeFormatter DATE_FORMATTER_3 = DateTimeFormatter.ofPattern("MM/dd/yyyy");
    private static final DateTimeFormatter DATE_FORMATTER_4 = DateTimeFormatter.ofPattern("d-M-yyyy");

    private static final String[] EXPECTED_HEADERS = {
            "Full Name", "Email", "Phone", "Role", "Address", "Gender", "Date of Birth", "Password"
    };

    @Override
    public boolean validateExcelFormat(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return false;
        }

        String filename = file.getOriginalFilename();
        if (filename == null
                || (!filename.toLowerCase().endsWith(".xlsx") && !filename.toLowerCase().endsWith(".xls"))) {
            return false;
        }

        String contentType = file.getContentType();
        return contentType != null
                && (contentType.equals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet") ||
                        contentType.equals("application/vnd.ms-excel"));
    }

    @Override
    @Transactional
    public UserImportResponseDTO importUsersFromExcel(MultipartFile file) {
        log.info("Starting user import from Excel file: {}", file.getOriginalFilename());

        if (!validateExcelFormat(file)) {
            return createErrorResponse("Invalid file format. Please upload an Excel file (.xlsx or .xls)");
        }

        List<UserImportDTO> errors = new ArrayList<>();
        List<UserProfile> successfulUsers = new ArrayList<>();
        int totalRows = 0;

        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);

            if (!validateHeaders(sheet)) {
                return createErrorResponse("Invalid Excel headers. Please use the provided template.");
            }

            for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row == null || isEmptyRow(row)) {
                    continue;
                }

                totalRows++;
                log.debug("Processing row {}", rowIndex + 1);

                UserImportDTO userImport = parseUserRow(row, rowIndex + 1);

                if (userImport.isValid()) {
                    try {
                        UserProfile createdUser = createUserAccount(userImport);
                        successfulUsers.add(createdUser);
                        log.info("Successfully created user: {} ({})", createdUser.getFullName(),
                                createdUser.getEmail());
                    } catch (Exception e) {
                        log.error("Failed to create user at row {}: {}", rowIndex + 1, e.getMessage());
                        userImport.setValid(false);
                        userImport.setErrorMessage("Account creation failed: " + e.getMessage());
                        errors.add(userImport);
                    }
                } else {
                    errors.add(userImport);
                }
            }

            String message = String.format("Import completed. Success: %d, Errors: %d",
                    successfulUsers.size(), errors.size());

            return UserImportResponseDTO.builder()
                    .success(errors.isEmpty())
                    .totalRows(totalRows)
                    .successCount(successfulUsers.size())
                    .errorCount(errors.size())
                    .errors(errors)
                    .message(message)
                    .build();

        } catch (Exception e) {
            log.error("Error processing Excel file: {}", e.getMessage());
            return createErrorResponse("Error processing file: " + e.getMessage());
        }
    }

    @Override
    public byte[] generateUserImportTemplate() {
        log.info("Generating user import template");

        try (Workbook workbook = new XSSFWorkbook();
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Users");

            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle instructionStyle = createInstructionStyle(workbook);

            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < EXPECTED_HEADERS.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(EXPECTED_HEADERS[i] + (isRequiredField(EXPECTED_HEADERS[i]) ? "*" : ""));
                cell.setCellStyle(headerStyle);
                sheet.setColumnWidth(i, 20 * 256);
            }

            addSampleData(sheet);

            addInstructions(sheet, instructionStyle);

            workbook.write(outputStream);
            log.info("Template generated successfully");
            return outputStream.toByteArray();

        } catch (IOException e) {
            log.error("Error generating template: {}", e.getMessage());
            throw new RuntimeException("Failed to generate Excel template", e);
        }
    }

    @Override
    public byte[] exportUsersToExcel(List<UserProfile> users) {
        log.info("Exporting {} users to Excel", users.size());

        try (Workbook workbook = new XSSFWorkbook();
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Users");
            CellStyle headerStyle = createHeaderStyle(workbook);

            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < EXPECTED_HEADERS.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(EXPECTED_HEADERS[i]);
                cell.setCellStyle(headerStyle);
                sheet.setColumnWidth(i, 20 * 256);
            }

            int rowIndex = 1;
            for (UserProfile user : users) {
                Row row = sheet.createRow(rowIndex++);
                populateUserRow(row, user);
            }

            workbook.write(outputStream);
            log.info("Export completed successfully");
            return outputStream.toByteArray();

        } catch (IOException e) {
            log.error("Error exporting users: {}", e.getMessage());
            throw new RuntimeException("Failed to export users to Excel", e);
        }
    }

    private boolean validateHeaders(Sheet sheet) {
        Row headerRow = sheet.getRow(0);
        if (headerRow == null) {
            return false;
        }

        for (int i = 0; i < EXPECTED_HEADERS.length; i++) {
            Cell cell = headerRow.getCell(i);
            if (cell == null) {
                return false;
            }

            String headerValue = cell.getStringCellValue().trim().replace("*", "");
            if (!EXPECTED_HEADERS[i].equals(headerValue)) {
                log.warn("Header mismatch at column {}: expected '{}', found '{}'",
                        i, EXPECTED_HEADERS[i], headerValue);
                return false;
            }
        }
        return true;
    }

    private boolean isEmptyRow(Row row) {
        for (int i = 0; i < EXPECTED_HEADERS.length; i++) {
            Cell cell = row.getCell(i);
            if (cell != null && cell.getCellType() != org.apache.poi.ss.usermodel.CellType.BLANK) {
                String value = getCellValueAsString(cell);
                if (value != null && !value.trim().isEmpty()) {
                    return false;
                }
            }
        }
        return true;
    }

    private UserImportDTO parseUserRow(Row row, int rowNumber) {
        UserImportDTO userImport = UserImportDTO.builder()
                .rowNumber(rowNumber)
                .isValid(true)
                .build();

        try {
            userImport.setFullName(getCellValueAsString(row.getCell(0)));
            userImport.setEmail(getCellValueAsString(row.getCell(1)));
            userImport.setPhone(getCellValueAsString(row.getCell(2)));
            userImport.setRole(getCellValueAsString(row.getCell(3)));
            userImport.setAddress(getCellValueAsString(row.getCell(4)));
            userImport.setGender(getCellValueAsString(row.getCell(5)));
            userImport.setDateOfBirth(getCellValueAsString(row.getCell(6)));
            userImport.setPassword(getCellValueAsString(row.getCell(7)));

            validateUserImport(userImport);

        } catch (Exception e) {
            log.error("Error parsing row {}: {}", rowNumber, e.getMessage());
            userImport.setValid(false);
            userImport.setErrorMessage("Parsing error: " + e.getMessage());
        }

        return userImport;
    }

    private void validateUserImport(UserImportDTO userImport) {
        List<String> errors = new ArrayList<>();

        if (isBlank(userImport.getFullName())) {
            errors.add("Full Name is required");
        }
        if (isBlank(userImport.getEmail())) {
            errors.add("Email is required");
        }
        if (isBlank(userImport.getRole())) {
            errors.add("Role is required");
        }

        if (!isBlank(userImport.getEmail()) && !isValidEmail(userImport.getEmail())) {
            errors.add("Invalid email format");
        }

        if (!isBlank(userImport.getRole()) && !isValidRole(userImport.getRole())) {
            errors.add("Invalid role. Must be ADMIN, MANAGER, NURSE, or PARENT");
        }

        if (!isBlank(userImport.getGender()) && !isValidGender(userImport.getGender())) {
            errors.add("Invalid gender. Must be MALE, FEMALE, or OTHER");
        }

        if (!isBlank(userImport.getPassword()) && userImport.getPassword().length() < 6) {
            errors.add("Password must be at least 6 characters long");
        }

        if (!isBlank(userImport.getEmail()) && userProfileRepository.findByEmail(userImport.getEmail()).isPresent()) {
            errors.add("Email already exists in the system");
        }

        if (!errors.isEmpty()) {
            userImport.setValid(false);
            userImport.setErrorMessage(String.join("; ", errors));
        }
    }

    @Transactional
    private UserProfile createUserAccount(UserImportDTO userImport) throws Exception {
        log.info("Creating user account for: {}", userImport.getEmail());

        String password = userImport.getPassword();
        if (isBlank(password)) {
            password = generateTemporaryPassword();
            log.info("No password provided, generating temporary password for user: {}", userImport.getEmail());
        }

        Map<String, Object> userMetadata = new HashMap<>();
        userMetadata.put("full_name", userImport.getFullName());
        userMetadata.put("role", userImport.getRole().toUpperCase());

        UUID supabaseUserId;
        try {
            supabaseUserId = supabaseAuthService.createUserInSupabase(
                    userImport.getEmail(),
                    password,
                    userMetadata);
            log.info("✅ Successfully created user in Supabase auth.users: {} with ID: {}",
                    userImport.getEmail(), supabaseUserId);
        } catch (Exception e) {
            log.error("❌ Failed to create user in Supabase: {}", e.getMessage());
            throw new RuntimeException("Failed to create Supabase account: " + e.getMessage(), e);
        }

        UserProfile user = new UserProfile();
        user.setId(supabaseUserId);
        user.setFullName(userImport.getFullName());
        user.setEmail(userImport.getEmail());
        user.setPhone(userImport.getPhone());
        user.setRole(userImport.getRole().toUpperCase());
        user.setAddress(userImport.getAddress());

        if (!isBlank(userImport.getGender())) {
            user.setGender(Gender.valueOf(userImport.getGender().toUpperCase()));
        }

        if (!isBlank(userImport.getDateOfBirth())) {
            try {
                LocalDate dateOfBirth = parseDate(userImport.getDateOfBirth());
                user.setDateOfBirth(dateOfBirth);
            } catch (Exception e) {
                log.warn("Could not parse date of birth for user {}: {}", userImport.getEmail(), e.getMessage());
            }
        }

        user.setIsActive(true);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        try {
            UserProfile savedUser = userProfileRepository.save(user);
            log.info("✅ Successfully created user_profile: {} ({}) with ID: {}",
                    savedUser.getFullName(), savedUser.getEmail(), savedUser.getId());

            return savedUser;
        } catch (Exception e) {
            log.error("❌ Failed to create user_profile, will cleanup Supabase user: {}", e.getMessage());

            try {
                supabaseAuthService.deleteUserFromSupabase(supabaseUserId);
                log.info("✅ Cleaned up Supabase user after profile creation failure");
            } catch (Exception cleanupError) {
                log.warn("⚠️ Failed to cleanup Supabase user after profile creation failure: {}",
                        cleanupError.getMessage());
            }

            throw new RuntimeException("Failed to create user profile: " + e.getMessage(), e);
        }
    }

    @Transactional
    private UserProfile createUserAccount(UserImportDTO userImport, LocalDate dobRaw) throws Exception {
        log.info("Creating user account for: {}", userImport.getEmail());

        String password = userImport.getPassword();
        if (isBlank(password)) {
            password = generateTemporaryPassword();
            log.info("No password provided, generating temporary password for user: {}", userImport.getEmail());
        }

        Map<String, Object> userMetadata = new HashMap<>();
        userMetadata.put("full_name", userImport.getFullName());
        userMetadata.put("role", userImport.getRole().toUpperCase());

        UUID supabaseUserId;
        try {
            supabaseUserId = supabaseAuthService.createUserInSupabase(
                    userImport.getEmail(),
                    password,
                    userMetadata);
            log.info("✅ Successfully created user in Supabase auth.users: {} with ID: {}",
                    userImport.getEmail(), supabaseUserId);
        } catch (Exception e) {
            log.error("❌ Failed to create user in Supabase: {}", e.getMessage());
            throw new RuntimeException("Failed to create Supabase account: " + e.getMessage(), e);
        }

        UserProfile user = new UserProfile();
        user.setId(supabaseUserId);
        user.setFullName(userImport.getFullName());
        user.setEmail(userImport.getEmail());
        user.setPhone(userImport.getPhone());
        user.setRole(userImport.getRole().toUpperCase());
        user.setAddress(userImport.getAddress());

        if (!isBlank(userImport.getGender())) {
            user.setGender(Gender.valueOf(userImport.getGender().toUpperCase()));
        }

        if (dobRaw != null) {
            user.setDateOfBirth(dobRaw);
        } else if (!isBlank(userImport.getDateOfBirth())) {
            try {
                LocalDate dateOfBirth = parseDate(userImport.getDateOfBirth());
                user.setDateOfBirth(dateOfBirth);
            } catch (Exception e) {
                log.warn("Could not parse date of birth for user {}: {}", userImport.getEmail(), e.getMessage());
            }
        }

        user.setIsActive(true);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        try {
            UserProfile savedUser = userProfileRepository.save(user);
            log.info("✅ Successfully created user_profile: {} ({}) with ID: {}",
                    savedUser.getFullName(), savedUser.getEmail(), savedUser.getId());

            return savedUser;
        } catch (Exception e) {
            log.error("❌ Failed to create user_profile, will cleanup Supabase user: {}", e.getMessage());

            try {
                supabaseAuthService.deleteUserFromSupabase(supabaseUserId);
                log.info("✅ Cleaned up Supabase user after profile creation failure");
            } catch (Exception cleanupError) {
                log.warn("⚠️ Failed to cleanup Supabase user after profile creation failure: {}",
                        cleanupError.getMessage());
            }

            throw new RuntimeException("Failed to create user profile: " + e.getMessage(), e);
        }
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null)
            return null;

        switch (cell.getCellType()) {
            case STRING -> {
                return cell.getStringCellValue().trim();
            }
            case NUMERIC -> {
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getLocalDateTimeCellValue().toLocalDate().format(DATE_FORMATTER);
                } else {
                    double numValue = cell.getNumericCellValue();
                    if (numValue == Math.floor(numValue)) {
                        return String.valueOf((long) numValue);
                    } else {
                        return String.valueOf(numValue);
                    }
                }
            }
            case BOOLEAN -> {
                return String.valueOf(cell.getBooleanCellValue());
            }
            case BLANK -> {
                return null;
            }
            default -> {
                return null;
            }
        }
    }

    private LocalDate parseDate(String dateStr) {
        try {
            return LocalDate.parse(dateStr, DATE_FORMATTER);
        } catch (DateTimeParseException e1) {
            try {
                return LocalDate.parse(dateStr, DATE_FORMATTER_ISO);
            } catch (DateTimeParseException e2) {
                throw new IllegalArgumentException("Invalid date format: " + dateStr);
            }
        }
    }

    private boolean isBlank(String str) {
        return str == null || str.trim().isEmpty();
    }

    private boolean isValidEmail(String email) {
        return email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }

    private boolean isValidRole(String role) {
        try {
            String upperRole = role.toUpperCase();
            return "ADMIN".equals(upperRole) || "MANAGER".equals(upperRole) ||
                    "NURSE".equals(upperRole) || "PARENT".equals(upperRole);
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isValidGender(String gender) {
        try {
            Gender.valueOf(gender.toUpperCase());
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private boolean isRequiredField(String header) {
        return "Full Name".equals(header) || "Email".equals(header) || "Role".equals(header);
    }

    private String generateTemporaryPassword() {
        return "TempPass" + System.currentTimeMillis();
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
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
        return headerStyle;
    }

    private CellStyle createInstructionStyle(Workbook workbook) {
        CellStyle instructionStyle = workbook.createCellStyle();
        Font instructionFont = workbook.createFont();
        instructionFont.setBold(true);
        instructionFont.setColor(IndexedColors.RED.getIndex());
        instructionStyle.setFont(instructionFont);
        return instructionStyle;
    }

    private void addSampleData(Sheet sheet) {
        Row sampleRow = sheet.createRow(1);
        sampleRow.createCell(0).setCellValue("Nguyễn Văn A");
        sampleRow.createCell(1).setCellValue("admin@medischool.com");
        sampleRow.createCell(2).setCellValue("0901234567");
        sampleRow.createCell(3).setCellValue("ADMIN");
        sampleRow.createCell(4).setCellValue("123 Đường ABC, Quận 1, TP.HCM");
        sampleRow.createCell(5).setCellValue("MALE");
        sampleRow.createCell(6).setCellValue("15/03/1990");
        sampleRow.createCell(7).setCellValue("password123");
    }

    private void addInstructions(Sheet sheet, CellStyle instructionStyle) {
        String[] instructions = {
                "HƯỚNG DẪN:",
                "- Các cột có dấu * là bắt buộc",
                "- Định dạng ngày tháng: dd/MM/yyyy (ví dụ: 15/03/1990)",
                "- Role: ADMIN, MANAGER, NURSE, PARENT",
                "- Gender: MALE, FEMALE, OTHER",
                "- Email phải unique trong hệ thống",
                "- Password: Để trống để tự động tạo mật khẩu tạm thời"
        };

        for (int i = 0; i < instructions.length; i++) {
            Row instructionRow = sheet.createRow(3 + i);
            Cell instructionCell = instructionRow.createCell(0);
            instructionCell.setCellValue(instructions[i]);
            instructionCell.setCellStyle(instructionStyle);
        }
    }

    private void populateUserRow(Row row, UserProfile user) {
        row.createCell(0).setCellValue(user.getFullName() != null ? user.getFullName() : "");
        row.createCell(1).setCellValue(user.getEmail() != null ? user.getEmail() : "");
        row.createCell(2).setCellValue(user.getPhone() != null ? user.getPhone() : "");
        row.createCell(3).setCellValue(user.getRole() != null ? user.getRole() : "");
        row.createCell(4).setCellValue(user.getAddress() != null ? user.getAddress() : "");
        row.createCell(5).setCellValue(user.getGender() != null ? user.getGender().name() : "");
        row.createCell(6)
                .setCellValue(user.getDateOfBirth() != null ? user.getDateOfBirth().format(DATE_FORMATTER) : "");
        row.createCell(7).setCellValue(""); // Password không được lưu trong database
    }

    private UserImportResponseDTO createErrorResponse(String message) {
        return UserImportResponseDTO.builder()
                .success(false)
                .totalRows(0)
                .successCount(0)
                .errorCount(0)
                .errors(new ArrayList<>())
                .message(message)
                .build();
    }

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
                        student = studentRepository.save(student);
                        // Xử lý parent (bố)
                        handleParentImport(student, studentImport, true);
                        // Xử lý parent (mẹ)
                        handleParentImport(student, studentImport, false);
                        successCount++;
                    } catch (Exception e) {
                        studentImport.setValid(false);
                        studentImport.setErrorMessage("Database error: " + e.getMessage());
                        errors.add(studentImport);
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
            studentImport.setStudentCode(getStringCellValue(row.getCell(0), "Student Code"));
            studentImport.setFullName(getStringCellValue(row.getCell(1), "Full Name"));
            studentImport.setClassCode(getStringCellValue(row.getCell(2), "Class Code"));
            // Bỏ qua grade
            studentImport.setDateOfBirth(getDateCellValue(row.getCell(3), "Date of Birth"));
            studentImport.setAddress(getStringCellValue(row.getCell(4), "Address"));
            studentImport.setGender(getStringCellValue(row.getCell(5), "Gender"));
            studentImport.setEnrollmentDate(getDateCellValue(row.getCell(6), "Enrollment Date"));
            studentImport.setEmergencyContact(getStringCellValue(row.getCell(7), "Emergency Contact"));
            studentImport.setEmergencyPhone(getStringCellValue(row.getCell(8), "Emergency Phone"));
            studentImport.setStatus(getStringCellValue(row.getCell(9), "Status"));
            studentImport.setAvatar(getStringCellValue(row.getCell(10), "Avatar"));
            // Father columns (11-18)
            studentImport.setFatherName(getStringCellValue(row.getCell(11), "Father Name"));
            studentImport.setFatherEmail(getStringCellValue(row.getCell(12), "Father Email"));
            studentImport.setFatherPhone(getStringCellValue(row.getCell(13), "Father Phone"));
            studentImport.setFatherAddress(getStringCellValue(row.getCell(14), "Father Address"));
            studentImport.setFatherDateOfBirth(getDateCellValue(row.getCell(15), "Father DateOfBirth"));
            studentImport.setFatherGender(getStringCellValue(row.getCell(16), "Father Gender"));
            studentImport.setFatherJob(getStringCellValue(row.getCell(17), "Father Job"));
            studentImport.setFatherJobPlace(getStringCellValue(row.getCell(18), "Father JobPlace"));
            // Mother columns (19-26)
            studentImport.setMotherName(getStringCellValue(row.getCell(19), "Mother Name"));
            studentImport.setMotherEmail(getStringCellValue(row.getCell(20), "Mother Email"));
            studentImport.setMotherPhone(getStringCellValue(row.getCell(21), "Mother Phone"));
            studentImport.setMotherAddress(getStringCellValue(row.getCell(22), "Mother Address"));
            studentImport.setMotherDateOfBirth(getDateCellValue(row.getCell(23), "Mother DateOfBirth"));
            studentImport.setMotherGender(getStringCellValue(row.getCell(24), "Mother Gender"));
            studentImport.setMotherJob(getStringCellValue(row.getCell(25), "Mother Job"));
            studentImport.setMotherJobPlace(getStringCellValue(row.getCell(26), "Mother JobPlace"));
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
                double value = cell.getNumericCellValue();
                if (value == Math.floor(value)) {
                    return String.valueOf((long) value);
                } else {
                    return String.valueOf(value);
                }
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
                        return LocalDate.parse(value, DATE_FORMATTER_ISO);
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
        if (studentImport.getStudentCode() == null || studentImport.getStudentCode().isEmpty()) {
            errors.add("Student Code is required");
        }
        if (studentImport.getFullName() == null || studentImport.getFullName().isEmpty()) {
            errors.add("Full Name is required");
        }
        if (studentImport.getClassCode() == null || studentImport.getClassCode().isEmpty()) {
            errors.add("Class Code is required");
        }
        if (studentImport.getGender() != null && !studentImport.getGender().isEmpty()) {
            try {
                Gender.valueOf(studentImport.getGender().toUpperCase());
            } catch (IllegalArgumentException e) {
                errors.add("Invalid gender value. Must be MALE, FEMALE, or OTHER");
            }
        }
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
            student.setStatus(StudentStatus.ACTIVE);
        }
        student.setAvatar(studentImport.getAvatar());
        return student;
    }

    @Override
    public byte[] generateStudentListExcel(List<Student> students) {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Students");
            // Header
            Row headerRow = sheet.createRow(0);
            String[] headers = {
                "Student Code", "Full Name", "Class Code", "Date of Birth", 
                "Address", "Gender", "Enrollment Date", "Emergency Contact", "Emergency Phone", "Status", "Avatar",
                // Father
                "Father Name", "Father Email", "Father Phone", "Father Address", "Father DateOfBirth", "Father Gender", "Father Job", "Father JobPlace",
                // Mother
                "Mother Name", "Mother Email", "Mother Phone", "Mother Address", "Mother DateOfBirth", "Mother Gender", "Mother Job", "Mother JobPlace"
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
                row.createCell(3).setCellValue(s.getDateOfBirth() != null ? s.getDateOfBirth().toString() : "");
                row.createCell(4).setCellValue(s.getAddress() != null ? s.getAddress() : "");
                row.createCell(5).setCellValue(s.getGender() != null ? s.getGender().name() : "");
                row.createCell(6).setCellValue(s.getEnrollmentDate() != null ? s.getEnrollmentDate().toString() : "");
                row.createCell(7).setCellValue(s.getEmergencyContact() != null ? s.getEmergencyContact() : "");
                row.createCell(8).setCellValue(s.getEmergencyPhone() != null ? s.getEmergencyPhone() : "");
                row.createCell(9).setCellValue(s.getStatus() != null ? s.getStatus().name() : "");
                row.createCell(10).setCellValue(s.getAvatar() != null ? s.getAvatar() : "");
                // Lấy thông tin bố mẹ
                List<ParentStudentLink> links = parentStudentLinkRepository.findByStudentId(s.getStudentId());
                UserProfile fatherProfile = null, motherProfile = null;
                Parent fatherParent = null, motherParent = null;
                for (ParentStudentLink link : links) {
                    Optional<UserProfile> up = userProfileRepository.findById(link.getParentId());
                    Optional<Parent> p = parentRepository.findByParentId(link.getParentId());
                    if (link.getRelationship() != null && link.getRelationship() == Relationship.FATHER) {
                        fatherProfile = up.orElse(null);
                        fatherParent = p.orElse(null);
                    }
                    if (link.getRelationship() != null && link.getRelationship() == Relationship.MOTHER) {
                        motherProfile = up.orElse(null);
                        motherParent = p.orElse(null);
                    }
                }
                // Father columns
                int col = 11;
                row.createCell(col++).setCellValue(fatherProfile != null ? fatherProfile.getFullName() : "");
                row.createCell(col++).setCellValue(fatherProfile != null ? fatherProfile.getEmail() : "");
                row.createCell(col++).setCellValue(fatherProfile != null ? fatherProfile.getPhone() : "");
                row.createCell(col++).setCellValue(fatherProfile != null ? fatherProfile.getAddress() : "");
                row.createCell(col++).setCellValue(fatherProfile != null && fatherProfile.getDateOfBirth() != null ? fatherProfile.getDateOfBirth().toString() : "");
                row.createCell(col++).setCellValue(fatherProfile != null && fatherProfile.getGender() != null ? fatherProfile.getGender().name() : "");
                row.createCell(col++).setCellValue(fatherParent != null ? fatherParent.getJob() : "");
                row.createCell(col++).setCellValue(fatherParent != null ? fatherParent.getJobPlace() : "");
                // Mother columns
                row.createCell(col++).setCellValue(motherProfile != null ? motherProfile.getFullName() : "");
                row.createCell(col++).setCellValue(motherProfile != null ? motherProfile.getEmail() : "");
                row.createCell(col++).setCellValue(motherProfile != null ? motherProfile.getPhone() : "");
                row.createCell(col++).setCellValue(motherProfile != null ? motherProfile.getAddress() : "");
                row.createCell(col++).setCellValue(motherProfile != null && motherProfile.getDateOfBirth() != null ? motherProfile.getDateOfBirth().toString() : "");
                row.createCell(col++).setCellValue(motherProfile != null && motherProfile.getGender() != null ? motherProfile.getGender().name() : "");
                row.createCell(col++).setCellValue(motherParent != null ? motherParent.getJob() : "");
                row.createCell(col++).setCellValue(motherParent != null ? motherParent.getJobPlace() : "");
            }
            workbook.write(outputStream);
            outputStream.flush();
            return outputStream.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Failed to generate Excel student list", e);
        }
    }

    private void handleParentImport(Student student, StudentImportDTO dto, boolean isFather) {
        String name = isFather ? dto.getFatherName() : dto.getMotherName();
        String email = isFather ? dto.getFatherEmail() : dto.getMotherEmail();
        String phone = isFather ? dto.getFatherPhone() : dto.getMotherPhone();
        String address = isFather ? dto.getFatherAddress() : dto.getMotherAddress();
        LocalDate dob = isFather ? dto.getFatherDateOfBirth() : dto.getMotherDateOfBirth();
        String gender = isFather ? dto.getFatherGender() : dto.getMotherGender();
        String job = isFather ? dto.getFatherJob() : dto.getMotherJob();
        String jobPlace = isFather ? dto.getFatherJobPlace() : dto.getMotherJobPlace();
        String relationship = isFather ? "FATHER" : "MOTHER";
        if (email == null || email.isBlank()) {
            return;
        }
        // Tìm hoặc tạo UserProfile (ưu tiên theo email)
        UserProfile parentProfile = userProfileRepository.findByEmail(email).orElse(null);
        boolean isNewUser = false;
        if (parentProfile == null) {
            // Tạo user trên Supabase và UserProfile
            UserImportDTO userImport = UserImportDTO.builder()
                    .fullName(name)
                    .email(email)
                    .phone(phone)
                    .role("PARENT")
                    .address(address)
                    .gender(gender)
                    .dateOfBirth(null) // không cần truyền chuỗi, sẽ set trực tiếp LocalDate
                    .password(null) // sẽ tự sinh mật khẩu tạm thời
                    .isValid(true)
                    .build();
            try {
                parentProfile = createUserAccount(userImport, dob);
                isNewUser = true;
            } catch (Exception e) {
                log.error("Failed to create parent user for email {}: {}", email, e.getMessage());
                return;
            }
        } else {
            // Cập nhật các trường thông tin nếu có dữ liệu mới từ file Excel
            boolean changed = false;
            if (name != null && !name.isBlank() && !name.equals(parentProfile.getFullName())) {
                parentProfile.setFullName(name);
                changed = true;
            }
            if (phone != null && !phone.isBlank() && !phone.equals(parentProfile.getPhone())) {
                parentProfile.setPhone(phone);
                changed = true;
            }
            if (address != null && !address.isBlank() && !address.equals(parentProfile.getAddress())) {
                parentProfile.setAddress(address);
                changed = true;
            }
            if (dob != null && (parentProfile.getDateOfBirth() == null || !dob.equals(parentProfile.getDateOfBirth()))) {
                parentProfile.setDateOfBirth(dob);
                changed = true;
            }
            if (gender != null && !gender.isBlank()) {
                try {
                    Gender g = Gender.valueOf(gender.toUpperCase());
                    if (parentProfile.getGender() == null || !g.equals(parentProfile.getGender())) {
                        parentProfile.setGender(g);
                        changed = true;
                    }
                } catch (Exception ignored) {}
            }
            if (changed) {
                userProfileRepository.save(parentProfile);
            }
        }
        // Tìm hoặc tạo Parent
        Parent parent = parentRepository.findByParentId(parentProfile.getId()).orElse(null);
        if (parent == null) {
            parent = new Parent();
            parent.setParentId(parentProfile.getId());
        }
        boolean parentChanged = false;
        if (job != null && !job.equals(parent.getJob())) {
            parent.setJob(job);
            parentChanged = true;
        }
        if (jobPlace != null && !jobPlace.equals(parent.getJobPlace())) {
            parent.setJobPlace(jobPlace);
            parentChanged = true;
        }
        if (parentChanged || parent.getParentId() == null) {
            parentRepository.save(parent);
        }
        // Tạo ParentStudentLink nếu chưa có
        if (!parentStudentLinkRepository.existsByParentIdAndStudentId(parentProfile.getId(), student.getStudentId())) {
            ParentStudentLink link = new ParentStudentLink();
            link.setParentId(parentProfile.getId());
            link.setStudentId(student.getStudentId());
            link.setRelationship(isFather ? Relationship.FATHER : Relationship.MOTHER);
            parentStudentLinkRepository.save(link);
        }
    }
}