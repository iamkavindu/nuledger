package dev.iamkavindu.nuledger.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String BEARER_JWT = "bearer-jwt";

    @Bean
    OpenAPI nuledgerOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Nuledger")
                        .description("""
                                Multi-tenant double-entry ledger.
                                All endpoints require a JWT with claim `tenant_id`.
                                Errors use RFC 9457 Problem Details (`application/problem+json`).
                                API version is a path segment, e.g. `/api/1.0/...` or `/api/v1/...`.
                                """)
                        .version("1.0")
                        .contact(new Contact().name("Nuledger").url("https://nuledger.dev")))
                .components(new Components()
                        .addSecuritySchemes(
                                BEARER_JWT,
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Keycloak JWT; must include `tenant_id` claim")))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_JWT));
    }
}
