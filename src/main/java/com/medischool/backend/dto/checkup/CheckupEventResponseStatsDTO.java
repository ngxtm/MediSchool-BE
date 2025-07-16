package com.medischool.backend.dto.checkup;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckupEventResponseStatsDTO {
    private long totalStudents;      // Tổng số học sinh
    private long totalSent;          // Số đơn đã gửi
    private long totalReplied;       // Số đơn đã phản hồi
    private long totalNotReplied;    // Số đơn chưa phản hồi
}
