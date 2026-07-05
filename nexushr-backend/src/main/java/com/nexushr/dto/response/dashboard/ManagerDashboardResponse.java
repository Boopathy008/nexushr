package com.nexushr.dto.response.dashboard;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class ManagerDashboardResponse {

    // ── Manager Info ──────────────────────────────────────────────────────────
    private String managerName;
    private String departmentName;
    private String departmentCode;

    // ── Team Overview ─────────────────────────────────────────────────────────
    private long totalTeamMembers;
    private long activeTeamMembers;
    private long onLeaveTeamMembers;

    // ── Team Attendance (today) ───────────────────────────────────────────────
    private long checkedInToday;
    private long absentToday;
    private long onLeaveToday;
    private double teamAttendancePercentage;

    // ── Team Attendance (this month) ──────────────────────────────────────────
    private double teamMonthlyAttendancePercentage;
    private long   teamLopDaysThisMonth;

    // ── Pending Leave Requests ────────────────────────────────────────────────
    private long                    pendingLeaveCount;
    private List<PendingLeaveItem>  pendingLeaveRequests;

    // ── Team Performance ──────────────────────────────────────────────────────
    private double teamAverageRating;
    private long   pendingReviewsCount;
    private long   goalsCompletedThisQuarter;
    private long   goalsInProgressThisQuarter;

    // ── Team Payroll Summary ──────────────────────────────────────────────────
    private BigDecimal teamTotalNetSalaryThisMonth;
    private long       teamPayslipsProcessed;

    // ── Top Performers ────────────────────────────────────────────────────────
    private List<TeamMemberSummary> topPerformers;

    // ── Attendance Risk ───────────────────────────────────────────────────────
    private List<TeamMemberSummary> lowAttendanceMembers;

    // ── Inner classes ─────────────────────────────────────────────────────────

    @Data
    @Builder
    public static class PendingLeaveItem {
        private String leaveRequestId;
        private String employeeName;
        private String employeeCode;
        private String leaveTypeName;
        private String startDate;
        private String endDate;
        private double totalDays;
        private String appliedAt;
        private String reason;
    }

    @Data
    @Builder
    public static class TeamMemberSummary {
        private String employeeId;
        private String employeeName;
        private String employeeCode;
        private String designation;
        private double attendancePercentage;
        private double performanceRating;
        private String status;
    }
}
