package com.nexushr.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when a resource creation or update request conflicts
 * with an existing unique record in the database.
 *
 * Examples:
 *   - Email address already registered
 *   - Username already taken
 *   - Department code already exists
 *   - Employee code collision
 *   - Performance review already submitted for this quarter
 *   - Payroll already exists for employee + month + year
 *   - Attendance record already exists for employee + date
 *
 * Maps to HTTP 409 Conflict via GlobalExceptionHandler.
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class DuplicateResourceException extends RuntimeException {

    /**
     * @param message Human-readable message identifying the duplicate field
     *                and value that caused the conflict.
     *                Example: "Email already registered: john@example.com"
     */
    public DuplicateResourceException(String message) {
        super(message);
    }

    /**
     * @param message   Description of the duplicate conflict.
     * @param cause     Original exception that triggered this one.
     */
    public DuplicateResourceException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Convenience factory — builds a standard "X already exists with Y: Z" message.
     *
     * Usage:
     *   throw DuplicateResourceException.of("Employee", "email", "john@example.com");
     *   // → "Employee already exists with email: john@example.com"
     *
     * @param resourceName  Entity name  (e.g. "Employee")
     * @param fieldName     Field name   (e.g. "email")
     * @param fieldValue    Field value  (e.g. "john@example.com")
     */
    public static DuplicateResourceException of(String resourceName,
                                                String fieldName,
                                                Object fieldValue) {
        return new DuplicateResourceException(
                resourceName + " already exists with "
                        + fieldName + ": " + fieldValue);
    }
}
