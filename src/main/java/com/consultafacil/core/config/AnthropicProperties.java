package com.consultafacil.core.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "anthropic")
public class AnthropicProperties {
    private String apiKey = "";
    private String model = "claude-haiku-4-5-20251001";
}
