package com.nexushr.service.ai;

import com.nexushr.domain.entity.Employee;
import com.nexushr.domain.enums.AttendanceStatus;
import com.nexushr.repository.AttendanceRepository;
import com.nexushr.repository.LeaveBalanceRepository;
import com.nexushr.repository.PerformanceReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class EngagementScoreEngine {

    private final AttendanceRepository      attendanceRepository;
    private final LeaveBalanceRepository    leaveBalanceRepository;
    private final PerformanceReviewRepository reviewRepository;

    public record EngagementResult(int score, String level, List<String> factors) {}

    public EngagementResult compute(Employee employee) {
        int score = 0;
        List<String> factors = new ArrayList<>();

        LocalDate today  = LocalDate.now();
        LocalDate from90 = today.minusDays(90);
        int workingDays  = countWorkingDays(from90, today);

        // ── Attendance rate ───────────────────────────────────────────────────
        long present = attendanceRepository.countByEmployeeAndDateRangeAndStatus(
                employee.getId(), from90, today, AttendanceStatus.PRESENT);
        long half    = attendanceRepository.countByEmployeeAndDateRangeAndStatus(
                employee.getId(), from90, today, AttendanceStatus.HALF_DAY);

        double effectivePresent = present + (half * 0.5);
        double attendanceRate   = workingDays > 0
                ? (effectivePresent / workingDays) * 100.0 : 100.0;

        if (attendanceRate >= 95) {
            score += 30;
            factors.add(String.format("Excellent attendance: %.1f%%", attendanceRate));
        } else if (attendanceRate >= 85) {
            score += 20;
            factors.add(String.format("Good attendance: %.1f%%", attendanceRate));
        } else if (attendanceRate >= 75) {
            score += 10;
            factors.add(String.format("Average attendance: %.1f%%", attendanceRate));
        }

        // ── Performance rating ────────────────────────────────────────────────
        BigDecimal avgRating = reviewRepository.averageRatingByEmployee(employee.getId());
        if (avgRating != null) {
            double rating = avgRating.doubleValue();
            if (rating >= 4.5) {
                score += 30;
                factors.add(String.format("Outstanding performance rating: %.1f", rating));
            } else if (rating >= 3.5) {
                score += 20;
                factors.add(String.format("Strong performance rating: %.1f", rating));
            } else if (rating >= 2.5) {
                score += 10;
                factors.add(String.format("Satisfactory performance rating: %.1f", rating));
            }
        }

        // ── Leave balance utilisation (healthy = using annual leave) ──────────
        int year = today.getYear();
        var balances = leaveBalanceRepository.findBalancesWithLeaveType(
                employee.getId(), year);

        if (!balances.isEmpty()) {
            BigDecimal totalQuota = balances.stream()
                    .map(b -> b.getTotalDays())
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal totalUsed = balances.stream()
                    .map(b -> b.getUsedDays())
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            if (totalQuota.compareTo(BigDecimal.ZERO) > 0) {
                double utilisation = totalUsed.divide(totalQuota, 4,
                        java.math.RoundingMode.HALF_UP).doubleValue() * 100;
                if (utilisation >= 30 && utilisation <= 80) {
                    score += 15;
                    factors.add(String.format(
                            "Healthy leave utilisation: %.1f%%", utilisation));
                }
            }
        }

        // ── Tenure ────────────────────────────────────────────────────────────
        long tenureMonths = ChronoUnit.MONTHS.between(employee.getDateOfJoining(), today);
        if (tenureMonths >= 24) {
            score += 15;
            factors.add("Long-term employee: " + tenureMonths + " months tenure");
        } else if (tenureMonths >= 12) {
            score += 8;
            factors.add("Established employee: " + tenureMonths + " months tenure");
        }

        // ── Zero LOP in last 3 months ─────────────────────────────────────────
        LocalDate from3m = today.minusMonths(3);
        int workingDays3m = countWorkingDays(from3m, today);
        long present3m = attendanceRepository.countByEmployeeAndDateRangeAndStatus(
                employee.getId(), from3m, today, AttendanceStatus.PRESENT);
        long onLeave3m = attendanceRepository.countByEmployeeAndDateRangeAndStatus(
                employee.getId(), from3m, today, AttendanceStatus.ON_LEAVE);
        long accounted = present3m + onLeave3m;

        if (accounted >= workingDays3m) {
            score += 10;
            factors.add("No loss-of-pay days in the last 3 months");
        }

        score = Math.min(score, 100);

        String level;
        if      (score >= 75) level = "HIGHLY_ENGAGED";
        else if (score >= 50) level = "ENGAGED";
        else if (score >= 25) level = "MODERATE";
        else                  level = "DISENGAGED";

        if (factors.isEmpty()) {
            factors.add("Insufficient data to determine engagement level");
        }

        return new EngagementResult(score, level, List.copyOf(factors));
    }

    private int countWorkingDays(LocalDate from, LocalDate to) {
        return (int) from.datesUntil(to.plusDays(1))
                .filter(d -> d.getDayOfWeek() != java.time.DayOfWeek.SATURDAY
                        && d.getDayOfWeek() != java.time.DayOfWeek.SUNDAY)
                .count();
    }
}
