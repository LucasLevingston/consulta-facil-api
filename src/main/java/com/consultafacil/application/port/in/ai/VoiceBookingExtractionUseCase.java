package com.consultafacil.application.port.in.ai;

import com.consultafacil.api.dto.ai.VoiceBookingResponseDTO;

public interface VoiceBookingExtractionUseCase {
    VoiceBookingResponseDTO execute(String transcript);
}
