package com.consultafacil.application.port.in;

import com.consultafacil.api.dto.messaging.MessageResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface GetConversationHistoryUseCase {

    Page<MessageResponseDTO> execute(String conversationId, String userId, Pageable pageable);
}
