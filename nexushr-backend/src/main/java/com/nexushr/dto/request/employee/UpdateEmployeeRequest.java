package com.nexushr.dto.request.employee;

import com.nexushr.domain.enums.EmployeeStatus;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class UpdateEmployeeRequest {

    private UUID departmentId;
    private UUID designationId;

    @Size(max = 100)
    private String firstName;

    @Size(max = 100)
    private String lastName;

    @Pattern(regexp = "^[+]?[0-9]{7,15}$", message = "Invalid phone number")
    private String phone;

    private String gender;
    private LocalDate dateOfBirth;
    private String address;
    private String emergencyContactName;
    private String emergencyContactPhone;
    private String profilePictureUrl;
    private EmployeeStatus status;
}
