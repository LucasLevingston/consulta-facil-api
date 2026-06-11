package com.consultafacil.api.dto.ai;

public record VoiceBookingResponseDTO(
        String specialty,
        String professionalName,
        String date,
        String timePreference,
        String modality,
        String reason,
        String confidence,
        String summary
) {}
