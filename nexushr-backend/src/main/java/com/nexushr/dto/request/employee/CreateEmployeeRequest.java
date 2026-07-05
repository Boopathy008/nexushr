package com.nexushr.dto.request.employee;

import com.nexushr.domain.enums.Role;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class CreateEmployeeRequest {

    @NotBlank @Size(min = 3, max = 50)
    private String username;

    @NotBlank @Email
    private String email;

    @NotBlank @Size(min = 8)
    private String password;

    @NotNull
    private UUID departmentId;

    @NotNull
    private UUID designationId;

    @NotBlank @Size(max = 100)
    private String firstName;

    @NotBlank @Size(max = 100)
    private String lastName;

    @Pattern(regexp = "^[+]?[0-9]{7,15}$", message = "Invalid phone number")
    private String phone;

    private String gender;

    private LocalDate dateOfBirth;

    @NotNull
    private LocalDate dateOfJoining;

    private String address;
    private String emergencyContactName;
    private String emergencyContactPhone;
    private Role role = Role.EMPLOYEE;
}
