package com.nexushr.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when an authenticated user attempts to access or modify
 * a resource they do not have permission to touch.
 *
 * Distinct from Spring Security's AccessDeniedException (which is thrown
 * for role-based access failures on endpoints). This exception is thrown
 * inside service-layer business logic for ownership / data-level checks.
 *
 * Examples:
 *   - Employee tries to cancel another employee's leave request
 *   - Employee tries to view another employee's payslip
 *   - Manager tries to approve a leave from a different department
 *   - Employee tries to edit a submitted performance review
 *   - User tries to update a resource that belongs to a different tenant
 *
 * Maps to HTTP 403 Forbidden via GlobalExceptionHandler.
 */
@ResponseStatus(HttpStatus.FORBIDDEN)
public class UnauthorizedAccessException extends RuntimeException {

    /**
     * @param message Human-readable message describing what action
     *                was attempted and why it is not permitted.
     *                Example: "You can only cancel your own leave requests"
     */
    public UnauthorizedAccessException(String message) {
        super(message);
    }

    /**
     * @param message   Description of the unauthorized access attempt.
     * @param cause     Original exception that triggered this one.
     */
    public UnauthorizedAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}
