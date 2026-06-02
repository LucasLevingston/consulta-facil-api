package com.example.consulta.adapter.out.persistence;

import com.example.consulta.domain.entity.ClinicalNote;
import com.example.consulta.domain.port.out.ClinicalNoteRepositoryPort;
import com.example.consulta.domain.repository.ClinicalNoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ClinicalNoteRepositoryAdapter implements ClinicalNoteRepositoryPort {

    private final ClinicalNoteRepository clinicalNoteRepository;

    @Override
    public ClinicalNote save(ClinicalNote note) {
        return clinicalNoteRepository.save(note);
    }

    @Override
    public Optional<ClinicalNote> findByAppointmentId(String appointmentId) {
        return clinicalNoteRepository.findByAppointmentId(appointmentId);
    }
}
