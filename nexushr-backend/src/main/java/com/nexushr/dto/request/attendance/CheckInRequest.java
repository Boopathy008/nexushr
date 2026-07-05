package com.nexushr.dto.request.attendance;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CheckInRequest {

    /**
     * Optional note added at check-in time.
     * Example: "Working from home today", "On client site"
     */
    @Size(max = 255, message = "Note must not exceed 255 characters")
    private String notes;

    /**
     * Optional: device or location identifier for future
     * biometric / geo-fencing integration.
     */
    @Size(max = 100)
    private String deviceId;
}
