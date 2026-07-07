package com.consultafacil.application.port.in.conversation;

import com.consultafacil.api.dto.messaging.ConversationResponseDTO;

public interface GetOrCreateConversationUseCase {

    ConversationResponseDTO execute(String requesterId, String professionalId);
}
