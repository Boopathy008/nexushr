package com.nexushr.controller;

import com.nexushr.dto.response.attendance.AttendanceResponse;
import com.nexushr.dto.response.attendance.MonthlyAttendanceReport;
import com.nexushr.service.AttendanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/attendance")
@RequiredArgsConstructor
@Tag(name = "Attendance Management")
public class AttendanceController {

    private final AttendanceService attendanceService;

    @PostMapping("/{employeeId}/check-in")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','EMPLOYEE')")
    @Operation(summary = "Record employee check-in")
    public ResponseEntity<AttendanceResponse> checkIn(@PathVariable UUID employeeId) {
        return ResponseEntity.ok(attendanceService.checkIn(employeeId));
    }

    @PostMapping("/{employeeId}/check-out")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','EMPLOYEE')")
    @Operation(summary = "Record employee check-out")
    public ResponseEntity<AttendanceResponse> checkOut(@PathVariable UUID employeeId) {
        return ResponseEntity.ok(attendanceService.checkOut(employeeId));
    }

    @GetMapping("/{employeeId}/today")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','EMPLOYEE')")
    @Operation(summary = "Get today's attendance record")
    public ResponseEntity<AttendanceResponse> getToday(@PathVariable UUID employeeId) {
        return ResponseEntity.ok(attendanceService.getTodayAttendance(employeeId));
    }

    @GetMapping("/{employeeId}/history")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','EMPLOYEE')")
    @Operation(summary = "Get paginated attendance history")
    public ResponseEntity<Page<AttendanceResponse>> getHistory(
            @PathVariable UUID employeeId,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "30") int size) {
        return ResponseEntity.ok(attendanceService.getAttendanceHistory(employeeId, page, size));
    }

    @GetMapping("/{employeeId}/report/monthly")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','EMPLOYEE')")
    @Operation(summary = "Get monthly attendance report")
    public ResponseEntity<MonthlyAttendanceReport> getMonthlyReport(
            @PathVariable UUID employeeId,
            @RequestParam int year,
            @RequestParam int month) {
        return ResponseEntity.ok(attendanceService.getMonthlyReport(employeeId, year, month));
    }
}
