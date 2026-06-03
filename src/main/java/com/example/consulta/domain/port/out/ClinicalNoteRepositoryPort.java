package com.example.consulta.domain.port.out;

import com.example.consulta.domain.entity.ClinicalNote;

import java.util.Optional;

public interface ClinicalNoteRepositoryPort {

    ClinicalNote save(ClinicalNote note);

    Optional<ClinicalNote> findByAppointmentId(String appointmentId);
}
