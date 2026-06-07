package com.example.consulta.core.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Configuration
@Profile("prod")
public class ProdEnvironmentValidator {

    private static final List<String> REQUIRED_VARS = List.of(
            "JWT_SECRET",
            "DB_URL",
            "DB_USERNAME",
            "DB_PASSWORD",
            "FLYWAY_DB_USERNAME",
            "FLYWAY_DB_PASSWORD",
            "AWS_S3_BUCKET",
            "AWS_REGION",
            "MERCADOPAGO_ACCESS_TOKEN",
            "APP_URL",
            "CORS_ALLOWED_ORIGINS",
            "GRAFANA_OTLP_ENDPOINT",
            "GRAFANA_OTLP_TOKEN"
    );

    @PostConstruct
    public void validate() {
        List<String> missing = new ArrayList<>();
        for (String var : REQUIRED_VARS) {
            String value = System.getenv(var);
            if (value == null || value.isBlank()) {
                missing.add(var);
            }
        }

        if (!missing.isEmpty()) {
            throw new IllegalStateException(
                    "Application startup aborted — missing required environment variables: " +
                    String.join(", ", missing) +
                    "\nSet these variables before starting in production."
            );
        }

        log.info("All {} required environment variables present.", REQUIRED_VARS.size());
    }
}
