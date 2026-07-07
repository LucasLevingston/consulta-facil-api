package com.consultafacil.application.service;

import com.consultafacil.api.dto.patient.PatientDocumentResponseDTO;
import com.consultafacil.application.port.in.ListDocumentsUseCase;
import com.consultafacil.domain.entity.PatientProfile;
import com.consultafacil.domain.port.out.PatientDocumentRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ListDocumentsService implements ListDocumentsUseCase {

    private final PatientProfileFinder profileFinder;
    private final PatientDocumentRepositoryPort documentRepository;
    private final PatientDocumentMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public List<PatientDocumentResponseDTO> execute(String userId) {
        PatientProfile profile = profileFinder.findOrThrow(userId);
        return documentRepository.findByPatientProfileId(profile.getId())
                .stream().map(mapper::toDTO).toList();
    }
}
