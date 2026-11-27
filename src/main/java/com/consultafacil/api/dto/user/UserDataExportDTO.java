package com.consultafacil.api.dto.user;

import java.time.LocalDateTime;
import java.util.List;

public record UserDataExportDTO(
        String id,
        String name,
        String email,
        String cpf,
        String phone,
        String role,
        LocalDateTime createdAt,
        LocalDateTime consentGivenAt,
        String consentVersion,
        List<AppointmentSummaryDTO> appointments
) {}
