package com.nexushr.service;

import com.nexushr.domain.entity.Department;
import com.nexushr.domain.entity.Designation;
import com.nexushr.dto.request.designation.DesignationRequest;
import com.nexushr.dto.response.designation.DesignationResponse;
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
public class DesignationService {

    private final DesignationRepository designationRepository;
    private final DepartmentRepository  departmentRepository;

    @Transactional
    public DesignationResponse createDesignation(DesignationRequest request) {
        Department department = departmentRepository
                .findById(request.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Department not found: " + request.getDepartmentId()));

        if (designationRepository.existsByTitleAndDepartmentId(
                request.getTitle(), request.getDepartmentId())) {
            throw new DuplicateResourceException(
                    "Designation '" + request.getTitle()
                            + "' already exists in department: "
                            + department.getName());
        }

        Designation designation = Designation.builder()
                .title(request.getTitle())
                .grade(request.getGrade())
                .department(department)
                .active(true)
                .build();

        return toResponse(designationRepository.save(designation));
    }

    @Transactional(readOnly = true)
    public List<DesignationResponse> getAllDesignations() {
        return designationRepository.findAll()
                .stream()
                .filter(Designation::isActive)
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<DesignationResponse> getDesignationsByDepartment(UUID departmentId) {
        return designationRepository
                .findAllByDepartmentIdAndActiveTrue(departmentId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public DesignationResponse updateDesignation(UUID id,
                                                 DesignationRequest request) {
        Designation designation = designationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Designation not found: " + id));

        if (request.getTitle() != null) {
            designation.setTitle(request.getTitle());
        }
        if (request.getGrade() != null) {
            designation.setGrade(request.getGrade());
        }
        if (request.getDepartmentId() != null) {
            Department dept = departmentRepository
                    .findById(request.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Department not found: " + request.getDepartmentId()));
            designation.setDepartment(dept);
        }

        return toResponse(designationRepository.save(designation));
    }

    @Transactional
    public void deactivateDesignation(UUID id) {
        Designation designation = designationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Designation not found: " + id));
        designation.setActive(false);
        designationRepository.save(designation);
    }

    public DesignationResponse toResponse(Designation d) {
        return DesignationResponse.builder()
                .id(d.getId())
                .title(d.getTitle())
                .grade(d.getGrade())
                .active(d.isActive())
                .departmentId(d.getDepartment().getId())
                .departmentName(d.getDepartment().getName())
                .departmentCode(d.getDepartment().getCode())
                .build();
    }
}
