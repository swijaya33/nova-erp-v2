package com.novaerp.gl.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI 3.0 (Swagger) configuration for GL Service.
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI novaGLOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Nova ERP v2 — General Ledger Service API")
                        .description("SAK-compliant accounting: Chart of Accounts, Journal Entries, Financial Reports.")
                        .version("0.0.1"));
    }

}
