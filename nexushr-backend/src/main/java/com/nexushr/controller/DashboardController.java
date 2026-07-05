package com.nexushr.controller;

import com.nexushr.dto.response.dashboard.*;
import com.nexushr.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Admin dashboard — company-wide KPIs and statistics")
    public ResponseEntity<AdminDashboardResponse> getAdminDashboard() {
        return ResponseEntity.ok(dashboardService.getAdminDashboard());
    }

    @GetMapping("/manager/{managerId}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @Operation(summary = "Manager dashboard — team overview and pending actions")
    public ResponseEntity<ManagerDashboardResponse> getManagerDashboard(
            @PathVariable UUID managerId) {
        return ResponseEntity.ok(
                dashboardService.getManagerDashboard(managerId));
    }

    @GetMapping("/employee/{employeeId}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','EMPLOYEE')")
    @Operation(summary = "Employee dashboard — personal attendance, leave, payroll, performance")
    public ResponseEntity<EmployeeDashboardResponse> getEmployeeDashboard(
            @PathVariable UUID employeeId) {
        return ResponseEntity.ok(
                dashboardService.getEmployeeDashboard(employeeId));
    }
}
