package com.nexushr.dto.request.designation;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.UUID;

@Data
public class DesignationRequest {

    @NotNull(message = "Department ID is required")
    private UUID departmentId;

    @NotBlank(message = "Designation title is required")
    @Size(min = 2, max = 100,
            message = "Title must be between 2 and 100 characters")
    private String title;

    @Size(max = 20, message = "Grade must not exceed 20 characters")
    private String grade;
}
