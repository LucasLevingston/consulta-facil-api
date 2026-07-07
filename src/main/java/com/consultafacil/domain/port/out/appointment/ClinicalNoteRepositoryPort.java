package com.consultafacil.domain.port.out.appointment;

import com.consultafacil.domain.entity.ClinicalNote;

import java.util.Optional;

public interface ClinicalNoteRepositoryPort {

    ClinicalNote save(ClinicalNote note);

    Optional<ClinicalNote> findByAppointmentId(String appointmentId);
}
