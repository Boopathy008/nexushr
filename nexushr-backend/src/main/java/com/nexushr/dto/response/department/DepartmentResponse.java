package com.nexushr.dto.response.department;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class DepartmentResponse {

    private UUID    id;
    private String  name;
    private String  code;
    private String  description;
    private boolean active;
    private int     employeeCount;
}
