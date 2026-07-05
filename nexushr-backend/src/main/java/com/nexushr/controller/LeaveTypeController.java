package com.nexushr.controller;

import com.nexushr.domain.entity.LeaveType;
import com.nexushr.repository.LeaveTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/leave-types")
@RequiredArgsConstructor
public class LeaveTypeController {
    private final LeaveTypeRepository leaveTypeRepository;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','EMPLOYEE')")
    public ResponseEntity<List<LeaveType>> getAll() {
        return ResponseEntity.ok(leaveTypeRepository.findAllByActiveTrue());
    }
}
