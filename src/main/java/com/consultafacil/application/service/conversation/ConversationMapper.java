package com.consultafacil.application.service.conversation;

import com.consultafacil.api.dto.messaging.ConversationResponseDTO;
import com.consultafacil.api.dto.messaging.MessageResponseDTO;
import com.consultafacil.domain.entity.Conversation;
import com.consultafacil.domain.entity.User;
import com.consultafacil.domain.port.out.MessageRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ConversationMapper {

    private final MessageRepositoryPort messageRepository;
    private final MessageMapper messageMapper;

    public ConversationResponseDTO toConversationDTO(Conversation c, String viewerId) {
        User other = c.getPatient().getId().equals(viewerId) ? c.getProfessional() : c.getPatient();
        long unread = messageRepository.countUnread(c.getId(), viewerId);

        Page<MessageResponseDTO> lastPage = messageRepository.findByConversationId(
                c.getId(), Pageable.ofSize(1)).map(messageMapper::toDTO);
        MessageResponseDTO lastMessage = lastPage.isEmpty() ? null : lastPage.getContent().get(0);

        return new ConversationResponseDTO(c.getId(), other.getId(), other.getName(),
                other.getImageUrl(), lastMessage, unread);
    }
}
