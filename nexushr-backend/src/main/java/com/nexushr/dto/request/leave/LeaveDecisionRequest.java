package com.nexushr.dto.request.leave;

import com.nexushr.domain.enums.LeaveStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LeaveDecisionRequest {

    @NotNull(message = "Decision is required")
    private LeaveStatus decision;   // APPROVED or REJECTED

    private String rejectionNote;   // required when REJECTED
}
