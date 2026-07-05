package com.nexushr.dto.request.payroll;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.UUID;

@Data
public class GeneratePayrollRequest {

    /**
     * Target employee — optional for bulk generation.
     * When null, payroll is generated for ALL active employees
     * who have a salary structure configured.
     */
    private UUID employeeId;

    /**
     * Pay month (1 = January … 12 = December).
     */
    @NotNull(message = "Pay month is required")
    @Min(value = 1,  message = "Month must be between 1 and 12")
    @Max(value = 12, message = "Month must be between 1 and 12")
    private Integer payMonth;

    /**
     * Pay year — must be a realistic 4-digit year.
     */
    @NotNull(message = "Pay year is required")
    @Min(value = 2020, message = "Year must be 2020 or later")
    @Max(value = 2100, message = "Year must be 2100 or earlier")
    private Integer payYear;

    /**
     * When true, regenerates payroll even if a record already
     * exists for this period (overwrites DRAFT status records only).
     * PROCESSED records are never overwritten.
     */
    private boolean forceRegenerate = false;
}
