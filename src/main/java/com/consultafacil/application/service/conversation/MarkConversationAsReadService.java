package com.consultafacil.application.service.conversation;

import com.consultafacil.application.port.in.MarkConversationAsReadUseCase;
import com.consultafacil.domain.port.out.MessageRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MarkConversationAsReadService implements MarkConversationAsReadUseCase {

    private final MessageRepositoryPort messageRepository;
    private final ConversationAccessValidator accessValidator;

    @Override
    @Transactional
    public void execute(String conversationId, String userId) {
        accessValidator.findAccessibleConversation(conversationId, userId);
        messageRepository.markAsRead(conversationId, userId);
    }
}
