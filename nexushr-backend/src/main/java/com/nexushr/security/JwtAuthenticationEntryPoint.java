package com.nexushr.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
@Slf4j
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    public JwtAuthenticationEntryPoint() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    /**
     * Invoked when an unauthenticated request tries to access a secured endpoint.
     *
     * Returns a structured JSON 401 response — consistent with the
     * GlobalExceptionHandler error format used across the entire API.
     *
     * Scenarios that trigger this:
     *   - No Authorization header present
     *   - Malformed / expired JWT token
     *   - Token signature invalid
     */
    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException)
            throws IOException {

        log.warn("Unauthorized access attempt: {} {} | Reason: {}",
                request.getMethod(),
                request.getRequestURI(),
                authException.getMessage());

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        Map<String, Object> errorBody = new LinkedHashMap<>();
        errorBody.put("status",    HttpStatus.UNAUTHORIZED.value());
        errorBody.put("error",     HttpStatus.UNAUTHORIZED.getReasonPhrase());
        errorBody.put("message",   "Authentication required. " +
                "Please provide a valid Bearer token.");
        errorBody.put("path",      request.getRequestURI());
        errorBody.put("timestamp", LocalDateTime.now().toString());

        response.getWriter().write(
                objectMapper.writeValueAsString(errorBody)
        );
    }
}
