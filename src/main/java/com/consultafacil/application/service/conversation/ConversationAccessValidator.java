package com.consultafacil.application.service.conversation;

import com.consultafacil.core.exception.ResourceNotFoundException;
import com.consultafacil.domain.entity.Conversation;
import com.consultafacil.domain.port.out.conversation.ConversationRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
@RequiredArgsConstructor
public class ConversationAccessValidator {

    private final ConversationRepositoryPort conversationRepository;

    public Conversation findAccessibleConversation(String conversationId, String userId) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation", conversationId));
        boolean isParticipant = conversation.getPatient().getId().equals(userId)
                || conversation.getProfessional().getId().equals(userId);
        if (!isParticipant) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }
        return conversation;
    }
}
