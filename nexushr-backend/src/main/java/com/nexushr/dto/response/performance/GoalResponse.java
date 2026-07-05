package com.nexushr.dto.response.performance;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class GoalResponse {

    private UUID          id;
    private UUID          employeeId;
    private String        employeeName;
    private String        title;
    private String        description;
    private LocalDate     targetDate;
    private String        status;
    private Integer       year;
    private Integer       quarter;
    private String        quarterLabel;    // e.g. "Q3 2024"
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
