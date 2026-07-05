package com.nexushr.dto.response.performance;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class ReviewResponse {

    private UUID          id;
    private UUID          employeeId;
    private String        employeeName;
    private String        employeeCode;
    private String        reviewerName;
    private Integer       reviewYear;
    private Integer       reviewQuarter;
    private String        reviewPeriod;      // e.g. "Q2 2024"
    private BigDecimal    rating;
    private String        ratingLabel;       // e.g. "Exceeds Expectations"
    private String        feedback;
    private String        strengths;
    private String        improvements;
    private String        status;
    private LocalDateTime submittedAt;
    private LocalDateTime createdAt;
}
