package com.consultafacil.application.service;

import com.consultafacil.core.exception.ResourceNotFoundException;
import com.consultafacil.domain.entity.EmergencyContact;
import com.consultafacil.domain.port.out.EmergencyContactRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EmergencyContactFinder {

    private final EmergencyContactRepositoryPort emergencyContactRepository;

    public EmergencyContact findOrThrow(String contactId) {
        return emergencyContactRepository.findById(contactId)
                .orElseThrow(() -> new ResourceNotFoundException("EmergencyContact", contactId));
    }
}
