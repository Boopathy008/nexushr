package com.nexushr.service;

import com.nexushr.domain.entity.*;
import com.nexushr.domain.enums.EmployeeStatus;
import com.nexushr.dto.request.employee.*;
import com.nexushr.dto.response.employee.EmployeeResponse;
import com.nexushr.exception.*;
import com.nexushr.repository.*;
import com.nexushr.repository.specification.EmployeeSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final DesignationRepository designationRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public EmployeeResponse createEmployee(CreateEmployeeRequest request) {
        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Department not found: " + request.getDepartmentId()));

        Designation designation = designationRepository.findById(request.getDesignationId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Designation not found: " + request.getDesignationId()));

        // If a User account already exists with this email (e.g. seeded manager/admin),
        // link the new Employee profile to that existing user instead of failing.
        User user = userRepository.findByEmail(request.getEmail()).orElse(null);

        if (user != null) {
            // Existing user — make sure they don't already have an employee profile
            if (employeeRepository.findByUserId(user.getId()).isPresent()) {
                throw new DuplicateResourceException(
                        "An employee profile already exists for: " + request.getEmail());
            }
            log.info("Linking employee profile to existing user: {}", user.getEmail());
        } else {
            // Brand-new user — check username uniqueness too
            if (userRepository.existsByUsername(request.getUsername())) {
                throw new DuplicateResourceException(
                        "Username already exists: " + request.getUsername());
            }
                String defaultPassword = request.getFirstName().toLowerCase() + "123";
            user = User.builder()
                    .username(request.getUsername())
                    .email(request.getEmail())
                    .password(passwordEncoder.encode(defaultPassword))
                    .role(request.getRole())
                    .active(true)
                    .build();
            userRepository.save(user);
        }

        Employee employee = Employee.builder()
                .user(user)
                .department(department)
                .designation(designation)
                .employeeCode(generateEmployeeCode(department))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phone(request.getPhone())
                .gender(request.getGender())
                .dateOfBirth(request.getDateOfBirth())
                .dateOfJoining(request.getDateOfJoining())
                .address(request.getAddress())
                .emergencyContactName(request.getEmergencyContactName())
                .emergencyContactPhone(request.getEmergencyContactPhone())
                .status(EmployeeStatus.ACTIVE)
                .build();

        employeeRepository.save(employee);
        log.info("Employee created: {} ({})", employee.getFullName(), employee.getEmployeeCode());
        return toResponse(employee);
    }


    @Transactional
    public EmployeeResponse updateEmployee(UUID id, UpdateEmployeeRequest request) {
        Employee employee = employeeRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found: " + id));

        if (request.getDepartmentId() != null) {
            Department dept = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Department not found"));
            employee.setDepartment(dept);
        }

        if (request.getDesignationId() != null) {
            Designation desig = designationRepository.findById(request.getDesignationId())
                    .orElseThrow(() -> new ResourceNotFoundException("Designation not found"));
            employee.setDesignation(desig);
        }

        if (request.getFirstName()  != null) employee.setFirstName(request.getFirstName());
        if (request.getLastName()   != null) employee.setLastName(request.getLastName());
        if (request.getPhone()      != null) employee.setPhone(request.getPhone());
        if (request.getGender()     != null) employee.setGender(request.getGender());
        if (request.getDateOfBirth()!= null) employee.setDateOfBirth(request.getDateOfBirth());
        if (request.getAddress()    != null) employee.setAddress(request.getAddress());
        if (request.getEmergencyContactName()  != null)
            employee.setEmergencyContactName(request.getEmergencyContactName());
        if (request.getEmergencyContactPhone() != null)
            employee.setEmergencyContactPhone(request.getEmergencyContactPhone());
        if (request.getProfilePictureUrl() != null)
            employee.setProfilePictureUrl(request.getProfilePictureUrl());
        if (request.getStatus() != null) employee.setStatus(request.getStatus());

        return toResponse(employeeRepository.save(employee));
    }

    @Transactional
    public void deleteEmployee(UUID id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found: " + id));
        User user = employee.getUser();
        // Hard delete — cascades to employee, payroll, attendance, leaves, performance etc.
        employeeRepository.delete(employee);
        userRepository.delete(user);
        log.info("Employee permanently deleted: {} (user: {})", id, user.getId());
    }

    @Transactional(readOnly = true)
    public EmployeeResponse getEmployee(UUID id) {
        return toResponse(employeeRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found: " + id)));
    }

    @Transactional(readOnly = true)
    public EmployeeResponse getEmployeeByUserId(UUID userId) {
        Employee employee = employeeRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("No employee linked to user: " + userId));
        return toResponse(employee);
    }

    @Transactional(readOnly = true)
    public Page<EmployeeResponse> searchEmployees(
            String search, UUID departmentId,
            UUID designationId, EmployeeStatus status,
            int page, int size, String sortBy, String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Specification<Employee> spec = EmployeeSpecification
                .withFilters(search, departmentId, designationId, status);

        return employeeRepository.findAll(spec, pageable).map(this::toResponse);
    }

    private String generateEmployeeCode(Department department) {
        String prefix = department.getCode().toUpperCase();
        String year = String.valueOf(LocalDate.now().getYear()).substring(2);
        long count = employeeRepository.countByDepartmentId(department.getId()) + 1;
        return String.format("%s%s%04d", prefix, year, count);
    }

    public EmployeeResponse toResponse(Employee e) {
        return EmployeeResponse.builder()
                .id(e.getId())
                .userId(e.getUser().getId())
                .employeeCode(e.getEmployeeCode())
                .firstName(e.getFirstName())
                .lastName(e.getLastName())
                .fullName(e.getFullName())
                .email(e.getUser().getEmail())
                .phone(e.getPhone())
                .gender(e.getGender())
                .dateOfBirth(e.getDateOfBirth())
                .dateOfJoining(e.getDateOfJoining())
                .address(e.getAddress())
                .profilePictureUrl(e.getProfilePictureUrl())
                .status(e.getStatus())
                .role(e.getUser().getRole())
                .department(EmployeeResponse.DepartmentSummary.builder()
                        .id(e.getDepartment().getId())
                        .name(e.getDepartment().getName())
                        .code(e.getDepartment().getCode())
                        .build())
                .designation(EmployeeResponse.DesignationSummary.builder()
                        .id(e.getDesignation().getId())
                        .title(e.getDesignation().getTitle())
                        .grade(e.getDesignation().getGrade())
                        .build())
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .build();
    }
}
