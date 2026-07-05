package com.nexushr.dto.response.payroll;

import com.nexushr.domain.enums.PayrollStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data @Builder
public class PayslipResponse {
    private UUID id;
    private String employeeName;
    private String employeeCode;
    private String department;
    private String designation;
    private int payMonth;
    private int payYear;
    private String payPeriod;

    private int workingDays;
    private BigDecimal presentDays;
    private BigDecimal leaveDays;
    private BigDecimal lopDays;

    private BigDecimal basicSalary;
    private BigDecimal hra;
    private BigDecimal allowances;
    private BigDecimal grossSalary;

    private BigDecimal taxDeduction;
    private BigDecimal pfDeduction;
    private BigDecimal lopDeduction;
    private BigDecimal otherDeductions;
    private BigDecimal totalDeductions;

    private BigDecimal netSalary;
    private PayrollStatus status;
    private LocalDateTime processedAt;
}
