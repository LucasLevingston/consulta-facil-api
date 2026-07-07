package com.consultafacil.application.service;

import com.consultafacil.api.dto.messaging.MessageResponseDTO;
import com.consultafacil.domain.entity.Message;
import org.springframework.stereotype.Component;

@Component
public class MessageMapper {

    public MessageResponseDTO toDTO(Message m) {
        return new MessageResponseDTO(m.getId(), m.getSender().getId(), m.getSender().getName(),
                m.getContent(), m.getSentAt(), m.getReadAt() != null);
    }
}
