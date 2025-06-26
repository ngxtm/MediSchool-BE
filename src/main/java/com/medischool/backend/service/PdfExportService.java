package com.medischool.backend.service;

import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.medischool.backend.dto.VaccinationHistoryWithStudentDTO;
import com.medischool.backend.model.vaccine.VaccinationHistory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class PdfExportService {

    public byte[] exportVaccinationHistoryByEvent(Long eventId, List<VaccinationHistoryWithStudentDTO> histories) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            // Use default font instead of Vietnamese font to avoid issues
            // PdfFont font = PdfFontFactory.createFont("STSong-Light", "UniGB-UCS2-H");
            // document.setFont(font);

            // Add title
            Paragraph title = new Paragraph("VACCINATION HISTORY REPORT - EVENT " + eventId)
                    .setFontSize(18)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setBold();
            document.add(title);

            // Add event info
            Paragraph eventInfo = new Paragraph("Event ID: " + eventId)
                    .setFontSize(12)
                    .setTextAlignment(TextAlignment.CENTER);
            document.add(eventInfo);

            // Add table with all fields
            Table table = new Table(UnitValue.createPercentArray(new float[]{8, 12, 15, 12, 8, 10, 15, 10, 10}))
                    .useAllAvailableWidth();

            // Add headers
            table.addHeaderCell(new Cell().add(new Paragraph("History ID")).setBold());
            table.addHeaderCell(new Cell().add(new Paragraph("Student ID")).setBold());
            table.addHeaderCell(new Cell().add(new Paragraph("Student Name")).setBold());
            table.addHeaderCell(new Cell().add(new Paragraph("Vaccine")).setBold());
            table.addHeaderCell(new Cell().add(new Paragraph("Dose")).setBold());
            table.addHeaderCell(new Cell().add(new Paragraph("Date")).setBold());
            table.addHeaderCell(new Cell().add(new Paragraph("Location")).setBold());
            table.addHeaderCell(new Cell().add(new Paragraph("Abnormal")).setBold());
            table.addHeaderCell(new Cell().add(new Paragraph("Created")).setBold());

            // Add data
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            
            for (VaccinationHistoryWithStudentDTO dto : histories) {
                VaccinationHistory history = dto.getHistory();
                table.addCell(new Cell().add(new Paragraph(history.getHistoryId().toString())));
                table.addCell(new Cell().add(new Paragraph(history.getStudentId().toString())));
                table.addCell(new Cell().add(new Paragraph(dto.getStudent() != null ? 
                    sanitizeText(dto.getStudent().getFullName()) : "N/A")));
                table.addCell(new Cell().add(new Paragraph(history.getVaccine().getName())));
                table.addCell(new Cell().add(new Paragraph(history.getDoseNumber().toString())));
                table.addCell(new Cell().add(new Paragraph(history.getVaccinationDate().format(dateFormatter))));
                table.addCell(new Cell().add(new Paragraph(history.getLocation() != null ? 
                    sanitizeText(history.getLocation()) : "N/A")));
                table.addCell(new Cell().add(new Paragraph(history.getAbnormal() != null && history.getAbnormal() ? "Yes" : "No")));
                table.addCell(new Cell().add(new Paragraph(history.getCreatedAt() != null ? 
                    history.getCreatedAt().format(dateTimeFormatter) : "N/A")));
            }

            document.add(table);

            // Add detailed information section
            document.add(new Paragraph("DETAILED INFORMATION").setFontSize(14).setBold());
            
            for (VaccinationHistoryWithStudentDTO dto : histories) {
                VaccinationHistory history = dto.getHistory();
                
                Paragraph recordTitle = new Paragraph("Record ID: " + history.getHistoryId())
                        .setFontSize(12)
                        .setBold();
                document.add(recordTitle);
                
                // Create detailed table for each record
                Table detailTable = new Table(UnitValue.createPercentArray(new float[]{30, 70}))
                        .useAllAvailableWidth();
                
                detailTable.addCell(new Cell().add(new Paragraph("Student ID")).setBold());
                detailTable.addCell(new Cell().add(new Paragraph(history.getStudentId().toString())));
                
                detailTable.addCell(new Cell().add(new Paragraph("Student Name")).setBold());
                detailTable.addCell(new Cell().add(new Paragraph(dto.getStudent() != null ? 
                    sanitizeText(dto.getStudent().getFullName()) : "N/A")));
                
                detailTable.addCell(new Cell().add(new Paragraph("Event ID")).setBold());
                detailTable.addCell(new Cell().add(new Paragraph(history.getEventId().toString())));
                
                detailTable.addCell(new Cell().add(new Paragraph("Vaccine")).setBold());
                detailTable.addCell(new Cell().add(new Paragraph(history.getVaccine().getName())));
                
                detailTable.addCell(new Cell().add(new Paragraph("Dose Number")).setBold());
                detailTable.addCell(new Cell().add(new Paragraph(history.getDoseNumber().toString())));
                
                detailTable.addCell(new Cell().add(new Paragraph("Vaccination Date")).setBold());
                detailTable.addCell(new Cell().add(new Paragraph(history.getVaccinationDate().format(dateFormatter))));
                
                detailTable.addCell(new Cell().add(new Paragraph("Location")).setBold());
                detailTable.addCell(new Cell().add(new Paragraph(history.getLocation() != null ? 
                    sanitizeText(history.getLocation()) : "N/A")));
                
                detailTable.addCell(new Cell().add(new Paragraph("Note")).setBold());
                detailTable.addCell(new Cell().add(new Paragraph(history.getNote() != null ? 
                    sanitizeText(history.getNote()) : "N/A")));
                
                detailTable.addCell(new Cell().add(new Paragraph("Abnormal")).setBold());
                detailTable.addCell(new Cell().add(new Paragraph(history.getAbnormal() != null && history.getAbnormal() ? "Yes" : "No")));
                
                detailTable.addCell(new Cell().add(new Paragraph("Follow-up Note")).setBold());
                detailTable.addCell(new Cell().add(new Paragraph(history.getFollowUpNote() != null ? 
                    sanitizeText(history.getFollowUpNote()) : "N/A")));
                
                detailTable.addCell(new Cell().add(new Paragraph("Created By")).setBold());
                detailTable.addCell(new Cell().add(new Paragraph(history.getCreatedBy() != null ? 
                    history.getCreatedBy().toString() : "N/A")));
                
                detailTable.addCell(new Cell().add(new Paragraph("Created At")).setBold());
                detailTable.addCell(new Cell().add(new Paragraph(history.getCreatedAt() != null ? 
                    history.getCreatedAt().format(dateTimeFormatter) : "N/A")));
                
                document.add(detailTable);
                document.add(new Paragraph("")); // Add space between records
            }

            // Add summary
            Paragraph summary = new Paragraph("Total students vaccinated: " + histories.size())
                    .setFontSize(12)
                    .setTextAlignment(TextAlignment.RIGHT);
            document.add(summary);

            document.close();
            return baos.toByteArray();

        } catch (Exception e) {
            log.error("Error generating PDF for event {}: {}", eventId, e.getMessage(), e);
            throw new RuntimeException("Failed to generate PDF", e);
        }
    }

    public byte[] exportStudentVaccinationHistory(Integer studentId, Map<String, List<VaccinationHistory>> historyByCategory) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            // Use default font instead of Vietnamese font to avoid issues
            // PdfFont font = PdfFontFactory.createFont("STSong-Light", "UniGB-UCS2-H");
            // document.setFont(font);

            // Add title
            Paragraph title = new Paragraph("STUDENT VACCINATION HISTORY")
                    .setFontSize(18)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setBold();
            document.add(title);

            // Add student info
            Paragraph studentInfo = new Paragraph("Student ID: " + studentId)
                    .setFontSize(12)
                    .setTextAlignment(TextAlignment.CENTER);
            document.add(studentInfo);

            // Add content for each category
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            
            for (Map.Entry<String, List<VaccinationHistory>> entry : historyByCategory.entrySet()) {
                String categoryName = entry.getKey();
                List<VaccinationHistory> histories = entry.getValue();

                // Add category title
                Paragraph categoryTitle = new Paragraph("Category: " + sanitizeText(categoryName))
                        .setFontSize(14)
                        .setBold();
                document.add(categoryTitle);

                // Add summary table for this category
                Table summaryTable = new Table(UnitValue.createPercentArray(new float[]{10, 20, 10, 10, 15, 15, 10, 10}))
                        .useAllAvailableWidth();

                // Add headers
                summaryTable.addHeaderCell(new Cell().add(new Paragraph("History ID")).setBold());
                summaryTable.addHeaderCell(new Cell().add(new Paragraph("Vaccine")).setBold());
                summaryTable.addHeaderCell(new Cell().add(new Paragraph("Dose")).setBold());
                summaryTable.addHeaderCell(new Cell().add(new Paragraph("Date")).setBold());
                summaryTable.addHeaderCell(new Cell().add(new Paragraph("Location")).setBold());
                summaryTable.addHeaderCell(new Cell().add(new Paragraph("Abnormal")).setBold());
                summaryTable.addHeaderCell(new Cell().add(new Paragraph("Event ID")).setBold());
                summaryTable.addHeaderCell(new Cell().add(new Paragraph("Created")).setBold());

                // Add data
                for (VaccinationHistory history : histories) {
                    summaryTable.addCell(new Cell().add(new Paragraph(history.getHistoryId().toString())));
                    summaryTable.addCell(new Cell().add(new Paragraph(history.getVaccine().getName())));
                    summaryTable.addCell(new Cell().add(new Paragraph(history.getDoseNumber().toString())));
                    summaryTable.addCell(new Cell().add(new Paragraph(history.getVaccinationDate().format(dateFormatter))));
                    summaryTable.addCell(new Cell().add(new Paragraph(history.getLocation() != null ? 
                        sanitizeText(history.getLocation()) : "N/A")));
                    summaryTable.addCell(new Cell().add(new Paragraph(history.getAbnormal() != null && history.getAbnormal() ? "Yes" : "No")));
                    summaryTable.addCell(new Cell().add(new Paragraph(history.getEventId().toString())));
                    summaryTable.addCell(new Cell().add(new Paragraph(history.getCreatedAt() != null ? 
                        history.getCreatedAt().format(dateTimeFormatter) : "N/A")));
                }

                document.add(summaryTable);
                
                // Add detailed information for each record in this category
                document.add(new Paragraph("DETAILED RECORDS").setFontSize(12).setBold());
                
                for (VaccinationHistory history : histories) {
                    Paragraph recordTitle = new Paragraph("Record ID: " + history.getHistoryId())
                            .setFontSize(11)
                            .setBold();
                    document.add(recordTitle);
                    
                    // Create detailed table for each record
                    Table detailTable = new Table(UnitValue.createPercentArray(new float[]{30, 70}))
                            .useAllAvailableWidth();
                    
                    detailTable.addCell(new Cell().add(new Paragraph("History ID")).setBold());
                    detailTable.addCell(new Cell().add(new Paragraph(history.getHistoryId().toString())));
                    
                    detailTable.addCell(new Cell().add(new Paragraph("Student ID")).setBold());
                    detailTable.addCell(new Cell().add(new Paragraph(history.getStudentId().toString())));
                    
                    detailTable.addCell(new Cell().add(new Paragraph("Event ID")).setBold());
                    detailTable.addCell(new Cell().add(new Paragraph(history.getEventId().toString())));
                    
                    detailTable.addCell(new Cell().add(new Paragraph("Vaccine")).setBold());
                    detailTable.addCell(new Cell().add(new Paragraph(history.getVaccine().getName())));
                    
                    detailTable.addCell(new Cell().add(new Paragraph("Dose Number")).setBold());
                    detailTable.addCell(new Cell().add(new Paragraph(history.getDoseNumber().toString())));
                    
                    detailTable.addCell(new Cell().add(new Paragraph("Vaccination Date")).setBold());
                    detailTable.addCell(new Cell().add(new Paragraph(history.getVaccinationDate().format(dateFormatter))));
                    
                    detailTable.addCell(new Cell().add(new Paragraph("Location")).setBold());
                    detailTable.addCell(new Cell().add(new Paragraph(history.getLocation() != null ? 
                        sanitizeText(history.getLocation()) : "N/A")));
                    
                    detailTable.addCell(new Cell().add(new Paragraph("Note")).setBold());
                    detailTable.addCell(new Cell().add(new Paragraph(history.getNote() != null ? 
                        sanitizeText(history.getNote()) : "N/A")));
                    
                    detailTable.addCell(new Cell().add(new Paragraph("Abnormal")).setBold());
                    detailTable.addCell(new Cell().add(new Paragraph(history.getAbnormal() != null && history.getAbnormal() ? "Yes" : "No")));
                    
                    detailTable.addCell(new Cell().add(new Paragraph("Follow-up Note")).setBold());
                    detailTable.addCell(new Cell().add(new Paragraph(history.getFollowUpNote() != null ? 
                        sanitizeText(history.getFollowUpNote()) : "N/A")));
                    
                    detailTable.addCell(new Cell().add(new Paragraph("Created By")).setBold());
                    detailTable.addCell(new Cell().add(new Paragraph(history.getCreatedBy() != null ? 
                        history.getCreatedBy().toString() : "N/A")));
                    
                    detailTable.addCell(new Cell().add(new Paragraph("Created At")).setBold());
                    detailTable.addCell(new Cell().add(new Paragraph(history.getCreatedAt() != null ? 
                        history.getCreatedAt().format(dateTimeFormatter) : "N/A")));
                    
                    document.add(detailTable);
                    document.add(new Paragraph("")); // Add space between records
                }
                
                document.add(new Paragraph("")); // Add space between categories
            }

            // Add summary
            int totalVaccinations = historyByCategory.values().stream()
                    .mapToInt(List::size)
                    .sum();
            Paragraph summary = new Paragraph("Total vaccinations: " + totalVaccinations)
                    .setFontSize(12)
                    .setTextAlignment(TextAlignment.RIGHT);
            document.add(summary);

            document.close();
            return baos.toByteArray();

        } catch (Exception e) {
            log.error("Error generating PDF for student {}: {}", studentId, e.getMessage(), e);
            throw new RuntimeException("Failed to generate PDF", e);
        }
    }

    /**
     * Sanitize text to remove or replace problematic characters
     */
    private String sanitizeText(String text) {
        if (text == null) {
            return "N/A";
        }
        
        // Replace Vietnamese characters with ASCII equivalents or remove them
        return text.replaceAll("[àáạảãâầấậẩẫăằắặẳẵ]", "a")
                   .replaceAll("[èéẹẻẽêềếệểễ]", "e")
                   .replaceAll("[ìíịỉĩ]", "i")
                   .replaceAll("[òóọỏõôồốộổỗơờớợởỡ]", "o")
                   .replaceAll("[ùúụủũưừứựửữ]", "u")
                   .replaceAll("[ỳýỵỷỹ]", "y")
                   .replaceAll("[đ]", "d")
                   .replaceAll("[ÀÁẠẢÃÂẦẤẬẨẪĂẰẮẶẲẴ]", "A")
                   .replaceAll("[ÈÉẸẺẼÊỀẾỆỂỄ]", "E")
                   .replaceAll("[ÌÍỊỈĨ]", "I")
                   .replaceAll("[ÒÓỌỎÕÔỒỐỘỔỖƠỜỚỢỞỠ]", "O")
                   .replaceAll("[ÙÚỤỦŨƯỪỨỰỬỮ]", "U")
                   .replaceAll("[ỲÝỴỶỸ]", "Y")
                   .replaceAll("[Đ]", "D");
    }
} 