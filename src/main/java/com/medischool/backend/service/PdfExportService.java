package com.medischool.backend.service;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.itextpdf.io.font.PdfEncodings;
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
import com.medischool.backend.dto.vaccination.VaccinationHistoryWithStudentDTO;
import com.medischool.backend.model.checkup.CheckupEvent;
import com.medischool.backend.model.checkup.CheckupEventConsent;
import com.medischool.backend.model.parentstudent.Student;
import com.medischool.backend.model.vaccine.VaccinationHistory;
import com.medischool.backend.model.vaccine.VaccineEvent;
import com.medischool.backend.repository.StudentRepository;
import com.medischool.backend.repository.checkup.CheckupConsentRepository;
import com.medischool.backend.repository.checkup.CheckupEventRepository;
import com.medischool.backend.repository.vaccination.VaccineEventRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class PdfExportService {

    private final VaccineEventRepository vaccineEventRepository;
    private final StudentRepository studentRepository;
    private final CheckupEventRepository checkupEventRepository;
    private final CheckupConsentRepository checkupConsentRepository;

    public byte[] exportVaccinationHistoryByEvent(Long eventId, List<VaccinationHistoryWithStudentDTO> histories) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            java.io.InputStream fontStream = getClass().getClassLoader().getResourceAsStream("fonts/DejaVuSans.ttf");
            if (fontStream == null) throw new RuntimeException("Font DejaVuSans.ttf not found in resources/fonts");
            java.io.ByteArrayOutputStream buffer = new java.io.ByteArrayOutputStream();
            int nRead;
            byte[] data = new byte[16384];
            while ((nRead = fontStream.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }
            buffer.flush();
            byte[] fontBytes = buffer.toByteArray();
            PdfFont unicodeFont = PdfFontFactory.createFont(fontBytes, PdfEncodings.IDENTITY_H, PdfFontFactory.EmbeddingStrategy.FORCE_EMBEDDED);
            Document document = new Document(pdf);
            document.setFont(unicodeFont);

            String eventTitle = "";
            VaccineEvent event = vaccineEventRepository.findById(eventId).orElse(null);
            if (event != null && event.getEventTitle() != null) {
                eventTitle = event.getEventTitle();
            } else {
                eventTitle = "(Unknown Event)";
            }

            // Add title
            Paragraph title = new Paragraph("BÁO CÁO LỊCH SỬ TIÊM CHỦNG - " + eventTitle)
                    .setFontSize(18)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setBold();
            document.add(title);

            // Add event info
            Paragraph eventInfo = new Paragraph("Sự kiện: " + eventTitle)
                    .setFontSize(12)
                    .setTextAlignment(TextAlignment.CENTER);
            document.add(eventInfo);

            // Add table with all fields
            Table table = new Table(UnitValue.createPercentArray(new float[]{8, 12, 15, 12, 8, 10, 15, 10}))
                    .useAllAvailableWidth();

            // Add headers
            table.addHeaderCell(new Cell().add(new Paragraph("Mã Lịch Sử")).setBold());
            table.addHeaderCell(new Cell().add(new Paragraph("Mã Học Sinh")).setBold());
            table.addHeaderCell(new Cell().add(new Paragraph("Tên Học Sinh")).setBold());
            table.addHeaderCell(new Cell().add(new Paragraph("Vaccine")).setBold());
            table.addHeaderCell(new Cell().add(new Paragraph("Số Liều")).setBold());
            table.addHeaderCell(new Cell().add(new Paragraph("Ngày")).setBold());
            table.addHeaderCell(new Cell().add(new Paragraph("Địa Điểm")).setBold());
            table.addHeaderCell(new Cell().add(new Paragraph("Bất Thường")).setBold());

            // Add data
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

            for (VaccinationHistoryWithStudentDTO dto : histories) {
                VaccinationHistory history = dto.getHistory();
                table.addCell(new Cell().add(new Paragraph(history.getHistoryId().toString())));
                table.addCell(new Cell().add(new Paragraph(history.getStudentId().toString())));
                table.addCell(new Cell().add(new Paragraph(dto.getStudent() != null ?
                        dto.getStudent().getFullName() : "N/A")));
                table.addCell(new Cell().add(new Paragraph(history.getVaccine().getName())));
                table.addCell(new Cell().add(new Paragraph(history.getDoseNumber().toString())));
                table.addCell(new Cell().add(new Paragraph(history.getVaccinationDate().format(dateFormatter))));
                table.addCell(new Cell().add(new Paragraph(history.getLocation() != null ?
                        history.getLocation() : "N/A")));
                table.addCell(new Cell().add(new Paragraph(history.getAbnormal() != null && history.getAbnormal() ? "Có" : "Không")));
            }

            document.add(table);

            // Add detailed information section
            document.add(new Paragraph("THÔNG TIN CHI TIẾT").setFontSize(14).setBold());

            for (VaccinationHistoryWithStudentDTO dto : histories) {
                VaccinationHistory history = dto.getHistory();

                Paragraph recordTitle = new Paragraph("Mã Bản Ghi: " + history.getHistoryId())
                        .setFontSize(12)
                        .setBold();
                document.add(recordTitle);

                // Create detailed table for each record
                Table detailTable = new Table(UnitValue.createPercentArray(new float[]{30, 70}))
                        .useAllAvailableWidth();

                detailTable.addCell(new Cell().add(new Paragraph("Mã Học Sinh")).setBold());
                detailTable.addCell(new Cell().add(new Paragraph(history.getStudentId().toString())));

                detailTable.addCell(new Cell().add(new Paragraph("Tên Học Sinh")).setBold());
                detailTable.addCell(new Cell().add(new Paragraph(dto.getStudent() != null ?
                        dto.getStudent().getFullName() : "N/A")));

                detailTable.addCell(new Cell().add(new Paragraph("Sự Kiện")).setBold());
                detailTable.addCell(new Cell().add(new Paragraph(eventTitle)));

                detailTable.addCell(new Cell().add(new Paragraph("Vaccine")).setBold());
                detailTable.addCell(new Cell().add(new Paragraph(history.getVaccine().getName())));

                detailTable.addCell(new Cell().add(new Paragraph("Số Liều")).setBold());
                detailTable.addCell(new Cell().add(new Paragraph(history.getDoseNumber().toString())));

                detailTable.addCell(new Cell().add(new Paragraph("Ngày Tiêm")).setBold());
                detailTable.addCell(new Cell().add(new Paragraph(history.getVaccinationDate().format(dateFormatter))));

                detailTable.addCell(new Cell().add(new Paragraph("Địa Điểm")).setBold());
                detailTable.addCell(new Cell().add(new Paragraph(history.getLocation() != null ?
                        history.getLocation() : "N/A")));

                detailTable.addCell(new Cell().add(new Paragraph("Ghi Chú")).setBold());
                detailTable.addCell(new Cell().add(new Paragraph(history.getNote() != null ?
                        history.getNote() : "N/A")));

                detailTable.addCell(new Cell().add(new Paragraph("Bất Thường")).setBold());
                detailTable.addCell(new Cell().add(new Paragraph(history.getAbnormal() != null && history.getAbnormal() ? "Có" : "Không")));

                detailTable.addCell(new Cell().add(new Paragraph("Ghi Chú Theo Dõi")).setBold());
                detailTable.addCell(new Cell().add(new Paragraph(history.getFollowUpNote() != null ?
                        history.getFollowUpNote() : "N/A")));

                detailTable.addCell(new Cell().add(new Paragraph("Ngày Tạo")).setBold());
                detailTable.addCell(new Cell().add(new Paragraph(history.getCreatedAt() != null ?
                        history.getCreatedAt().format(dateTimeFormatter) : "N/A")));

                document.add(detailTable);
                document.add(new Paragraph("")); // Add space between records
            }

            // Add summary
            Paragraph summary = new Paragraph("Tổng số học sinh đã tiêm: " + histories.size())
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
            java.io.InputStream fontStream = getClass().getClassLoader().getResourceAsStream("fonts/DejaVuSans.ttf");
            if (fontStream == null) throw new RuntimeException("Font DejaVuSans.ttf not found in resources/fonts");
            java.io.ByteArrayOutputStream buffer = new java.io.ByteArrayOutputStream();
            int nRead;
            byte[] data = new byte[16384];
            while ((nRead = fontStream.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }
            buffer.flush();
            byte[] fontBytes = buffer.toByteArray();
            PdfFont unicodeFont = PdfFontFactory.createFont(fontBytes, PdfEncodings.IDENTITY_H, PdfFontFactory.EmbeddingStrategy.FORCE_EMBEDDED);
            Document document = new Document(pdf);
            document.setFont(unicodeFont);

            // Lấy map eventId -> eventTitle
            Set<Long> eventIds = historyByCategory.values().stream()
                .flatMap(List::stream)
                .map(VaccinationHistory::getEventId)
                .collect(Collectors.toSet());
            Map<Long, String> eventIdToTitle = vaccineEventRepository.findAllById(eventIds)
                .stream()
                .collect(Collectors.toMap(VaccineEvent::getId, VaccineEvent::getEventTitle));

            // Lấy map studentId -> studentName
            Set<Integer> studentIds = historyByCategory.values().stream()
                .flatMap(List::stream)
                .map(VaccinationHistory::getStudentId)
                .collect(Collectors.toSet());
            Map<Integer, String> studentIdToName = studentRepository.findAllById(studentIds)
                .stream()
                .collect(Collectors.toMap(Student::getStudentId, Student::getFullName));

            // Add title
            Paragraph title = new Paragraph("LỊCH SỬ TIÊM CHỦNG HỌC SINH")
                    .setFontSize(18)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setBold();
            document.add(title);

            // Add student info
            String studentCode = studentRepository.findById(studentId)
                .map(student -> student.getStudentCode())
                .orElse("");
            Paragraph studentInfo = new Paragraph("Mã Học Sinh: " + studentCode)
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
                Paragraph categoryTitle = new Paragraph("Danh Mục: " + categoryName)
                        .setFontSize(14)
                        .setBold();
                document.add(categoryTitle);

                // Add summary table for this category
                Table summaryTable = new Table(UnitValue.createPercentArray(new float[]{20, 20, 10, 10, 15, 15, 15}))
                        .useAllAvailableWidth();

                // Add headers
                summaryTable.addHeaderCell(new Cell().add(new Paragraph("Tên Học Sinh")).setBold());
                summaryTable.addHeaderCell(new Cell().add(new Paragraph("Sự Kiện")).setBold());
                summaryTable.addHeaderCell(new Cell().add(new Paragraph("Vaccine")).setBold());
                summaryTable.addHeaderCell(new Cell().add(new Paragraph("Liều")).setBold());
                summaryTable.addHeaderCell(new Cell().add(new Paragraph("Ngày")).setBold());
                summaryTable.addHeaderCell(new Cell().add(new Paragraph("Địa Điểm")).setBold());
                summaryTable.addHeaderCell(new Cell().add(new Paragraph("Bất Thường")).setBold());

                // Add data
                for (VaccinationHistory history : histories) {
                    String eventTitle = eventIdToTitle.getOrDefault(history.getEventId(), "(Sự Kiện Không Xác Định)");
                    String studentName = studentIdToName.getOrDefault(history.getStudentId(), "");
                    summaryTable.addCell(new Cell().add(new Paragraph(studentName)));
                    summaryTable.addCell(new Cell().add(new Paragraph(eventTitle)));
                    summaryTable.addCell(new Cell().add(new Paragraph(history.getVaccine().getName())));
                    summaryTable.addCell(new Cell().add(new Paragraph(history.getDoseNumber().toString())));
                    summaryTable.addCell(new Cell().add(new Paragraph(history.getVaccinationDate().format(dateFormatter))));
                    summaryTable.addCell(new Cell().add(new Paragraph(history.getLocation() != null ?
                        history.getLocation() : "N/A")));
                    summaryTable.addCell(new Cell().add(new Paragraph(history.getAbnormal() != null && history.getAbnormal() ? "Có" : "Không")));
                }

                document.add(summaryTable);
                
                // Add detailed information for each record in this category
                document.add(new Paragraph("BẢN GHI CHI TIẾT").setFontSize(12).setBold());
                
                for (VaccinationHistory history : histories) {
                    Paragraph recordTitle = new Paragraph("Bản Ghi Cho Sự Kiện: " + eventIdToTitle.getOrDefault(history.getEventId(), "(Sự Kiện Không Xác Định)") )
                            .setFontSize(11)
                            .setBold();
                    document.add(recordTitle);
                    
                    // Create detailed table for each record
                    Table detailTable = new Table(UnitValue.createPercentArray(new float[]{30, 70}))
                            .useAllAvailableWidth();
                    
                    detailTable.addCell(new Cell().add(new Paragraph("Tên Học Sinh")).setBold());
                    String studentName = studentIdToName.getOrDefault(history.getStudentId(), "");
                    detailTable.addCell(new Cell().add(new Paragraph(studentName)));
                    
                    detailTable.addCell(new Cell().add(new Paragraph("Sự Kiện")).setBold());
                    String eventTitle = eventIdToTitle.getOrDefault(history.getEventId(), "(Sự Kiện Không Xác Định)");
                    detailTable.addCell(new Cell().add(new Paragraph(eventTitle)));
                    
                    detailTable.addCell(new Cell().add(new Paragraph("Vaccine")).setBold());
                    detailTable.addCell(new Cell().add(new Paragraph(history.getVaccine().getName())));
                    
                    detailTable.addCell(new Cell().add(new Paragraph("Số Liều")).setBold());
                    detailTable.addCell(new Cell().add(new Paragraph(history.getDoseNumber().toString())));
                    
                    detailTable.addCell(new Cell().add(new Paragraph("Ngày Tiêm")).setBold());
                    detailTable.addCell(new Cell().add(new Paragraph(history.getVaccinationDate().format(dateFormatter))));
                    
                    detailTable.addCell(new Cell().add(new Paragraph("Địa Điểm")).setBold());
                    detailTable.addCell(new Cell().add(new Paragraph(history.getLocation() != null ?
                        history.getLocation() : "N/A")));
                    
                    detailTable.addCell(new Cell().add(new Paragraph("Ghi Chú")).setBold());
                    detailTable.addCell(new Cell().add(new Paragraph(history.getNote() != null ?
                        history.getNote() : "N/A")));
                    
                    detailTable.addCell(new Cell().add(new Paragraph("Bất Thường")).setBold());
                    detailTable.addCell(new Cell().add(new Paragraph(history.getAbnormal() != null && history.getAbnormal() ? "Có" : "Không")));
                    
                    detailTable.addCell(new Cell().add(new Paragraph("Ghi Chú Theo Dõi")).setBold());
                    detailTable.addCell(new Cell().add(new Paragraph(history.getFollowUpNote() != null ?
                        history.getFollowUpNote() : "N/A")));
                    
                    detailTable.addCell(new Cell().add(new Paragraph("Ngày Tạo")).setBold());
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
            Paragraph summary = new Paragraph("Tổng số lần tiêm: " + totalVaccinations)
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
     * Generate PDF report for health checkup consents
     * 
     * @param eventId The health checkup event ID
     * @return PDF content as byte array
     * @throws Exception if PDF generation fails
     */
    public byte[] generateHealthCheckupConsentsPDF(Long eventId) throws Exception {
        // Get health checkup event and consent data
        CheckupEvent checkupEvent = checkupEventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Health checkup event not found with ID: " + eventId));

        List<CheckupEventConsent> consents = checkupConsentRepository.findByEventId(eventId);

        if (consents.isEmpty()) {
            throw new IllegalArgumentException("No consent records found for health checkup event ID: " + eventId);
        }

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            
            // Load font for Vietnamese characters
            java.io.InputStream fontStream = getClass().getClassLoader().getResourceAsStream("fonts/DejaVuSans.ttf");
            if (fontStream == null) throw new RuntimeException("Font DejaVuSans.ttf not found in resources/fonts");
            java.io.ByteArrayOutputStream buffer = new java.io.ByteArrayOutputStream();
            int nRead;
            byte[] data = new byte[16384];
            while ((nRead = fontStream.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }
            buffer.flush();
            byte[] fontBytes = buffer.toByteArray();
            PdfFont unicodeFont = PdfFontFactory.createFont(fontBytes, PdfEncodings.IDENTITY_H, PdfFontFactory.EmbeddingStrategy.FORCE_EMBEDDED);
            
            Document document = new Document(pdf);
            document.setFont(unicodeFont);

            // Add title
            Paragraph title = new Paragraph("BÁO CÁO ĐỒNG THUẬN KHÁM SỨC KHỎE")
                    .setFontSize(18)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setBold();
            document.add(title);

            // Add event information
            document.add(new Paragraph(""));

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

            Paragraph eventInfo = new Paragraph("Sự kiện: " + checkupEvent.getEventTitle())
                    .setFontSize(12);
            document.add(eventInfo);

            Paragraph dateInfo = new Paragraph("Thời gian: " +
                    checkupEvent.getStartDate().format(formatter) + " - " + checkupEvent.getEndDate().format(formatter))
                    .setFontSize(12);
            document.add(dateInfo);



            document.add(new Paragraph(""));

            // Create table for consent data
            Table table = new Table(UnitValue.createPercentArray(new float[]{8, 20, 12, 20, 20, 12, 8}))
                    .useAllAvailableWidth();

            // Add table headers
            table.addHeaderCell(new Cell().add(new Paragraph("STT")).setBold());
            table.addHeaderCell(new Cell().add(new Paragraph("Học sinh")).setBold());
            table.addHeaderCell(new Cell().add(new Paragraph("Lớp")).setBold());
            table.addHeaderCell(new Cell().add(new Paragraph("Phụ huynh")).setBold());
            table.addHeaderCell(new Cell().add(new Paragraph("Liên lạc")).setBold());
            table.addHeaderCell(new Cell().add(new Paragraph("Trạng thái")).setBold());
            table.addHeaderCell(new Cell().add(new Paragraph("Ngày phản hồi")).setBold());

            // Add data rows
            int counter = 1;

            for (CheckupEventConsent consent : consents) {
                // STT
                table.addCell(new Cell().add(new Paragraph(String.valueOf(counter++))));

                // Student name
                String studentName = consent.getStudent().getFullName() + "\n(" + consent.getStudent().getStudentCode() + ")";
                table.addCell(new Cell().add(new Paragraph(studentName)));

                // Class
                table.addCell(new Cell().add(new Paragraph(consent.getStudent().getClassCode())));

                // Parent name
                String parentName = consent.getParent() != null ? consent.getParent().getFullName() : "N/A";
                table.addCell(new Cell().add(new Paragraph(parentName)));

                // Contact info
                StringBuilder contactInfo = new StringBuilder();
                if (consent.getParent() != null) {
                    if (consent.getParent().getEmail() != null && !consent.getParent().getEmail().isEmpty()) {
                        contactInfo.append(consent.getParent().getEmail()).append("\n");
                    }
                    if (consent.getParent().getPhone() != null && !consent.getParent().getPhone().isEmpty()) {
                        contactInfo.append(consent.getParent().getPhone());
                    }
                }
                String contact = contactInfo.length() > 0 ? contactInfo.toString() : "N/A";
                table.addCell(new Cell().add(new Paragraph(contact)));

                // Status
                String status = "Chưa phản hồi";
                if (consent.getConsentStatus() != null) {
                    switch (consent.getConsentStatus().toString()) {
                        case "APPROVED":
                            status = "Đồng ý";
                            break;
                        case "REJECTED":
                            status = "Từ chối";
                            break;
                        default:
                            status = "Chưa phản hồi";
                    }
                }
                table.addCell(new Cell().add(new Paragraph(status)));

                // Response date
                String responseDate = consent.getUpdatedAt() != null ? consent.getUpdatedAt().format(formatter) : "N/A";
                table.addCell(new Cell().add(new Paragraph(responseDate)));
            }

            document.add(table);

            // Add summary statistics
            document.add(new Paragraph(""));

            long approvedCount = consents.stream()
                    .filter(c -> c.getConsentStatus() != null && "APPROVED".equals(c.getConsentStatus().toString()))
                    .count();

            long rejectedCount = consents.stream()
                    .filter(c -> c.getConsentStatus() != null && "REJECTED".equals(c.getConsentStatus().toString()))
                    .count();

            long pendingCount = consents.stream()
                    .filter(c -> c.getConsentStatus() == null || "PENDING".equals(c.getConsentStatus().toString()))
                    .count();

            Paragraph summary = new Paragraph("Tổng số: " + consents.size() + 
                    " | Đồng ý: " + approvedCount + 
                    " | Từ chối: " + rejectedCount + 
                    " | Chưa phản hồi: " + pendingCount)
                    .setFontSize(12)
                    .setTextAlignment(TextAlignment.CENTER);
            document.add(summary);

            // Add generation timestamp
            document.add(new Paragraph(""));
            Paragraph timestamp = new Paragraph("Báo cáo được tạo lúc: " +
                    java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")))
                    .setFontSize(10)
                    .setItalic()
                    .setTextAlignment(TextAlignment.RIGHT);
            document.add(timestamp);

            document.close();
            return baos.toByteArray();

        } catch (Exception e) {
            log.error("Error generating health checkup PDF for event {}: {}", eventId, e.getMessage(), e);
            throw new RuntimeException("Failed to generate health checkup PDF", e);
        }
    }
} 