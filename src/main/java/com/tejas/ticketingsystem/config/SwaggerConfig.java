package com.tejas.ticketingsystem.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI ticketingOpenAPI() {

        final String securityScheme = "bearerAuth";

        return new OpenAPI()
                .addServersItem(
                        new Server()
                                .url("https://support-ticket-system-production-9564.up.railway.app")
                                .description("Production")
                )
                .info(new Info()
                        .title("Support Ticket System API")
                        .version("1.0")
                        .description("Role-Based Support Ticket Platform"))
                .addSecurityItem(new SecurityRequirement().addList(securityScheme))
                .schemaRequirement(
                        securityScheme,
                        new SecurityScheme()
                                .name(securityScheme)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                );
    }
}