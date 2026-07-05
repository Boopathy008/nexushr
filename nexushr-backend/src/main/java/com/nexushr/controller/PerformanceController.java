package com.nexushr.controller;

import com.nexushr.dto.request.performance.GoalRequest;
import com.nexushr.dto.request.performance.ReviewRequest;
import com.nexushr.dto.response.performance.GoalResponse;
import com.nexushr.dto.response.performance.ReviewResponse;
import com.nexushr.service.PerformanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/performance")
@RequiredArgsConstructor
@Tag(name = "Performance Management")
public class PerformanceController {

    private final PerformanceService performanceService;

    // ── Reviews ───────────────────────────────────────────────────────────────

    @PostMapping("/reviews")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @Operation(summary = "Submit a quarterly performance review")
    public ResponseEntity<ReviewResponse> submitReview(
            @RequestParam UUID reviewerId,
            @Valid @RequestBody ReviewRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(performanceService.submitReview(reviewerId, request));
    }

    @GetMapping("/{employeeId}/reviews")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','EMPLOYEE')")
    @Operation(summary = "Get all reviews for an employee")
    public ResponseEntity<List<ReviewResponse>> getReviews(
            @PathVariable UUID employeeId) {
        return ResponseEntity.ok(
                performanceService.getEmployeeReviews(employeeId));
    }

    @GetMapping("/{employeeId}/rating")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','EMPLOYEE')")
    @Operation(summary = "Get average performance rating for an employee")
    public ResponseEntity<BigDecimal> getAverageRating(
            @PathVariable UUID employeeId) {
        return ResponseEntity.ok(
                performanceService.getAverageRating(employeeId));
    }

    // ── Goals ─────────────────────────────────────────────────────────────────

    @PostMapping("/goals")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @Operation(summary = "Create a performance goal for an employee")
    public ResponseEntity<GoalResponse> createGoal(
            @Valid @RequestBody GoalRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(performanceService.createGoal(request));
    }

    @GetMapping("/{employeeId}/goals")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','EMPLOYEE')")
    @Operation(summary = "Get all goals for an employee")
    public ResponseEntity<List<GoalResponse>> getGoals(
            @PathVariable UUID employeeId) {
        return ResponseEntity.ok(
                performanceService.getEmployeeGoals(employeeId));
    }

    @GetMapping("/{employeeId}/goals/quarter")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','EMPLOYEE')")
    @Operation(summary = "Get goals for a specific quarter")
    public ResponseEntity<List<GoalResponse>> getGoalsByQuarter(
            @PathVariable UUID employeeId,
            @RequestParam int year,
            @RequestParam int quarter) {
        return ResponseEntity.ok(
                performanceService.getGoalsByQuarter(employeeId, year, quarter));
    }

    @PatchMapping("/goals/{goalId}/status")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','EMPLOYEE')")
    @Operation(summary = "Update goal status (IN_PROGRESS / COMPLETED / CANCELLED)")
    public ResponseEntity<GoalResponse> updateGoalStatus(
            @PathVariable UUID goalId,
            @RequestParam String status) {
        return ResponseEntity.ok(
                performanceService.updateGoalStatus(goalId, status));
    }
}
