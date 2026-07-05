package com.nexushr.controller;

import com.nexushr.dto.request.payroll.SalaryStructureRequest;
import com.nexushr.dto.response.payroll.PayslipResponse;
import com.nexushr.service.PayrollService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/payroll")
@RequiredArgsConstructor
@Tag(name = "Payroll Management")
public class PayrollController {

    private final PayrollService payrollService;

    @PostMapping("/{employeeId}/salary-structure")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Set or update salary structure for an employee")
    public ResponseEntity<PayslipResponse> setSalaryStructure(
            @PathVariable UUID employeeId,
            @Valid @RequestBody SalaryStructureRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(payrollService.setSalaryStructure(employeeId, request));
    }

    @PostMapping("/{employeeId}/generate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Generate payroll for an employee for a given month")
    public ResponseEntity<PayslipResponse> generate(
            @PathVariable UUID employeeId,
            @RequestParam int month,
            @RequestParam int year) {
        return ResponseEntity.ok(payrollService.generatePayroll(employeeId, month, year));
    }

    @PostMapping("/bulk-generate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Generate payroll for all active employees")
    public ResponseEntity<List<PayslipResponse>> bulkGenerate(
            @RequestParam int month,
            @RequestParam int year) {
        return ResponseEntity.ok(payrollService.generateBulkPayroll(month, year));
    }

    @GetMapping("/{employeeId}/payslip")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','EMPLOYEE')")
    @Operation(summary = "Get payslip for a specific month")
    public ResponseEntity<PayslipResponse> getPayslip(
            @PathVariable UUID employeeId,
            @RequestParam int month,
            @RequestParam int year) {
        return ResponseEntity.ok(payrollService.getPayslip(employeeId, month, year));
    }

    @GetMapping("/{employeeId}/history")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','EMPLOYEE')")
    @Operation(summary = "Get complete payroll history for an employee")
    public ResponseEntity<List<PayslipResponse>> getHistory(@PathVariable UUID employeeId) {
        return ResponseEntity.ok(payrollService.getPayrollHistory(employeeId));
    }

    @GetMapping("/period")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all payroll records for a period")
    public ResponseEntity<List<PayslipResponse>> getByPeriod(
            @RequestParam int month,
            @RequestParam int year) {
        return ResponseEntity.ok(payrollService.getPayrollByPeriod(month, year));
    }
}
