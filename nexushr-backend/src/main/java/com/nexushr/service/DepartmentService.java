package com.nexushr.service;

import com.nexushr.domain.entity.Department;
import com.nexushr.dto.request.department.DepartmentRequest;
import com.nexushr.dto.response.department.DepartmentResponse;
import com.nexushr.exception.DuplicateResourceException;
import com.nexushr.exception.ResourceNotFoundException;
import com.nexushr.repository.DepartmentRepository;
import com.nexushr.repository.DesignationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final DesignationRepository designationRepository;

    @Transactional
    public DepartmentResponse createDepartment(DepartmentRequest request) {
        if (departmentRepository.existsByName(request.getName()))
            throw new DuplicateResourceException("Department already exists: " + request.getName());
        if (departmentRepository.existsByCode(request.getCode()))
            throw new DuplicateResourceException("Department code already exists: " + request.getCode());

        Department dept = Department.builder()
                .name(request.getName())
                .code(request.getCode().toUpperCase())
                .description(request.getDescription())
                .active(true)
                .build();
        return toResponse(departmentRepository.save(dept));
    }

    @Transactional(readOnly = true)
    public List<DepartmentResponse> getAllDepartments() {
        return departmentRepository.findAllActiveWithDesignations()
                .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public DepartmentResponse getDepartment(UUID id) {
        return toResponse(departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found: " + id)));
    }

    @Transactional
    public DepartmentResponse updateDepartment(UUID id, DepartmentRequest request) {
        Department dept = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found: " + id));
        dept.setName(request.getName());
        dept.setDescription(request.getDescription());
        return toResponse(departmentRepository.save(dept));
    }

    private DepartmentResponse toResponse(Department d) {
        return DepartmentResponse.builder()
                .id(d.getId())
                .name(d.getName())
                .code(d.getCode())
                .description(d.getDescription())
                .active(d.isActive())
                .employeeCount(d.getEmployees() != null ? d.getEmployees().size() : 0)
                .build();
    }
}
