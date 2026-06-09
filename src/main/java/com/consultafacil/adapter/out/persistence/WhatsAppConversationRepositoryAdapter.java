package com.consultafacil.adapter.out.persistence;

import com.consultafacil.domain.entity.WhatsAppConversation;
import com.consultafacil.domain.port.out.WhatsAppConversationRepositoryPort;
import com.consultafacil.domain.repository.WhatsAppConversationRepository;
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
