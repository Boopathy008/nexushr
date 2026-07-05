package com.nexushr.controller;

import com.nexushr.dto.request.leave.*;
import com.nexushr.dto.response.leave.*;
import com.nexushr.service.LeaveService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.Year;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/leaves")
@RequiredArgsConstructor
@Tag(name = "Leave Management")
public class LeaveController {

    private final LeaveService leaveService;

    @PostMapping("/{employeeId}/apply")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','EMPLOYEE')")
    @Operation(summary = "Apply for leave")
    public ResponseEntity<LeaveResponse> apply(
            @PathVariable UUID employeeId,
            @Valid @RequestBody ApplyLeaveRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(leaveService.applyLeave(employeeId, request));
    }

    @PatchMapping("/{leaveRequestId}/cancel")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','EMPLOYEE')")
    @Operation(summary = "Cancel a pending or approved leave")
    public ResponseEntity<LeaveResponse> cancel(
            @PathVariable UUID leaveRequestId,
            @RequestParam UUID employeeId) {
        return ResponseEntity.ok(leaveService.cancelLeave(leaveRequestId, employeeId));
    }

    @PatchMapping("/{leaveRequestId}/decide")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @Operation(summary = "Approve or reject a leave request")
    public ResponseEntity<LeaveResponse> decide(
            @PathVariable UUID leaveRequestId,
            @RequestParam UUID managerId,
            @Valid @RequestBody LeaveDecisionRequest request) {
        return ResponseEntity.ok(
                leaveService.decideLeave(leaveRequestId, managerId, request));
    }

    @GetMapping("/{employeeId}/history")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','EMPLOYEE')")
    @Operation(summary = "Get leave history for an employee")
    public ResponseEntity<Page<LeaveResponse>> getHistory(
            @PathVariable UUID employeeId,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(leaveService.getMyLeaveHistory(employeeId, page, size));
    }

    @GetMapping("/pending")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @Operation(summary = "List all pending leave requests")
    public ResponseEntity<Page<LeaveResponse>> getPending(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(leaveService.getPendingLeaves(page, size));
    }

    @GetMapping("/department/{departmentId}/pending")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @Operation(summary = "Get pending leaves for a department")
    public ResponseEntity<List<LeaveResponse>> getDepartmentPending(
            @PathVariable UUID departmentId) {
        return ResponseEntity.ok(leaveService.getDepartmentPendingLeaves(departmentId));
    }

    @GetMapping("/{employeeId}/balance")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','EMPLOYEE')")
    @Operation(summary = "Get leave balance for an employee")
    public ResponseEntity<LeaveBalanceResponse> getBalance(
            @PathVariable UUID employeeId,
            @RequestParam(defaultValue = "0") int year) {
        int targetYear = year == 0 ? Year.now().getValue() : year;
        return ResponseEntity.ok(leaveService.getLeaveBalance(employeeId, targetYear));
    }
}
