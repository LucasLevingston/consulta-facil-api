package com.consultafacil.api.dto.patient;

import com.consultafacil.domain.enums.EmergencyContactRelationship;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record EmergencyContactDTO(
        String id,
        @NotBlank @Size(max = 150) String name,
        @NotBlank @Size(max = 20) String phone,
        String email,
        EmergencyContactRelationship relationship
) {}
