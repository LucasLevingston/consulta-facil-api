package com.example.consulta.domain.repository;

import com.example.consulta.domain.entity.ClinicalNote;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ClinicalNoteRepository extends JpaRepository<ClinicalNote, String> {
    Optional<ClinicalNote> findByAppointmentId(String appointmentId);
}
