package com.nexushr.dto.request.payroll;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class SalaryStructureRequest {

    @NotNull @DecimalMin("0.01")
    private BigDecimal basicSalary;

    @DecimalMin("0") private BigDecimal hra             = BigDecimal.ZERO;
    @DecimalMin("0") private BigDecimal transportAllowance = BigDecimal.ZERO;
    @DecimalMin("0") private BigDecimal medicalAllowance   = BigDecimal.ZERO;
    @DecimalMin("0") private BigDecimal otherAllowances    = BigDecimal.ZERO;

    @DecimalMin("0") @DecimalMax("50")
    private BigDecimal taxRate = BigDecimal.ZERO;

    @DecimalMin("0") @DecimalMax("30")
    private BigDecimal pfRate = new BigDecimal("12.00");

    @NotNull private LocalDate effectiveFrom;
}
