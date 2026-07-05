package com.nexushr.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Value("${spring.profiles.active:dev}")
    private String activeProfile;

    private static final String SECURITY_SCHEME_NAME = "bearerAuth";

    /**
     * Main OpenAPI bean — defines metadata, security scheme,
     * and per-environment server URLs shown in Swagger UI.
     */
    @Bean
    public OpenAPI nexusHrOpenAPI() {
        return new OpenAPI()
                .info(buildApiInfo())
                .servers(buildServers())
                .externalDocs(buildExternalDocs())
                .components(buildComponents())
                .addSecurityItem(new SecurityRequirement()
                        .addList(SECURITY_SCHEME_NAME));
    }

    // ── Info ─────────────────────────────────────────────────────────────────

    private Info buildApiInfo() {
        return new Info()
                .title("NexusHR API")
                .description("""
                        **NexusHR** — AI-Enabled Enterprise HR & Workforce Intelligence Platform.

                        ## Authentication
                        All endpoints except `/api/v1/auth/**` require a JWT Bearer token.

                        **Steps:**
                        1. Call `POST /api/v1/auth/login` with your credentials.
                        2. Copy the `accessToken` from the response.
                        3. Click **Authorize** (top right), paste `Bearer <token>`.
                        4. All subsequent requests will include the token automatically.

                        ## Roles
                        | Role | Access Level |
                        |---|---|
                        | ADMIN | Full access to all endpoints |
                        | MANAGER | Team-level access; cannot manage payroll structure |
                        | EMPLOYEE | Self-service access only |

                        ## Rate Limits (Production)
                        - Auth endpoints: 5 req/s per IP
                        - All other API endpoints: 30 req/s per IP
                        """)
                .version("1.0.0")
                .contact(new Contact()
                        .name("NexusHR Development Team")
                        .email("dev@nexushr.com")
                        .url("https://github.com/your-org/nexushr"))
                .license(new License()
                        .name("MIT License")
                        .url("https://opensource.org/licenses/MIT"));
    }

    // ── Servers ──────────────────────────────────────────────────────────────

    private List<Server> buildServers() {
        Server localServer = new Server()
                .url("http://localhost:8080")
                .description("Local Development Server");

        Server stagingServer = new Server()
                .url("https://staging-api.nexushr.com")
                .description("Staging Server");

        Server productionServer = new Server()
                .url("https://api.nexushr.com")
                .description("Production Server");

        return switch (activeProfile) {
            case "prod"    -> List.of(productionServer, stagingServer, localServer);
            case "staging" -> List.of(stagingServer, localServer);
            default        -> List.of(localServer, stagingServer, productionServer);
        };
    }

    // ── External Docs ─────────────────────────────────────────────────────────

    private ExternalDocumentation buildExternalDocs() {
        return new ExternalDocumentation()
                .description("NexusHR Full Documentation & Source Code")
                .url("https://github.com/your-org/nexushr");
    }

    // ── Security Components ───────────────────────────────────────────────────

    /**
     * Registers the JWT Bearer security scheme.
     * This enables the Authorize button in Swagger UI.
     *
     * Scheme type: HTTP
     * Scheme name: bearer
     * Bearer format: JWT
     */
    private Components buildComponents() {
        SecurityScheme jwtScheme = new SecurityScheme()
                .name(SECURITY_SCHEME_NAME)
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .description("""
                        Enter your JWT access token in the format: `Bearer <token>`

                        Obtain a token by calling:
                        `POST /api/v1/auth/login`

                        Tokens expire after **15 minutes**.
                        Use `POST /api/v1/auth/refresh-token` to get a new one.
                        """);

        return new Components()
                .addSecuritySchemes(SECURITY_SCHEME_NAME, jwtScheme);
    }
}
