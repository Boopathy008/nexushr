package com.nexushr.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

@Configuration
@ConfigurationProperties(prefix = "app.jwt")
@Validated
@Getter
@Setter
public class JwtConfig {

    /**
     * Base64-encoded HMAC-SHA256 secret key.
     * Minimum 256-bit (32 bytes). Set via JWT_SECRET env variable.
     * Generate: openssl rand -base64 64
     */
    @NotBlank(message = "JWT secret must not be blank")
    private String secret;

    /**
     * Access token validity in milliseconds.
     * Default: 900000 (15 minutes)
     */
    @Positive
    private long accessTokenExpiryMs = 900_000L;

    /**
     * Refresh token validity in milliseconds.
     * Default: 604800000 (7 days)
     */
    @Positive
    private long refreshTokenExpiryMs = 604_800_000L;

    /**
     * Token type prefix used in Authorization header.
     * Default: Bearer
     */
    private String tokenPrefix = "Bearer";

    /**
     * Authorization header name.
     */
    private String headerName = "Authorization";

    // ── Convenience helpers ──────────────────────────────────────────────────

    /**
     * Returns access token expiry in seconds (for response DTOs).
     */
    public long getAccessTokenExpirySeconds() {
        return accessTokenExpiryMs / 1000;
    }

    /**
     * Returns refresh token expiry in seconds.
     */
    public long getRefreshTokenExpirySeconds() {
        return refreshTokenExpiryMs / 1000;
    }
}
