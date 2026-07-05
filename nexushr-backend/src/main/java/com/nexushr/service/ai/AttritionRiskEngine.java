package com.nexushr.service.ai;

import com.nexushr.domain.entity.Employee;
import com.nexushr.domain.enums.AttendanceStatus;
import com.nexushr.repository.AttendanceRepository;
import com.nexushr.repository.LeaveRequestRepository;
import com.nexushr.repository.PerformanceReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class AttritionRiskEngine {

    private final AttendanceRepository     attendanceRepository;
    private final LeaveRequestRepository   leaveRequestRepository;
    private final PerformanceReviewRepository reviewRepository;

    public record AttritionResult(int score, String level, List<String> factors) {}

    public AttritionResult compute(Employee employee) {
        int score = 0;
        List<String> factors = new ArrayList<>();

        // ── Attendance rate (last 90 days) ────────────────────────────────────
        LocalDate today    = LocalDate.now();
        LocalDate from90   = today.minusDays(90);
        int workingDays90  = countWorkingDays(from90, today);

        long presentCount = attendanceRepository
                .countByEmployeeAndDateRangeAndStatus(
                        employee.getId(), from90, today, AttendanceStatus.PRESENT);
        long halfDayCount = attendanceRepository
                .countByEmployeeAndDateRangeAndStatus(
                        employee.getId(), from90, today, AttendanceStatus.HALF_DAY);

        double effectivePresent = presentCount + (halfDayCount * 0.5);
        double attendanceRate   = workingDays90 > 0
                ? (effectivePresent / workingDays90) * 100.0 : 100.0;

        if (attendanceRate < 70) {
            score += 35;
            factors.add(String.format(
                    "Very low attendance rate: %.1f%% (last 90 days)", attendanceRate));
        } else if (attendanceRate < 85) {
            score += 20;
            factors.add(String.format(
                    "Below-average attendance: %.1f%% (last 90 days)", attendanceRate));
        }

        // ── LOP days (last 30 days) ───────────────────────────────────────────
        LocalDate from30 = today.minusDays(30);
        Long totalMinutes30 = attendanceRepository
                .sumWorkingMinutesForMonth(
                        employee.getId(),
                        today.getYear(),
                        today.getMonthValue());

        int workingDays30 = countWorkingDays(from30, today);
        long present30    = attendanceRepository.countByEmployeeAndDateRangeAndStatus(
                employee.getId(), from30, today, AttendanceStatus.PRESENT);
        int lopDays       = (int) Math.max(0, workingDays30 - present30);

        if (lopDays > 3) {
            score += 15;
            factors.add("High LOP days in current month: " + lopDays + " days");
        }

        // ── Pending leave requests ────────────────────────────────────────────
        long pendingLeaves = leaveRequestRepository.countByEmployeeIdAndStatus(
                employee.getId(),
                com.nexushr.domain.enums.LeaveStatus.PENDING);
        if (pendingLeaves > 2) {
            score += 10;
            factors.add("Multiple pending leave requests: " + pendingLeaves);
        }

        // ── Tenure ────────────────────────────────────────────────────────────
        long tenureMonths = ChronoUnit.MONTHS.between(
                employee.getDateOfJoining(), today);
        if (tenureMonths < 6) {
            score += 15;
            factors.add("New employee: tenure less than 6 months (" + tenureMonths + " months)");
        }

        // ── Performance ───────────────────────────────────────────────────────
        java.math.BigDecimal avgRating = reviewRepository
                .averageRatingByEmployee(employee.getId());
        int reviewCount = reviewRepository
                .countByEmployeeId(employee.getId());

        if (reviewCount == 0) {
            score += 10;
            factors.add("No performance reviews on record");
        } else if (avgRating != null
                && avgRating.compareTo(java.math.BigDecimal.valueOf(2.5)) < 0) {
            score += 20;
            factors.add(String.format(
                    "Low average performance rating: %.1f / 5.0", avgRating));
        }

        score = Math.min(score, 100);

        String level;
        if      (score >= 70) level = "CRITICAL";
        else if (score >= 45) level = "HIGH";
        else if (score >= 20) level = "MEDIUM";
        else                  level = "LOW";

        if (factors.isEmpty()) {
            factors.add("No significant attrition risk indicators detected");
        }

        return new AttritionResult(score, level, List.copyOf(factors));
    }

    private int countWorkingDays(LocalDate from, LocalDate to) {
        return (int) from.datesUntil(to.plusDays(1))
                .filter(d -> d.getDayOfWeek() != java.time.DayOfWeek.SATURDAY
                        && d.getDayOfWeek() != java.time.DayOfWeek.SUNDAY)
                .count();
    }
}
