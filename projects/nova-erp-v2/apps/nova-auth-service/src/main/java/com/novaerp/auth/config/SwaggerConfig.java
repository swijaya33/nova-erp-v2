package com.novaerp.auth.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI 3.0 (Swagger) configuration for Auth Service.
 * Exposes /swagger-ui.html with JWT Bearer token auth in the UI.
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI novaAuthOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Nova ERP v2 — Auth Service API")
                        .description("JWT-based authentication, RBAC, and user management service.")
                        .version("0.0.1"))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .schemaRequirement("bearerAuth", new SecurityScheme()
                        .name("bearerAuth")
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT"));
    }

}
