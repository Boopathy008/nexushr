package com.nexushr.service;

import com.nexushr.domain.entity.*;
import com.nexushr.domain.enums.*;
import com.nexushr.dto.response.dashboard.*;
import com.nexushr.exception.ResourceNotFoundException;
import com.nexushr.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class DashboardService {

    private final EmployeeRepository      employeeRepository;
    private final DepartmentRepository    departmentRepository;
    private final AttendanceRepository    attendanceRepository;
    private final LeaveRequestRepository  leaveRequestRepository;
    private final LeaveBalanceRepository  leaveBalanceRepository;
    private final PayrollRunRepository    payrollRunRepository;
    private final PerformanceReviewRepository reviewRepository;
    private final PerformanceGoalRepository   goalRepository;
    private final UserRepository          userRepository;

    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("dd MMM yyyy");
    private static final DateTimeFormatter TIME_FMT =
            DateTimeFormatter.ofPattern("HH:mm");

    // ── Admin Dashboard ───────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public AdminDashboardResponse getAdminDashboard() {
        LocalDate today     = LocalDate.now();
        LocalDate monthStart = today.withDayOfMonth(1);
        int currentYear     = today.getYear();
        int currentMonth    = today.getMonthValue();
        int currentQuarter  = (currentMonth - 1) / 3 + 1;

        // ── Workforce counts ──────────────────────────────────────────────────
        long totalEmployees      = employeeRepository.countByStatus(EmployeeStatus.ACTIVE)
                + employeeRepository.countByStatus(EmployeeStatus.INACTIVE)
                + employeeRepository.countByStatus(EmployeeStatus.ON_LEAVE)
                + employeeRepository.countByStatus(EmployeeStatus.TERMINATED);
        long activeEmployees     = employeeRepository.countByStatus(EmployeeStatus.ACTIVE);
        long inactiveEmployees   = employeeRepository.countByStatus(EmployeeStatus.INACTIVE);
        long terminatedEmployees = employeeRepository.countByStatus(EmployeeStatus.TERMINATED);

        // New joiners this month
        long newJoinees = employeeRepository
                .findAllWithFilters(EmployeeStatus.ACTIVE, null, Pageable.unpaged())
                .getContent().stream()
                .filter(e -> !e.getDateOfJoining().isBefore(monthStart))
                .count();

        // ── Department headcounts ─────────────────────────────────────────────
        List<AdminDashboardResponse.DepartmentHeadcount> deptHeadcounts =
                departmentRepository.findAllActiveWithDesignations().stream()
                        .map(dept -> {
                            long active = employeeRepository
                                    .countByDepartmentId(dept.getId());
                            return AdminDashboardResponse.DepartmentHeadcount.builder()
                                    .departmentName(dept.getName())
                                    .departmentCode(dept.getCode())
                                    .activeCount(active)
                                    .totalCount(active)
                                    .headcountPercentage(activeEmployees > 0
                                            ? round2((double) active / activeEmployees * 100)
                                            : 0)
                                    .build();
                        }).toList();

        // ── Attendance today ──────────────────────────────────────────────────
        long checkedInToday = attendanceRepository
                .countByDateAndStatus(today, AttendanceStatus.PRESENT);
        long onLeaveToday = attendanceRepository
                .countByDateAndStatus(today, AttendanceStatus.ON_LEAVE);
        long absentToday  = activeEmployees - checkedInToday - onLeaveToday;
        double todayPct   = activeEmployees > 0
                ? round2((double) checkedInToday / activeEmployees * 100) : 0;

        // ── Leave summary ─────────────────────────────────────────────────────
        long pendingLeaves   = leaveRequestRepository
                .countByStatus(LeaveStatus.PENDING);
        long approvedLeaves  = leaveRequestRepository
                .countByStatusAndDateRange(LeaveStatus.APPROVED, monthStart.atStartOfDay(), today.atTime(LocalTime.MAX));
        long rejectedLeaves  = leaveRequestRepository
                .countByStatusAndDateRange(LeaveStatus.REJECTED, monthStart.atStartOfDay(), today.atTime(LocalTime.MAX));

        // ── Payroll summary ───────────────────────────────────────────────────
        BigDecimal totalPayroll = payrollRunRepository
                .sumNetSalaryForPeriod(currentMonth, currentYear);
        long processedPayslips  = payrollRunRepository
                .countByPayMonthAndPayYearAndStatus(
                        currentMonth, currentYear, PayrollStatus.PROCESSED);
        long pendingPayslips    = activeEmployees - processedPayslips;

        // ── Performance summary ───────────────────────────────────────────────
        BigDecimal avgRating = reviewRepository.averageRatingAllEmployees();
        long reviewsThisQtr  = reviewRepository
                .countByReviewYearAndReviewQuarter(currentYear, currentQuarter);

        return AdminDashboardResponse.builder()
                .totalEmployees(totalEmployees)
                .activeEmployees(activeEmployees)
                .inactiveEmployees(inactiveEmployees)
                .terminatedEmployees(terminatedEmployees)
                .newJoineesThisMonth(newJoinees)
                .totalDepartments(deptHeadcounts.size())
                .departmentHeadcounts(deptHeadcounts)
                .checkedInToday(checkedInToday)
                .absentToday(Math.max(absentToday, 0))
                .onLeaveToday(onLeaveToday)
                .todayAttendancePercentage(todayPct)
                .pendingLeaveRequests(pendingLeaves)
                .approvedLeavesThisMonth(approvedLeaves)
                .rejectedLeavesThisMonth(rejectedLeaves)
                .totalPayrollThisMonth(
                        totalPayroll != null ? totalPayroll : BigDecimal.ZERO)
                .processedPayslipsThisMonth(processedPayslips)
                .pendingPayslipsThisMonth(Math.max(pendingPayslips, 0))
                .averagePerformanceRating(
                        avgRating != null ? avgRating.doubleValue() : 0.0)
                .reviewsSubmittedThisQuarter(reviewsThisQtr)
                .criticalAttritionRiskCount(0L)
                .highAttritionRiskCount(0L)
                .mediumAttritionRiskCount(0L)
                .lowAttritionRiskCount(0L)
                .disengagedEmployeesCount(0L)
                .recentActivities(List.of())
                .build();
    }

    // ── Manager Dashboard ─────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public ManagerDashboardResponse getManagerDashboard(UUID managerId) {
        User manager = userRepository.findById(managerId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Manager not found: " + managerId));

        // Find the department this manager leads
        Department department = departmentRepository
                .findByManagerId(managerId)
                .orElse(null);

        if (department == null) {
            return ManagerDashboardResponse.builder()
                    .managerName(manager.getUsername())
                    .departmentName("Unassigned")
                    .departmentCode("NONE")
                    .totalTeamMembers(0L)
                    .activeTeamMembers(0L)
                    .onLeaveTeamMembers(0L)
                    .checkedInToday(0L)
                    .absentToday(0L)
                    .onLeaveToday(0L)
                    .teamAttendancePercentage(0.0)
                    .pendingLeaveCount(0)
                    .pendingLeaveRequests(List.of())
                    .teamAverageRating(0.0)
                    .pendingReviewsCount(0L)
                    .goalsCompletedThisQuarter(0L)
                    .goalsInProgressThisQuarter(0L)
                    .teamTotalNetSalaryThisMonth(BigDecimal.ZERO)
                    .teamPayslipsProcessed(0L)
                    .topPerformers(List.of())
                    .lowAttendanceMembers(List.of())
                    .build();
        }

        LocalDate today      = LocalDate.now();
        LocalDate monthStart = today.withDayOfMonth(1);
        int currentYear      = today.getYear();
        int currentMonth     = today.getMonthValue();
        int currentQuarter   = (currentMonth - 1) / 3 + 1;
        UUID deptId          = department.getId();

        // ── Team overview ─────────────────────────────────────────────────────
        List<Employee> teamMembers = employeeRepository
                .findAllByDepartmentIdAndStatus(deptId, EmployeeStatus.ACTIVE);
        long totalTeam   = teamMembers.size();
        long onLeaveTeam = teamMembers.stream()
                .filter(e -> e.getStatus() == EmployeeStatus.ON_LEAVE).count();

        // ── Team attendance today ─────────────────────────────────────────────
        long checkedInToday = teamMembers.stream()
                .filter(e -> attendanceRepository
                        .findByEmployeeIdAndAttendanceDate(e.getId(), today)
                        .map(a -> a.getStatus() == AttendanceStatus.PRESENT
                                || a.getStatus() == AttendanceStatus.HALF_DAY)
                        .orElse(false))
                .count();
        long onLeaveTodayTeam = teamMembers.stream()
                .filter(e -> attendanceRepository
                        .findByEmployeeIdAndAttendanceDate(e.getId(), today)
                        .map(a -> a.getStatus() == AttendanceStatus.ON_LEAVE)
                        .orElse(false))
                .count();
        long absentToday = totalTeam - checkedInToday - onLeaveTodayTeam;
        double teamPct   = totalTeam > 0
                ? round2((double) checkedInToday / totalTeam * 100) : 0;

        // ── Pending leaves for department ─────────────────────────────────────
        List<ManagerDashboardResponse.PendingLeaveItem> pendingLeaves =
                leaveRequestRepository
                        .findByDepartmentAndStatus(deptId, LeaveStatus.PENDING)
                        .stream()
                        .map(lr -> ManagerDashboardResponse.PendingLeaveItem.builder()
                                .leaveRequestId(lr.getId().toString())
                                .employeeName(lr.getEmployee().getFullName())
                                .employeeCode(lr.getEmployee().getEmployeeCode())
                                .leaveTypeName(lr.getLeaveType().getName())
                                .startDate(lr.getStartDate().format(DATE_FMT))
                                .endDate(lr.getEndDate().format(DATE_FMT))
                                .totalDays(lr.getTotalDays().doubleValue())
                                .appliedAt(lr.getAppliedAt()
                                        .format(DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm")))
                                .reason(lr.getReason())
                                .build())
                        .toList();

        // ── Team performance ──────────────────────────────────────────────────
        BigDecimal teamAvgRating = reviewRepository
                .averageRatingByDepartment(deptId);
        long reviewsThisQtr = reviewRepository
                .countByDepartmentAndQuarter(deptId, currentYear, currentQuarter);
        long goalsCompleted = goalRepository
                .countByDepartmentIdAndStatus(deptId, "COMPLETED");
        long goalsInProgress = goalRepository
                .countByDepartmentIdAndStatus(deptId, "IN_PROGRESS");

        // ── Payroll ───────────────────────────────────────────────────────────
        BigDecimal teamPayroll = payrollRunRepository
                .sumNetSalaryByDepartmentAndPeriod(deptId, currentMonth, currentYear);
        long processedPayslips = payrollRunRepository
                .countByDepartmentAndPeriod(deptId, currentMonth, currentYear);

        // ── Top performers ────────────────────────────────────────────────────
        List<ManagerDashboardResponse.TeamMemberSummary> topPerformers =
                buildTeamSummaries(teamMembers, today, monthStart, true);

        // ── Low attendance members ────────────────────────────────────────────
        List<ManagerDashboardResponse.TeamMemberSummary> lowAttendance =
                buildTeamSummaries(teamMembers, today, monthStart, false);

        return ManagerDashboardResponse.builder()
                .managerName(manager.getUsername())
                .departmentName(department.getName())
                .departmentCode(department.getCode())
                .totalTeamMembers(totalTeam)
                .activeTeamMembers(totalTeam - onLeaveTeam)
                .onLeaveTeamMembers(onLeaveTeam)
                .checkedInToday(checkedInToday)
                .absentToday(Math.max(absentToday, 0))
                .onLeaveToday(onLeaveTodayTeam)
                .teamAttendancePercentage(teamPct)
                .teamMonthlyAttendancePercentage(0.0)
                .teamLopDaysThisMonth(0L)
                .pendingLeaveCount(pendingLeaves.size())
                .pendingLeaveRequests(pendingLeaves)
                .teamAverageRating(teamAvgRating != null
                        ? teamAvgRating.doubleValue() : 0.0)
                .pendingReviewsCount(totalTeam - reviewsThisQtr)
                .goalsCompletedThisQuarter(goalsCompleted)
                .goalsInProgressThisQuarter(goalsInProgress)
                .teamTotalNetSalaryThisMonth(
                        teamPayroll != null ? teamPayroll : BigDecimal.ZERO)
                .teamPayslipsProcessed(processedPayslips)
                .topPerformers(topPerformers)
                .lowAttendanceMembers(lowAttendance)
                .build();
    }

    // ── Employee Dashboard ────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public EmployeeDashboardResponse getEmployeeDashboard(UUID employeeId) {
        Employee employee = employeeRepository.findByIdWithDetails(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Employee not found: " + employeeId));

        LocalDate today      = LocalDate.now();
        LocalDate monthStart = today.withDayOfMonth(1);
        int currentYear      = today.getYear();
        int currentMonth     = today.getMonthValue();
        int currentQuarter   = (currentMonth - 1) / 3 + 1;
        int prevMonth        = currentMonth == 1 ? 12 : currentMonth - 1;
        int prevYear         = currentMonth == 1 ? currentYear - 1 : currentYear;

        // ── Tenure ────────────────────────────────────────────────────────────
        long tenureMonths = ChronoUnit.MONTHS.between(
                employee.getDateOfJoining(), today);
        String tenureLabel = tenureMonths < 12
                ? tenureMonths + " months"
                : (tenureMonths / 12) + " yr " + (tenureMonths % 12) + " mo";

        // ── Today's attendance ────────────────────────────────────────────────
        Attendance todayRecord = attendanceRepository
                .findByEmployeeIdAndAttendanceDate(employeeId, today)
                .orElse(null);

        boolean checkedInToday    = todayRecord != null
                && todayRecord.getCheckInTime() != null;
        String checkInTime        = checkedInToday
                ? todayRecord.getCheckInTime().format(TIME_FMT) : null;
        String checkOutTime       = (todayRecord != null
                && todayRecord.getCheckOutTime() != null)
                ? todayRecord.getCheckOutTime().format(TIME_FMT) : null;
        String workingHoursToday  = todayRecord != null
                ? formatMinutes(todayRecord.getWorkingMinutes()) : "0h 00m";
        String attendanceStatus   = todayRecord != null
                ? todayRecord.getStatus().name() : "NOT_CHECKED_IN";

        // ── Monthly attendance ────────────────────────────────────────────────
        List<Attendance> monthRecords = attendanceRepository
                .findAllByEmployeeIdAndAttendanceDateBetweenOrderByAttendanceDateDesc(
                        employeeId, monthStart, today);

        int workingDays   = countWorkingDays(monthStart, today);
        int presentDays   = (int) monthRecords.stream()
                .filter(a -> a.getStatus() == AttendanceStatus.PRESENT).count();
        int halfDays      = (int) monthRecords.stream()
                .filter(a -> a.getStatus() == AttendanceStatus.HALF_DAY).count();
        int leaveDays     = (int) monthRecords.stream()
                .filter(a -> a.getStatus() == AttendanceStatus.ON_LEAVE).count();
        int lopDays       = Math.max(0, workingDays - presentDays - halfDays - leaveDays);
        int absentDays    = (int) monthRecords.stream()
                .filter(a -> a.getStatus() == AttendanceStatus.ABSENT).count();
        double attendPct  = workingDays > 0
                ? round2((presentDays + halfDays * 0.5) / workingDays * 100) : 0;

        // ── Leave balances ────────────────────────────────────────────────────
        List<EmployeeDashboardResponse.LeaveBalanceSummary> leaveBalances =
                leaveBalanceRepository
                        .findBalancesWithLeaveType(employeeId, currentYear)
                        .stream()
                        .map(lb -> {
                            double usagePct = lb.getTotalDays().doubleValue() > 0
                                    ? round2(lb.getUsedDays().doubleValue()
                                    / lb.getTotalDays().doubleValue() * 100)
                                    : 0;
                            return EmployeeDashboardResponse.LeaveBalanceSummary.builder()
                                    .leaveTypeName(lb.getLeaveType().getName())
                                    .leaveTypeCode(lb.getLeaveType().getCode())
                                    .isPaid(lb.getLeaveType().isPaid())
                                    .totalDays(lb.getTotalDays().doubleValue())
                                    .usedDays(lb.getUsedDays().doubleValue())
                                    .availableDays(lb.getAvailableDays().doubleValue())
                                    .usagePercentage(usagePct)
                                    .build();
                        }).toList();

        long pendingLeaves = leaveRequestRepository
                .countByEmployeeIdAndStatus(employeeId, LeaveStatus.PENDING);

        // ── Payroll summary (last month) ──────────────────────────────────────
        PayrollRun lastPayroll = payrollRunRepository
                .findByEmployeeIdAndPayMonthAndPayYear(employeeId, prevMonth, prevYear)
                .orElse(null);

        String lastPayPeriod  = Month.of(prevMonth)
                .getDisplayName(TextStyle.FULL, Locale.ENGLISH) + " " + prevYear;

        // ── Performance ───────────────────────────────────────────────────────
        BigDecimal avgRating    = reviewRepository.averageRatingByEmployee(employeeId);
        int reviewCount         = reviewRepository.countByEmployeeId(employeeId);
        List<PerformanceReview> reviews = reviewRepository
                .findAllByEmployeeIdOrderByReviewYearDescReviewQuarterDesc(employeeId);
        PerformanceReview latestReview  = reviews.isEmpty() ? null : reviews.get(0);

        // ── Goals ─────────────────────────────────────────────────────────────
        long goalsInProgress = goalRepository
                .countByEmployeeIdAndStatus(employeeId, "IN_PROGRESS");
        long goalsCompleted  = goalRepository
                .countByEmployeeIdAndStatus(employeeId, "COMPLETED");
        long goalsTotal      = goalsInProgress + goalsCompleted;

        return EmployeeDashboardResponse.builder()
                .employeeId(employeeId.toString())
                .employeeName(employee.getFullName())
                .employeeCode(employee.getEmployeeCode())
                .designation(employee.getDesignation().getTitle())
                .department(employee.getDepartment().getName())
                .dateOfJoining(employee.getDateOfJoining().format(DATE_FMT))
                .tenureLabel(tenureLabel)
                .profilePictureUrl(employee.getProfilePictureUrl())
                // today
                .checkedInToday(checkedInToday)
                .checkInTime(checkInTime)
                .checkOutTime(checkOutTime)
                .workingHoursToday(workingHoursToday)
                .attendanceStatusToday(attendanceStatus)
                // monthly attendance
                .workingDaysThisMonth(workingDays)
                .presentDaysThisMonth(presentDays)
                .absentDaysThisMonth(absentDays)
                .halfDaysThisMonth(halfDays)
                .lopDaysThisMonth(lopDays)
                .attendancePercentageThisMonth(attendPct)
                // leave
                .leaveBalances(leaveBalances)
                .pendingLeaveRequests(pendingLeaves)
                .nextLeave(null)
                // payroll
                .lastMonthNetSalary(lastPayroll != null
                        ? lastPayroll.getNetSalary() : BigDecimal.ZERO)
                .lastMonthGrossSalary(lastPayroll != null
                        ? lastPayroll.getGrossSalary() : BigDecimal.ZERO)
                .lastMonthDeductions(lastPayroll != null
                        ? lastPayroll.getTotalDeductions() : BigDecimal.ZERO)
                .lastPayslipPeriod(lastPayPeriod)
                .lastPayslipStatus(lastPayroll != null
                        ? lastPayroll.getStatus().name() : "NOT_GENERATED")
                // performance
                .averagePerformanceRating(
                        avgRating != null ? avgRating : BigDecimal.ZERO)
                .ratingLabel(resolveRatingLabel(avgRating))
                .totalReviewsCount(reviewCount)
                .latestQuarterRating(latestReview != null
                        ? latestReview.getRating() : BigDecimal.ZERO)
                .latestReviewPeriod(latestReview != null
                        ? "Q" + latestReview.getReviewQuarter()
                        + " " + latestReview.getReviewYear() : "—")
                // goals
                .goalsInProgress(goalsInProgress)
                .goalsCompleted(goalsCompleted)
                .goalsTotal(goalsTotal)
                .notifications(List.of())
                .build();
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private List<ManagerDashboardResponse.TeamMemberSummary> buildTeamSummaries(
            List<Employee> team, LocalDate today,
            LocalDate monthStart, boolean topPerformers) {

        return team.stream().map(e -> {
                    int wDays = countWorkingDays(monthStart, today);
                    long present = attendanceRepository.countByEmployeeAndDateRangeAndStatus(
                            e.getId(), monthStart, today, AttendanceStatus.PRESENT);
                    double attPct = wDays > 0
                            ? round2((double) present / wDays * 100) : 0;
                    BigDecimal rating = reviewRepository.averageRatingByEmployee(e.getId());

                    return ManagerDashboardResponse.TeamMemberSummary.builder()
                            .employeeId(e.getId().toString())
                            .employeeName(e.getFullName())
                            .employeeCode(e.getEmployeeCode())
                            .designation(e.getDesignation().getTitle())
                            .attendancePercentage(attPct)
                            .performanceRating(rating != null ? rating.doubleValue() : 0.0)
                            .status(e.getStatus().name())
                            .build();
                })
                .filter(s -> topPerformers
                        ? s.getPerformanceRating() >= 4.0
                        : s.getAttendancePercentage() < 75.0)
                .limit(5)
                .toList();
    }

    private int countWorkingDays(LocalDate from, LocalDate to) {
        return (int) from.datesUntil(to.plusDays(1))
                .filter(d -> d.getDayOfWeek() != DayOfWeek.SATURDAY
                        && d.getDayOfWeek() != DayOfWeek.SUNDAY)
                .count();
    }

    private String formatMinutes(Integer minutes) {
        if (minutes == null || minutes == 0) return "0h 00m";
        return String.format("%dh %02dm", minutes / 60, minutes % 60);
    }

    private double round2(double val) {
        return Math.round(val * 100.0) / 100.0;
    }

    private String resolveRatingLabel(BigDecimal rating) {
        if (rating == null) return "Not Rated";
        double r = rating.doubleValue();
        if (r >= 4.5) return "Outstanding";
        if (r >= 4.0) return "Exceeds Expectations";
        if (r >= 3.0) return "Meets Expectations";
        if (r >= 2.0) return "Needs Improvement";
        return "Unsatisfactory";
    }
}
