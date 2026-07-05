package com.nexushr.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when a request violates a domain-level business rule.
 *
 * The request is technically valid (passes validation) but cannot
 * be fulfilled due to the current state of the system or domain constraints.
 *
 * Examples:
 *   - Employee already checked in today
 *   - Insufficient leave balance for requested days
 *   - Cannot cancel a leave that has already started
 *   - Cannot approve a leave that is not in PENDING status
 *   - Payroll already generated for this period
 *   - Check-out attempted before check-in
 *   - Leave date range contains no working days
 *   - Refresh token has expired
 *
 * Maps to HTTP 422 Unprocessable Entity via GlobalExceptionHandler.
 */
@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
public class BusinessRuleException extends RuntimeException {

    /**
     * @param message Human-readable explanation of which business rule
     *                was violated and why the operation cannot proceed.
     */
    public BusinessRuleException(String message) {
        super(message);
    }

    /**
     * @param message   Description of the violated business rule.
     * @param cause     Original exception that triggered this one.
     */
    public BusinessRuleException(String message, Throwable cause) {
        super(message, cause);
    }
}
