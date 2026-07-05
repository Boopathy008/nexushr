package com.nexushr.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;
import java.util.List;

@Configuration
@Slf4j
public class CorsConfig {

    @Value("${app.cors.allowed-origins:http://localhost:5173,http://localhost:5174}")
    private String allowedOriginsRaw;

    /**
     * Allowed HTTP methods for cross-origin requests.
     */
    private static final List<String> ALLOWED_METHODS = List.of(
            "GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS", "HEAD"
    );

    /**
     * Headers the client is allowed to send.
     */
    private static final List<String> ALLOWED_HEADERS = List.of(
            "Authorization",
            "Content-Type",
            "Accept",
            "X-Requested-With",
            "Cache-Control",
            "Origin"
    );

    /**
     * Headers the browser is allowed to read from the response.
     */
    private static final List<String> EXPOSED_HEADERS = List.of(
            "Authorization",
            "Content-Disposition"
    );

    /**
     * Primary CORS configuration source — consumed by Spring Security's
     * cors() DSL in SecurityConfig.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        List<String> allowedOrigins = parseOrigins(allowedOriginsRaw);
        log.info("CORS allowed origins: {}", allowedOrigins);

        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(allowedOrigins);
        config.setAllowedMethods(ALLOWED_METHODS);
        config.setAllowedHeaders(ALLOWED_HEADERS);
        config.setExposedHeaders(EXPOSED_HEADERS);

        // Allow cookies / Authorization headers in cross-origin requests.
        // Must NOT be combined with allowedOrigins = ["*"]
        config.setAllowCredentials(true);

        // How long the browser caches the preflight response (seconds)
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

        // Apply CORS config to all API endpoints
        source.registerCorsConfiguration("/api/**", config);

        // Swagger UI endpoints also need CORS in dev
        source.registerCorsConfiguration("/swagger-ui/**", config);
        source.registerCorsConfiguration("/api-docs/**", config);

        return source;
    }

    /**
     * Standalone CorsFilter bean — acts as a pre-security filter so that
     * preflight OPTIONS requests are handled before authentication checks.
     */
    @Bean
    public CorsFilter corsFilter() {
        return new CorsFilter(corsConfigurationSource());
    }

    // ── Private helpers ──────────────────────────────────────────────────────

    private List<String> parseOrigins(String raw) {
        if (raw == null || raw.isBlank()) {
            return List.of("http://localhost:5173");
        }
        return Arrays.stream(raw.split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .toList();
    }
}
