package com.consultafacil.application.port.in;

import com.consultafacil.api.dto.ai.ChatMessage;

import java.util.List;
import java.util.Map;

public interface AnamnesisExtractionUseCase {
    Map<String, String> execute(List<ChatMessage> messages);
}
