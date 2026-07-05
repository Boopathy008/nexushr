package com.nexushr.service;

import com.nexushr.domain.entity.Attendance;
import com.nexushr.domain.entity.Employee;
import com.nexushr.domain.enums.AttendanceStatus;
import com.nexushr.dto.response.attendance.AttendanceResponse;
import com.nexushr.dto.response.attendance.MonthlyAttendanceReport;
import com.nexushr.exception.BusinessRuleException;
import com.nexushr.exception.ResourceNotFoundException;
import com.nexushr.repository.AttendanceRepository;
import com.nexushr.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AttendanceService {

    private static final int STANDARD_WORKING_MINUTES = 480; // 8 hours

    private final AttendanceRepository attendanceRepository;
    private final EmployeeRepository employeeRepository;

    @Transactional
    public AttendanceResponse checkIn(UUID employeeId) {
        Employee employee = getActiveEmployee(employeeId);
        LocalDate today = LocalDate.now();

        if (attendanceRepository.existsByEmployeeIdAndAttendanceDate(employeeId, today)) {
            Attendance existing = attendanceRepository
                    .findByEmployeeIdAndAttendanceDate(employeeId, today).get();
            if (existing.getCheckInTime() != null) {
                throw new BusinessRuleException("Already checked in today at "
                        + existing.getCheckInTime().format(DateTimeFormatter.ofPattern("HH:mm")));
            }
            existing.setCheckInTime(LocalDateTime.now());
            existing.setStatus(AttendanceStatus.PRESENT);
            return toResponse(attendanceRepository.save(existing));
        }

        Attendance attendance = Attendance.builder()
                .employee(employee)
                .attendanceDate(today)
                .checkInTime(LocalDateTime.now())
                .status(AttendanceStatus.PRESENT)
                .workingMinutes(0)
                .build();

        return toResponse(attendanceRepository.save(attendance));
    }

    @Transactional
    public AttendanceResponse checkOut(UUID employeeId) {
        LocalDate today = LocalDate.now();
        Attendance attendance = attendanceRepository
                .findByEmployeeIdAndAttendanceDate(employeeId, today)
                .orElseThrow(() -> new BusinessRuleException(
                        "No check-in record found for today. Please check in first."));

        if (attendance.getCheckInTime() == null) {
            throw new BusinessRuleException("Cannot check out without checking in.");
        }
        if (attendance.getCheckOutTime() != null) {
            throw new BusinessRuleException("Already checked out today at "
                    + attendance.getCheckOutTime().format(DateTimeFormatter.ofPattern("HH:mm")));
        }

        LocalDateTime checkOut = LocalDateTime.now();
        int workedMinutes = (int) Duration.between(
                attendance.getCheckInTime(), checkOut).toMinutes();

        attendance.setCheckOutTime(checkOut);
        attendance.setWorkingMinutes(workedMinutes);
        attendance.setStatus(workedMinutes < STANDARD_WORKING_MINUTES / 2
                ? AttendanceStatus.HALF_DAY : AttendanceStatus.PRESENT);

        return toResponse(attendanceRepository.save(attendance));
    }

    @Transactional(readOnly = true)
    public AttendanceResponse getTodayAttendance(UUID employeeId) {
        getActiveEmployee(employeeId);
        return attendanceRepository
                .findByEmployeeIdAndAttendanceDate(employeeId, LocalDate.now())
                .map(this::toResponse)
                .orElse(null);
    }

    @Transactional(readOnly = true)
    public Page<AttendanceResponse> getAttendanceHistory(UUID employeeId,
                                                         int page, int size) {
        getActiveEmployee(employeeId);
        Pageable pageable = PageRequest.of(page, size,
                Sort.by("attendanceDate").descending());
        return attendanceRepository.findAllByEmployeeId(employeeId, pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public MonthlyAttendanceReport getMonthlyReport(UUID employeeId, int year, int month) {
        Employee employee = getActiveEmployee(employeeId);

        LocalDate from = LocalDate.of(year, month, 1);
        LocalDate to   = from.withDayOfMonth(from.lengthOfMonth());

        List<Attendance> records = attendanceRepository
                .findAllByEmployeeIdAndAttendanceDateBetweenOrderByAttendanceDateDesc(
                        employeeId, from, to);

        long presentDays  = records.stream().filter(a -> a.getStatus() == AttendanceStatus.PRESENT).count();
        long absentDays   = records.stream().filter(a -> a.getStatus() == AttendanceStatus.ABSENT).count();
        long halfDays     = records.stream().filter(a -> a.getStatus() == AttendanceStatus.HALF_DAY).count();
        long leaveDays    = records.stream().filter(a -> a.getStatus() == AttendanceStatus.ON_LEAVE).count();
        long holidayDays  = records.stream().filter(a -> a.getStatus() == AttendanceStatus.HOLIDAY).count();
        long totalMinutes = records.stream().mapToLong(a ->
                a.getWorkingMinutes() != null ? a.getWorkingMinutes() : 0).sum();

        int workingDays = (int)(from.datesUntil(to.plusDays(1))
                .filter(d -> d.getDayOfWeek() != DayOfWeek.SATURDAY
                        && d.getDayOfWeek() != DayOfWeek.SUNDAY)
                .count());

        double attendancePct = workingDays > 0
                ? (presentDays + (halfDays * 0.5)) / workingDays * 100 : 0;

        return MonthlyAttendanceReport.builder()
                .employeeName(employee.getFullName())
                .employeeCode(employee.getEmployeeCode())
                .year(year)
                .month(month)
                .totalWorkingDays(workingDays)
                .presentDays((int) presentDays)
                .absentDays((int) absentDays)
                .halfDays((int) halfDays)
                .leaveDays((int) leaveDays)
                .holidayDays((int) holidayDays)
                .totalWorkingMinutes(totalMinutes)
                .totalWorkingHours(formatMinutes(totalMinutes))
                .attendancePercentage(Math.round(attendancePct * 100.0) / 100.0)
                .dailyRecords(records.stream().map(this::toResponse).toList())
                .build();
    }

    private Employee getActiveEmployee(UUID employeeId) {
        return employeeRepository.findByIdWithDetails(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Employee not found: " + employeeId));
    }

    private String formatMinutes(long minutes) {
        return String.format("%dh %02dm", minutes / 60, minutes % 60);
    }

    public AttendanceResponse toResponse(Attendance a) {
        return AttendanceResponse.builder()
                .id(a.getId())
                .employeeId(a.getEmployee().getId())
                .employeeCode(a.getEmployee().getEmployeeCode())
                .employeeName(a.getEmployee().getFullName())
                .attendanceDate(a.getAttendanceDate())
                .checkInTime(a.getCheckInTime())
                .checkOutTime(a.getCheckOutTime())
                .status(a.getStatus())
                .workingMinutes(a.getWorkingMinutes())
                .workingHours(formatMinutes(a.getWorkingMinutes() != null
                        ? a.getWorkingMinutes() : 0))
                .notes(a.getNotes())
                .build();
    }
}
