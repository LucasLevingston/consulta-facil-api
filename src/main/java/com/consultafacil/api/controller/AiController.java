package com.consultafacil.api.controller;

import com.consultafacil.api.dto.ai.AnamnesisChatRequestDTO;
import com.consultafacil.api.dto.ai.AnamnesisExtractRequestDTO;
import com.consultafacil.api.dto.ai.VoiceBookingRequestDTO;
import com.consultafacil.api.dto.ai.VoiceBookingResponseDTO;
import com.consultafacil.application.service.AnamnesisChatStreamService;
import com.consultafacil.application.service.AnamnesisExtractionService;
import com.consultafacil.application.service.VoiceBookingExtractionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.util.Map;

@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
@Tag(name = "AI", description = "AI-powered endpoints")
public class AiController {

    private final AnamnesisChatStreamService anamnesisChatStreamService;
    private final AnamnesisExtractionService anamnesisExtractionService;
    private final VoiceBookingExtractionService voiceBookingExtractionService;

    @PostMapping(value = "/anamnesis/chat", produces = MediaType.TEXT_PLAIN_VALUE)
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Anamnese AI chat (streaming)")
    public ResponseEntity<StreamingResponseBody> anamnesisChat(
            @RequestBody AnamnesisChatRequestDTO request) {
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_PLAIN)
                .body(anamnesisChatStreamService.stream(request.messages()));
    }

    @PostMapping("/anamnesis/extract")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Extrair campos de anamnese da conversa")
    public ResponseEntity<Map<String, String>> anamnesisExtract(
            @RequestBody AnamnesisExtractRequestDTO request) {
        return ResponseEntity.ok(anamnesisExtractionService.extract(request.messages()));
    }

    @PostMapping("/voice-booking")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Extrair intenção de agendamento de transcrição de voz")
    public ResponseEntity<VoiceBookingResponseDTO> voiceBooking(
            @RequestBody VoiceBookingRequestDTO request) {
        return ResponseEntity.ok(voiceBookingExtractionService.process(request.transcript()));
    }
}
