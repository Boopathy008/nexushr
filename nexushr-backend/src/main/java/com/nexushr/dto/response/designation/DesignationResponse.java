package com.nexushr.dto.response.designation;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class DesignationResponse {

    private UUID    id;
    private String  title;
    private String  grade;
    private boolean active;
    private UUID    departmentId;
    private String  departmentName;
    private String  departmentCode;
}
