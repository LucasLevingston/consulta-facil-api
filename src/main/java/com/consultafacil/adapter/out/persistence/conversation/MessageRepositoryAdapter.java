package com.consultafacil.adapter.out.persistence.conversation;

import com.consultafacil.domain.entity.Message;
import com.consultafacil.domain.port.out.conversation.MessageRepositoryPort;
import com.consultafacil.domain.repository.conversation.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MessageRepositoryAdapter implements MessageRepositoryPort {

    private final MessageRepository repository;

    @Override
    public Message save(Message message) {
        return repository.save(message);
    }

    @Override
    public Page<Message> findByConversationId(String conversationId, Pageable pageable) {
        return repository.findByConversationIdOrderBySentAtDesc(conversationId, pageable);
    }

    @Override
    public void markAsRead(String conversationId, String userId) {
        repository.markAsRead(conversationId, userId);
    }

    @Override
    public long countUnread(String conversationId, String userId) {
        return repository.countByConversationIdAndSenderIdNotAndReadAtIsNull(conversationId, userId);
    }
}
