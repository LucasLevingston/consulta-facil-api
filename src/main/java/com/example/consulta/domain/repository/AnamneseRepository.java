package com.example.consulta.domain.repository;

import com.example.consulta.domain.entity.Anamnese;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AnamneseRepository extends JpaRepository<Anamnese, String> {
    Optional<Anamnese> findByAppointmentId(String appointmentId);
}
