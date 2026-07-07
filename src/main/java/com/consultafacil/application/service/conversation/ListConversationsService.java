package com.consultafacil.application.service.conversation;

import com.consultafacil.api.dto.messaging.ConversationResponseDTO;
import com.consultafacil.application.port.in.ListConversationsUseCase;
import com.consultafacil.domain.port.out.ConversationRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ListConversationsService implements ListConversationsUseCase {

    private final ConversationRepositoryPort conversationRepository;
    private final ConversationMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public List<ConversationResponseDTO> execute(String userId) {
        return conversationRepository.findByUserId(userId).stream()
                .map(c -> mapper.toConversationDTO(c, userId))
                .toList();
    }
}
