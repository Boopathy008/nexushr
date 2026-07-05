package com.nexushr.dto.response.attendance;

import com.nexushr.domain.enums.AttendanceStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data @Builder
public class AttendanceResponse {
    private UUID id;
    private UUID employeeId;
    private String employeeCode;
    private String employeeName;
    private LocalDate attendanceDate;
    private LocalDateTime checkInTime;
    private LocalDateTime checkOutTime;
    private AttendanceStatus status;
    private Integer workingMinutes;
    private String workingHours;
    private String notes;
}
