package com.nexushr.dto.request.performance;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class GoalRequest {

    /**
     * Employee this goal is assigned to.
     */
    @NotNull(message = "Employee ID is required")
    private UUID employeeId;

    /**
     * Short goal title shown in dashboards and reports.
     */
    @NotBlank(message = "Goal title is required")
    @Size(min = 5, max = 200,
            message = "Title must be between 5 and 200 characters")
    private String title;

    /**
     * Detailed description of the goal and success criteria.
     */
    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    private String description;

    /**
     * Target completion date — must be a future or present date.
     */
    @FutureOrPresent(message = "Target date must be today or a future date")
    private LocalDate targetDate;

    /**
     * Calendar year this goal belongs to.
     */
    @NotNull(message = "Year is required")
    @Min(value = 2020, message = "Year must be 2020 or later")
    private Integer year;

    /**
     * Quarter this goal belongs to (1 = Jan–Mar, 4 = Oct–Dec).
     */
    @NotNull(message = "Quarter is required")
    @Min(value = 1, message = "Quarter must be between 1 and 4")
    @Max(value = 4, message = "Quarter must be between 1 and 4")
    private Integer quarter;

    /**
     * Initial status — defaults to IN_PROGRESS.
     * Allowed values: IN_PROGRESS, COMPLETED, CANCELLED
     */
    private String status = "IN_PROGRESS";
}