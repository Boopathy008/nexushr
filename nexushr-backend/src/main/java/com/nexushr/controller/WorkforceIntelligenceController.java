package com.nexushr.controller;

import com.nexushr.dto.response.ai.DepartmentIntelligenceResponse;
import com.nexushr.dto.response.ai.WorkforceIntelligenceResponse;
import com.nexushr.service.ai.WorkforceIntelligenceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/intelligence")
@RequiredArgsConstructor
@Tag(name = "AI Workforce Intelligence")
public class WorkforceIntelligenceController {

    private final WorkforceIntelligenceService intelligenceService;

    private final com.nexushr.repository.EmployeeRepository employeeRepository;

    @GetMapping("/employee/{identifier}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @Operation(summary = "Full workforce intelligence report for one employee")
    public ResponseEntity<WorkforceIntelligenceResponse> analyzeEmployee(
            @PathVariable String identifier) {
        UUID employeeId;
        try {
            employeeId = UUID.fromString(identifier);
        } catch (IllegalArgumentException e) {
            employeeId = employeeRepository.findByEmployeeCode(identifier)
                    .map(com.nexushr.domain.entity.Employee::getId)
                    .orElseThrow(() -> new com.nexushr.exception.ResourceNotFoundException("Employee not found with code: " + identifier));
        }
        return ResponseEntity.ok(intelligenceService.analyzeEmployee(employeeId));
    }

    @GetMapping("/department/{departmentId}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @Operation(summary = "Department-level workforce intelligence report")
    public ResponseEntity<DepartmentIntelligenceResponse> analyzeDepartment(
            @PathVariable UUID departmentId) {
        return ResponseEntity.ok(intelligenceService.analyzeDepartment(departmentId));
    }

    @GetMapping("/high-risk")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "List all HIGH and CRITICAL attrition risk employees")
    public ResponseEntity<List<WorkforceIntelligenceResponse>> getHighRisk() {
        return ResponseEntity.ok(intelligenceService.getHighAttritionRiskEmployees());
    }
}
