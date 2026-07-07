package com.consultafacil.adapter.out.persistence.appointment;

import com.consultafacil.domain.entity.ClinicalNote;
import com.consultafacil.domain.port.out.appointment.ClinicalNoteRepositoryPort;
import com.consultafacil.domain.repository.appointment.ClinicalNoteRepository;
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
