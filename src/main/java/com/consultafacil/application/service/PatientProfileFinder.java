package com.consultafacil.application.service;

import com.consultafacil.core.exception.ResourceNotFoundException;
import com.consultafacil.domain.entity.PatientProfile;
import com.consultafacil.domain.port.out.PatientProfileRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PatientProfileFinder {

    private final PatientProfileRepositoryPort patientProfileRepository;

    public PatientProfile findOrThrow(String userId) {
        return patientProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient profile", userId));
    }
}
