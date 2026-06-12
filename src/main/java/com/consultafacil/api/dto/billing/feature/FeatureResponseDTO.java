package com.consultafacil.api.dto.billing.feature;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class FeatureResponseDTO {
    private String id;
    private String key;
    private String name;
    private String description;
    private LocalDateTime createdAt;
}
