package com.nexushr.dto.response.dashboard;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class EmployeeDashboardResponse {

    // ── Personal Info ─────────────────────────────────────────────────────────
    private String employeeId;
    private String employeeName;
    private String employeeCode;
    private String designation;
    private String department;
    private String dateOfJoining;
    private String tenureLabel;
    private String profilePictureUrl;

    // ── Today's Attendance ────────────────────────────────────────────────────
    private boolean checkedInToday;
    private String  checkInTime;
    private String  checkOutTime;
    private String  workingHoursToday;
    private String  attendanceStatusToday;

    // ── Monthly Attendance ────────────────────────────────────────────────────
    private int    workingDaysThisMonth;
    private int    presentDaysThisMonth;
    private int    absentDaysThisMonth;
    private int    halfDaysThisMonth;
    private int    lopDaysThisMonth;
    private double attendancePercentageThisMonth;

    // ── Leave Balance ─────────────────────────────────────────────────────────
    private List<LeaveBalanceSummary> leaveBalances;
    private long                      pendingLeaveRequests;
    private String                    nextLeave;        // upcoming approved leave date

    // ── Payroll Summary ───────────────────────────────────────────────────────
    private BigDecimal lastMonthNetSalary;
    private BigDecimal lastMonthGrossSalary;
    private BigDecimal lastMonthDeductions;
    private String     lastPayslipPeriod;    // e.g. "June 2024"
    private String     lastPayslipStatus;

    // ── Performance Summary ───────────────────────────────────────────────────
    private BigDecimal averagePerformanceRating;
    private String     ratingLabel;            // e.g. "Exceeds Expectations"
    private int        totalReviewsCount;
    private BigDecimal latestQuarterRating;
    private String     latestReviewPeriod;     // e.g. "Q2 2024"

    // ── Goals Summary ─────────────────────────────────────────────────────────
    private long goalsInProgress;
    private long goalsCompleted;
    private long goalsTotal;

    // ── Announcements / Notifications ─────────────────────────────────────────
    private List<DashboardNotification> notifications;

    // ── Inner classes ─────────────────────────────────────────────────────────

    @Data
    @Builder
    public static class LeaveBalanceSummary {
        private String leaveTypeName;
        private String leaveTypeCode;
        private boolean isPaid;
        private double totalDays;
        private double usedDays;
        private double availableDays;
        private double usagePercentage;
    }

    @Data
    @Builder
    public static class DashboardNotification {
        private String type;        // LEAVE_APPROVED, PAYSLIP_READY, REVIEW_SUBMITTED
        private String title;
        private String message;
        private String timestamp;
        private boolean isRead;
    }
}
