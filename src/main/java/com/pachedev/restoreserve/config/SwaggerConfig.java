package com.pachedev.restoreserve.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

/**
 * Configuración de OpenAPI/Swagger para documentar la API y definir el esquema
 * JWT.
 */
@Configuration
public class SwaggerConfig {

    /**
     * Define la información básica de la API y el esquema de seguridad bearer para
     * Swagger.
     */
    @Bean
    public OpenAPI customOpenAPI() {

        String securitySchemeName = "RestoReserveToken";

        return new OpenAPI().info(new Info()
                .title("RestoReserve API")
                .version("1.0")
                .description("REST API for managing restaurant tables and reservations"))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components().addSecuritySchemes(securitySchemeName, new SecurityScheme()
                        .name(securitySchemeName)
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")));
    }
}
