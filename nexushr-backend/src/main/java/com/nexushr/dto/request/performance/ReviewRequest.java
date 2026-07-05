package com.nexushr.dto.request.performance;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class ReviewRequest {

    /**
     * The employee being reviewed.
     */
    @NotNull(message = "Employee ID is required")
    private UUID employeeId;

    /**
     * Calendar year of the review cycle.
     */
    @NotNull(message = "Review year is required")
    @Min(value = 2020, message = "Review year must be 2020 or later")
    private Integer reviewYear;

    /**
     * Review quarter (1 = Q1 Jan–Mar … 4 = Q4 Oct–Dec).
     */
    @NotNull(message = "Review quarter is required")
    @Min(value = 1, message = "Quarter must be between 1 and 4")
    @Max(value = 4, message = "Quarter must be between 1 and 4")
    private Integer reviewQuarter;

    /**
     * Overall performance rating on a 1.0–5.0 scale.
     *
     * Interpretation:
     *   1.0 – 1.9  →  Unsatisfactory
     *   2.0 – 2.9  →  Needs Improvement
     *   3.0 – 3.9  →  Meets Expectations
     *   4.0 – 4.4  →  Exceeds Expectations
     *   4.5 – 5.0  →  Outstanding
     */
    @NotNull(message = "Rating is required")
    @DecimalMin(value = "1.0", message = "Rating must be at least 1.0")
    @DecimalMax(value = "5.0", message = "Rating must not exceed 5.0")
    private BigDecimal rating;

    /**
     * Detailed performance feedback — required, shown in employee reports.
     */
    @NotBlank(message = "Feedback is required")
    @Size(min = 20, max = 3000,
            message = "Feedback must be between 20 and 3000 characters")
    private String feedback;

    /**
     * Key strengths demonstrated during the review period.
     */
    @Size(max = 1000, message = "Strengths must not exceed 1000 characters")
    private String strengths;

    /**
     * Areas where improvement is recommended.
     */
    @Size(max = 1000, message = "Improvements must not exceed 1000 characters")
    private String improvements;
}
