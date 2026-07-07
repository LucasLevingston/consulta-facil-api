package com.consultafacil.domain.repository.conversation;

import com.consultafacil.domain.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MessageRepository extends JpaRepository<Message, String> {

    @EntityGraph(attributePaths = {"sender"})
    Page<Message> findByConversationIdOrderBySentAtDesc(String conversationId, Pageable pageable);

    @Modifying
    @Query("UPDATE Message m SET m.readAt = CURRENT_TIMESTAMP WHERE m.conversation.id = :conversationId AND m.sender.id != :userId AND m.readAt IS NULL")
    void markAsRead(@Param("conversationId") String conversationId, @Param("userId") String userId);

    long countByConversationIdAndSenderIdNotAndReadAtIsNull(String conversationId, String userId);
}
