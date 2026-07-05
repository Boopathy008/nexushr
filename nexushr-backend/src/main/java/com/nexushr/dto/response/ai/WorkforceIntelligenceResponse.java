package com.nexushr.dto.response.ai;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data @Builder
public class WorkforceIntelligenceResponse {

    private UUID   employeeId;
    private String employeeName;
    private String employeeCode;
    private String department;
    private String designation;

    // ── Attrition risk ────────────────────────────────────────────────────────
    private int    attritionRiskScore;          // 0–100
    private String attritionRiskLevel;          // LOW / MEDIUM / HIGH / CRITICAL
    private List<String> attritionRiskFactors;  // Human-readable reasons

    // ── Engagement ────────────────────────────────────────────────────────────
    private int    engagementScore;             // 0–100
    private String engagementLevel;             // DISENGAGED / MODERATE / ENGAGED / HIGHLY_ENGAGED
    private List<String> engagementFactors;

    // ── Attendance insight ────────────────────────────────────────────────────
    private double attendanceRatePercent;
    private int    lopDaysLast3Months;
    private int    presentDaysLast30;

    // ── Leave insight ─────────────────────────────────────────────────────────
    private BigDecimal leaveBalanceUsedPercent;
    private long   pendingLeaveRequests;

    // ── Performance insight ───────────────────────────────────────────────────
    private BigDecimal averagePerformanceRating;
    private int        totalReviewsCount;

    // ── Tenure ────────────────────────────────────────────────────────────────
    private long   tenureInMonths;
    private String tenureLabel;

    // ── Recommendations ───────────────────────────────────────────────────────
    private List<String> recommendations;
}