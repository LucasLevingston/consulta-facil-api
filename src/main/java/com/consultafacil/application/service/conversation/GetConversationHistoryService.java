package com.consultafacil.application.service.conversation;

import com.consultafacil.api.dto.messaging.MessageResponseDTO;
import com.consultafacil.application.port.in.GetConversationHistoryUseCase;
import com.consultafacil.domain.port.out.MessageRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetConversationHistoryService implements GetConversationHistoryUseCase {

    private final MessageRepositoryPort messageRepository;
    private final ConversationAccessValidator accessValidator;
    private final MessageMapper mapper;

    @Override
    @Transactional
    public Page<MessageResponseDTO> execute(String conversationId, String userId, Pageable pageable) {
        accessValidator.findAccessibleConversation(conversationId, userId);
        return messageRepository.findByConversationId(conversationId, pageable)
                .map(mapper::toDTO);
    }
}
