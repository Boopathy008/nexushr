package com.nexushr.dto.response.ai;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data @Builder
public class DepartmentIntelligenceResponse {
    private UUID   departmentId;
    private String departmentName;
    private int    totalEmployees;
    private double avgAttritionRisk;
    private double avgEngagementScore;
    private double avgAttendanceRate;
    private int    highRiskCount;
    private int    criticalRiskCount;
    private List<WorkforceIntelligenceResponse> employeeInsights;
}
