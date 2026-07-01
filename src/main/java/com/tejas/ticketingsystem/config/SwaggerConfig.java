package com.tejas.ticketingsystem.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI ticketingOpenAPI() {

        final String securityScheme = "bearerAuth";

        return new OpenAPI()
                .info(new Info()
                        .title("Support Ticket System API")
                        .version("1.0")
                        .description("Role-Based Support Ticket Platform"))
                .servers(List.of(
                        new Server()
                                .url("https://support-ticket-system-production-9564.up.railway.app")
                                .description("Production"),
                        new Server()
                                .url("http://localhost:8080")
                                .description("Local")
                ))
                .addSecurityItem(new SecurityRequirement().addList(securityScheme))
                .components(new Components()
                        .addSecuritySchemes(securityScheme, new SecurityScheme()
                                .name(securityScheme)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}