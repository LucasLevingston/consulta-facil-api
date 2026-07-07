package com.consultafacil.application.port.in.conversation;

import com.consultafacil.api.dto.messaging.ConversationResponseDTO;

import java.util.List;

public interface ListConversationsUseCase {

    List<ConversationResponseDTO> execute(String userId);
}
