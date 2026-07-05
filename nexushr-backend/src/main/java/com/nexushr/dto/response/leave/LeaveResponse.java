package com.nexushr.dto.response.leave;

import com.nexushr.domain.enums.LeaveStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data @Builder
public class LeaveResponse {
    private UUID id;
    private UUID employeeId;
    private String employeeName;
    private String employeeCode;
    private String leaveTypeName;
    private String leaveTypeCode;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal totalDays;
    private LeaveStatus status;
    private String reason;
    private String rejectionNote;
    private String approvedByName;
    private LocalDateTime appliedAt;
    private LocalDateTime decidedAt;
}
