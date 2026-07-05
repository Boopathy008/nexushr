package com.nexushr.service.ai;

import com.nexushr.domain.entity.Employee;
import com.nexushr.domain.enums.AttendanceStatus;
import com.nexushr.domain.enums.LeaveStatus;
import com.nexushr.dto.response.ai.DepartmentIntelligenceResponse;
import com.nexushr.dto.response.ai.WorkforceIntelligenceResponse;
import com.nexushr.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WorkforceIntelligenceService {

    private final EmployeeRepository          employeeRepository;
    private final DepartmentRepository        departmentRepository;
    private final AttendanceRepository        attendanceRepository;
    private final LeaveBalanceRepository      leaveBalanceRepository;
    private final LeaveRequestRepository      leaveRequestRepository;
    private final PerformanceReviewRepository reviewRepository;
    private final AttritionRiskEngine         attritionEngine;
    private final EngagementScoreEngine       engagementEngine;

    // ── Single employee ───────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public WorkforceIntelligenceResponse analyzeEmployee(UUID employeeId) {
        Employee employee = employeeRepository.findByIdWithDetails(employeeId)
                .orElseThrow(() -> new com.nexushr.exception.ResourceNotFoundException(
                        "Employee not found: " + employeeId));
        return buildInsight(employee);
    }

    // ── Department view ───────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public DepartmentIntelligenceResponse analyzeDepartment(UUID departmentId) {
        var dept = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new com.nexushr.exception.ResourceNotFoundException(
                        "Department not found: " + departmentId));

        List<Employee> employees = employeeRepository.findAllWithFilters(
                com.nexushr.domain.enums.EmployeeStatus.ACTIVE,
                departmentId, Pageable.unpaged()).getContent();

        List<WorkforceIntelligenceResponse> insights = employees.stream()
                .map(this::buildInsight).toList();

        double avgRisk       = insights.stream()
                .mapToInt(WorkforceIntelligenceResponse::getAttritionRiskScore)
                .average().orElse(0);
        double avgEngagement = insights.stream()
                .mapToInt(WorkforceIntelligenceResponse::getEngagementScore)
                .average().orElse(0);
        double avgAttendance = insights.stream()
                .mapToDouble(WorkforceIntelligenceResponse::getAttendanceRatePercent)
                .average().orElse(0);

        long highRisk     = insights.stream()
                .filter(i -> "HIGH".equals(i.getAttritionRiskLevel())).count();
        long criticalRisk = insights.stream()
                .filter(i -> "CRITICAL".equals(i.getAttritionRiskLevel())).count();

        return DepartmentIntelligenceResponse.builder()
                .departmentId(dept.getId())
                .departmentName(dept.getName())
                .totalEmployees(employees.size())
                .avgAttritionRisk(round2(avgRisk))
                .avgEngagementScore(round2(avgEngagement))
                .avgAttendanceRate(round2(avgAttendance))
                .highRiskCount((int) highRisk)
                .criticalRiskCount((int) criticalRisk)
                .employeeInsights(insights)
                .build();
    }

    // ── Company-wide high-risk list ───────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<WorkforceIntelligenceResponse> getHighAttritionRiskEmployees() {
        return employeeRepository.findAllWithFilters(
                        com.nexushr.domain.enums.EmployeeStatus.ACTIVE,
                        null, Pageable.unpaged())
                .getContent()
                .stream()
                .map(this::buildInsight)
                .filter(i -> "HIGH".equals(i.getAttritionRiskLevel())
                        || "CRITICAL".equals(i.getAttritionRiskLevel()))
                .sorted(Comparator.comparingInt(
                        WorkforceIntelligenceResponse::getAttritionRiskScore).reversed())
                .toList();
    }

    // ── Builder ───────────────────────────────────────────────────────────────

    private WorkforceIntelligenceResponse buildInsight(Employee employee) {
        LocalDate today  = LocalDate.now();
        LocalDate from90 = today.minusDays(90);
        LocalDate from30 = today.minusDays(30);

        // Attrition + engagement
        AttritionRiskEngine.AttritionResult attrition =
                attritionEngine.compute(employee);
        EngagementScoreEngine.EngagementResult engagement =
                engagementEngine.compute(employee);

        // Attendance stats (30 days)
        int workingDays30 = countWorkingDays(from30, today);
        long present30 = attendanceRepository.countByEmployeeAndDateRangeAndStatus(
                employee.getId(), from30, today, AttendanceStatus.PRESENT);

        // Attendance rate (90 days)
        int workingDays90 = countWorkingDays(from90, today);
        long present90 = attendanceRepository.countByEmployeeAndDateRangeAndStatus(
                employee.getId(), from90, today, AttendanceStatus.PRESENT);
        long half90 = attendanceRepository.countByEmployeeAndDateRangeAndStatus(
                employee.getId(), from90, today, AttendanceStatus.HALF_DAY);
        double attendanceRate = workingDays90 > 0
                ? ((present90 + half90 * 0.5) / workingDays90) * 100.0 : 100.0;

        // LOP last 3 months
        LocalDate from3m = today.minusMonths(3);
        long present3m = attendanceRepository.countByEmployeeAndDateRangeAndStatus(
                employee.getId(), from3m, today, AttendanceStatus.PRESENT);
        long leave3m = attendanceRepository.countByEmployeeAndDateRangeAndStatus(
                employee.getId(), from3m, today, AttendanceStatus.ON_LEAVE);
        int workingDays3m = countWorkingDays(from3m, today);
        int lopDays = (int) Math.max(0, workingDays3m - present3m - leave3m);

        // Leave balance
        var balances = leaveBalanceRepository
                .findBalancesWithLeaveType(employee.getId(), today.getYear());
        BigDecimal totalQuota = balances.stream()
                .map(b -> b.getTotalDays()).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalUsed = balances.stream()
                .map(b -> b.getUsedDays()).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal usedPercent = totalQuota.compareTo(BigDecimal.ZERO) > 0
                ? totalUsed.divide(totalQuota, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(1, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        long pendingLeaves = leaveRequestRepository.countByEmployeeIdAndStatus(
                employee.getId(), LeaveStatus.PENDING);

        // Performance
        BigDecimal avgRating = reviewRepository.averageRatingByEmployee(employee.getId());
        int reviewCount      = reviewRepository.countByEmployeeId(employee.getId());

        // Tenure
        long tenureMonths = ChronoUnit.MONTHS.between(employee.getDateOfJoining(), today);
        String tenureLabel = tenureMonths < 12
                ? tenureMonths + " months"
                : (tenureMonths / 12) + " yr " + (tenureMonths % 12) + " mo";

        // Recommendations
        List<String> recommendations = buildRecommendations(
                attrition, engagement, attendanceRate, lopDays,
                avgRating, tenureMonths);

        return WorkforceIntelligenceResponse.builder()
                .employeeId(employee.getId())
                .employeeName(employee.getFullName())
                .employeeCode(employee.getEmployeeCode())
                .department(employee.getDepartment().getName())
                .designation(employee.getDesignation().getTitle())
                .attritionRiskScore(attrition.score())
                .attritionRiskLevel(attrition.level())
                .attritionRiskFactors(attrition.factors())
                .engagementScore(engagement.score())
                .engagementLevel(engagement.level())
                .engagementFactors(engagement.factors())
                .attendanceRatePercent(round2(attendanceRate))
                .lopDaysLast3Months(lopDays)
                .presentDaysLast30((int) present30)
                .leaveBalanceUsedPercent(usedPercent)
                .pendingLeaveRequests(pendingLeaves)
                .averagePerformanceRating(avgRating != null
                        ? avgRating.setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO)
                .totalReviewsCount(reviewCount)
                .tenureInMonths(tenureMonths)
                .tenureLabel(tenureLabel)
                .recommendations(recommendations)
                .build();
    }

    private List<String> buildRecommendations(
            AttritionRiskEngine.AttritionResult attrition,
            EngagementScoreEngine.EngagementResult engagement,
            double attendanceRate, int lopDays,
            BigDecimal avgRating, long tenureMonths) {

        List<String> recs = new ArrayList<>();

        if ("CRITICAL".equals(attrition.level()) || "HIGH".equals(attrition.level())) {
            recs.add("Schedule a 1-on-1 retention conversation with the employee's manager");
            recs.add("Review compensation and benefits alignment with market rates");
        }
        if (attendanceRate < 80) {
            recs.add("Investigate attendance pattern — consider flexible work arrangements");
        }
        if (lopDays > 3) {
            recs.add("Review workload and personal circumstances causing unplanned absences");
        }
        if ("DISENGAGED".equals(engagement.level())) {
            recs.add("Assign meaningful projects to rebuild motivation and purpose");
            recs.add("Enroll employee in skill development or leadership program");
        }
        if (avgRating != null && avgRating.doubleValue() < 2.5) {
            recs.add("Create a Performance Improvement Plan (PIP) with clear targets");
        }
        if (tenureMonths < 6) {
            recs.add("Assign a senior mentor — structured onboarding reduces early attrition");
        }
        if ("HIGHLY_ENGAGED".equals(engagement.level())) {
            recs.add("Consider for fast-track promotion or expanded responsibilities");
        }
        if (recs.isEmpty()) {
            recs.add("Employee is performing well — maintain regular check-ins");
        }
        return List.copyOf(recs);
    }

    private int countWorkingDays(LocalDate from, LocalDate to) {
        return (int) from.datesUntil(to.plusDays(1))
                .filter(d -> d.getDayOfWeek() != java.time.DayOfWeek.SATURDAY
                        && d.getDayOfWeek() != java.time.DayOfWeek.SUNDAY)
                .count();
    }

    private double round2(double val) {
        return Math.round(val * 100.0) / 100.0;
    }
}
