package com.consultafacil.adapter.out.persistence;

import com.consultafacil.domain.entity.Conversation;
import com.consultafacil.domain.port.out.ConversationRepositoryPort;
import com.consultafacil.domain.repository.ConversationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ConversationRepositoryAdapter implements ConversationRepositoryPort {

    private final ConversationRepository repository;

    @Override
    public Conversation save(Conversation conversation) {
        return repository.save(conversation);
    }

    @Override
    public Optional<Conversation> findByPatientIdAndProfessionalId(String patientId, String professionalId) {
        return repository.findByPatientIdAndProfessionalId(patientId, professionalId);
    }

    @Override
    public List<Conversation> findByUserId(String userId) {
        return repository.findByUserId(userId);
    }

    @Override
    public Optional<Conversation> findById(String id) {
        return repository.findById(id);
    }
}
