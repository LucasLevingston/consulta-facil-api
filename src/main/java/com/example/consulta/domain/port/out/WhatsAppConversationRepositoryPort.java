package com.example.consulta.domain.port.out;

import com.example.consulta.domain.entity.WhatsAppConversation;

import java.util.Optional;

public interface WhatsAppConversationRepositoryPort {

    WhatsAppConversation save(WhatsAppConversation conversation);

    Optional<WhatsAppConversation> findByPhoneNumber(String phoneNumber);
}
