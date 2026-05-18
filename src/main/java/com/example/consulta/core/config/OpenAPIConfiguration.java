package com.example.consulta.core.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "Consulta Fácil API",
        version = "1.0.0",
        description = "API para agendamento de consultas médicas com autenticação JWT",
        contact = @Contact(
            name = "Consulta Fácil Team",
            email = "support@consultafacil.com"
        )
    ),
    servers = {
        @Server(
            url = "http://localhost:8080/api/v1",
            description = "Local Development Server"
        ),
        @Server(
            url = "https://api.consultafacil.com/api/v1",
            description = "Production Server"
        )
    }
)
@SecurityScheme(
    name = "bearerAuth",
    type = SecuritySchemeType.HTTP,
    scheme = "bearer",
    bearerFormat = "JWT",
    description = "JWT token para autenticação"
)
public class OpenAPIConfiguration {
}
