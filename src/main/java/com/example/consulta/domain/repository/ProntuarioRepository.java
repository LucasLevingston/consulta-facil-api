package com.example.consulta.domain.repository;

import com.example.consulta.domain.entity.Prontuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProntuarioRepository extends JpaRepository<Prontuario, String> {
    Optional<Prontuario> findByAppointmentId(String appointmentId);
}
