package com.consultafacil.domain.port.out;

import com.consultafacil.domain.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MessageRepositoryPort {
    Message save(Message message);
    Page<Message> findByConversationId(String conversationId, Pageable pageable);
    void markAsRead(String conversationId, String userId);
    long countUnread(String conversationId, String userId);
}
