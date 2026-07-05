package com.nexushr.controller;

import com.nexushr.dto.request.designation.DesignationRequest;
import com.nexushr.dto.response.designation.DesignationResponse;
import com.nexushr.service.DesignationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/designations")
@RequiredArgsConstructor
@Tag(name = "Designation Management")
public class DesignationController {

    private final DesignationService designationService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new designation under a department")
    public ResponseEntity<DesignationResponse> create(
            @Valid @RequestBody DesignationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(designationService.createDesignation(request));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','EMPLOYEE')")
    @Operation(summary = "Get all active designations")
    public ResponseEntity<List<DesignationResponse>> getAll() {
        return ResponseEntity.ok(designationService.getAllDesignations());
    }

    @GetMapping("/department/{departmentId}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','EMPLOYEE')")
    @Operation(summary = "Get all designations for a specific department")
    public ResponseEntity<List<DesignationResponse>> getByDepartment(
            @PathVariable UUID departmentId) {
        return ResponseEntity.ok(
                designationService.getDesignationsByDepartment(departmentId));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update a designation")
    public ResponseEntity<DesignationResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody DesignationRequest request) {
        return ResponseEntity.ok(
                designationService.updateDesignation(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Deactivate a designation")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        designationService.deactivateDesignation(id);
        return ResponseEntity.noContent().build();
    }
}
