package com.nexushr.dto.response.employee;

import com.nexushr.domain.enums.EmployeeStatus;
import com.nexushr.domain.enums.Role;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data @Builder
public class EmployeeResponse {
    private UUID id;
    private UUID userId;
    private String employeeCode;
    private String firstName;
    private String lastName;
    private String fullName;
    private String email;
    private String phone;
    private String gender;
    private LocalDate dateOfBirth;
    private LocalDate dateOfJoining;
    private String address;
    private String profilePictureUrl;
    private EmployeeStatus status;
    private Role role;

    private DepartmentSummary department;
    private DesignationSummary designation;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data @Builder
    public static class DepartmentSummary {
        private UUID id;
        private String name;
        private String code;
    }

    @Data @Builder
    public static class DesignationSummary {
        private UUID id;
        private String title;
        private String grade;
    }
}
