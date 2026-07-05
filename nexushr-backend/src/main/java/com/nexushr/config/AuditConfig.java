package com.nexushr.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
public class AuditConfig {

    /**
     * Supplies the currently authenticated username to JPA auditing.
     *
     * Used by @CreatedBy and @LastModifiedBy fields on entities.
     * Falls back to "SYSTEM" for operations triggered outside
     * of an authenticated request (e.g. Flyway seed scripts,
     * scheduled jobs, application startup).
     *
     * How it works:
     *   Spring Data JPA calls getCurrentAuditor() on every INSERT
     *   and UPDATE and stores the returned value in the auditing column.
     */
    @Bean
    public AuditorAware<String> auditorProvider() {
        return () -> {
            Authentication authentication =
                    SecurityContextHolder.getContext().getAuthentication();

            if (authentication == null
                    || !authentication.isAuthenticated()
                    || "anonymousUser".equals(authentication.getPrincipal())) {
                return Optional.of("SYSTEM");
            }

            return Optional.of(authentication.getName());
        };
    }
}
