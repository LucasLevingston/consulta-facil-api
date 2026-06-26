package com.consultafacil.application.port.in;

import com.consultafacil.api.dto.messaging.ConversationResponseDTO;
import com.consultafacil.api.dto.messaging.MessageResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ConversationUseCase {
    ConversationResponseDTO getOrCreateConversation(String requesterId, String professionalId);
    List<ConversationResponseDTO> listConversations(String userId);
    Page<MessageResponseDTO> getHistory(String conversationId, String userId, Pageable pageable);
    MessageResponseDTO sendMessage(String conversationId, String senderId, String content);
    void markAsRead(String conversationId, String userId);
}
