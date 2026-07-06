package com.consultafacil.application.port.in;

import com.consultafacil.api.dto.messaging.MessageResponseDTO;

public interface SendMessageUseCase {

    MessageResponseDTO execute(String conversationId, String senderId, String content);
}
