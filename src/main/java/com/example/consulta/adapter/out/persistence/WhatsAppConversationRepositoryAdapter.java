package com.example.consulta.adapter.out.persistence;

import com.example.consulta.domain.entity.WhatsAppConversation;
import com.example.consulta.domain.port.out.WhatsAppConversationRepositoryPort;
import com.example.consulta.domain.repository.WhatsAppConversationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class WhatsAppConversationRepositoryAdapter implements WhatsAppConversationRepositoryPort {

    private final WhatsAppConversationRepository whatsAppConversationRepository;

    @Override
    public WhatsAppConversation save(WhatsAppConversation conversation) {
        return whatsAppConversationRepository.save(conversation);
    }

    @Override
    public Optional<WhatsAppConversation> findByPhoneNumber(String phoneNumber) {
        return whatsAppConversationRepository.findByPhoneNumber(phoneNumber);
    }
}
