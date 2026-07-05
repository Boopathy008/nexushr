package com.nexushr.dto.request.department;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class DepartmentRequest {

    @NotBlank(message = "Department name is required")
    @Size(min = 2, max = 100,
            message = "Department name must be between 2 and 100 characters")
    private String name;

    @NotBlank(message = "Department code is required")
    @Size(min = 2, max = 20,
            message = "Department code must be between 2 and 20 characters")
    @Pattern(regexp = "^[A-Z0-9_]+$",
            message = "Department code must contain only uppercase letters, " +
                    "numbers, or underscores")
    private String code;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;
}
