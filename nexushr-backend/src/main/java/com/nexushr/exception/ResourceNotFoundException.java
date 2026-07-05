package com.nexushr.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when a requested resource does not exist in the database.
 *
 * Examples:
 *   - Employee not found by ID
 *   - Department not found by code
 *   - Leave request not found
 *   - Salary structure not configured for employee
 *
 * Maps to HTTP 404 Not Found via GlobalExceptionHandler.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException {

    /**
     * @param message Human-readable message describing which resource
     *                was not found and what identifier was used.
     *                Example: "Employee not found: a1b2c3d4-..."
     */
    public ResourceNotFoundException(String message) {
        super(message);
    }

    /**
     * @param message   Description of the missing resource.
     * @param cause     Original exception that triggered this one.
     */
    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Convenience factory — builds a standard "X not found with Y: Z" message.
     *
     * Usage:
     *   throw ResourceNotFoundException.of("Employee", "id", employeeId);
     *   // → "Employee not found with id: a1b2c3d4-..."
     *
     * @param resourceName  Entity name  (e.g. "Employee")
     * @param fieldName     Field name   (e.g. "id")
     * @param fieldValue    Field value  (e.g. UUID)
     */
    public static ResourceNotFoundException of(String resourceName,
                                               String fieldName,
                                               Object fieldValue) {
        return new ResourceNotFoundException(
                resourceName + " not found with "
                        + fieldName + ": " + fieldValue);
    }
}
