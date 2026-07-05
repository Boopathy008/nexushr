package com.nexushr.dto.response.dashboard;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class AdminDashboardResponse {

    // ── Workforce Summary ─────────────────────────────────────────────────────
    private long totalEmployees;
    private long activeEmployees;
    private long inactiveEmployees;
    private long terminatedEmployees;
    private long newJoineesThisMonth;

    // ── Department Summary ────────────────────────────────────────────────────
    private long totalDepartments;
    private List<DepartmentHeadcount> departmentHeadcounts;

    // ── Attendance Summary (today) ────────────────────────────────────────────
    private long checkedInToday;
    private long absentToday;
    private long onLeaveToday;
    private double todayAttendancePercentage;

    // ── Leave Summary ─────────────────────────────────────────────────────────
    private long pendingLeaveRequests;
    private long approvedLeavesThisMonth;
    private long rejectedLeavesThisMonth;

    // ── Payroll Summary ───────────────────────────────────────────────────────
    private BigDecimal totalPayrollThisMonth;
    private long       processedPayslipsThisMonth;
    private long       pendingPayslipsThisMonth;

    // ── Performance Summary ───────────────────────────────────────────────────
    private double averagePerformanceRating;
    private long   reviewsSubmittedThisQuarter;

    // ── AI Intelligence Summary ───────────────────────────────────────────────
    private long criticalAttritionRiskCount;
    private long highAttritionRiskCount;
    private long mediumAttritionRiskCount;
    private long lowAttritionRiskCount;
    private long disengagedEmployeesCount;

    // ── Recent Activity ───────────────────────────────────────────────────────
    private List<RecentActivity> recentActivities;

    // ── Inner classes ─────────────────────────────────────────────────────────

    @Data
    @Builder
    public static class DepartmentHeadcount {
        private String departmentName;
        private String departmentCode;
        private long   activeCount;
        private long   totalCount;
        private double headcountPercentage;
    }

    @Data
    @Builder
    public static class RecentActivity {
        private String activityType;   // LEAVE_APPLIED, EMPLOYEE_JOINED, PAYROLL_GENERATED
        private String description;
        private String performedBy;
        private String timestamp;
    }
}
