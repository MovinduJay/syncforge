package com.syncforge.syncforge.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME_NAME = "bearerAuth";

    @Bean
    public OpenAPI syncForgeOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("SyncForge API")
                        .version("1.0.0")
                        .description("""
                                SyncForge is a multi-tenant SaaS data synchronization platform.
                                It supports webhook ingestion, entity mapping, async sync jobs,
                                transactional outbox recovery, audit logs, JWT authentication,
                                role-based authorization, and tenant isolation.
                                """)
                        .contact(new Contact()
                                .name("Movindu Jayathilake"))
                        .license(new License()
                                .name("Portfolio Project")))
                .addSecurityItem(new SecurityRequirement()
                        .addList(SECURITY_SCHEME_NAME))
                .components(new Components()
                        .addSecuritySchemes(
                                SECURITY_SCHEME_NAME,
                                new SecurityScheme()
                                        .name(SECURITY_SCHEME_NAME)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                        ));
    }
}
