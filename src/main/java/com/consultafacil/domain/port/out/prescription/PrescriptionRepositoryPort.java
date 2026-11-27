package com.consultafacil.domain.port.out.prescription;

import com.consultafacil.domain.entity.Prescription;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface PrescriptionRepositoryPort {

    Prescription save(Prescription prescription);

    Optional<Prescription> findById(String id);

    List<Prescription> findByAppointmentIdOrderByCreatedAtDesc(String appointmentId);

    Page<Prescription> findByPatientIdOrderByCreatedAtDesc(String patientId, Pageable pageable);

    List<Prescription> findByProfessionalIdAndAppointmentId(String professionalId, String appointmentId);
}
