package com.consultafacil.domain.repository.prescription;

import com.consultafacil.domain.entity.Prescription;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PrescriptionRepository extends JpaRepository<Prescription, String> {

    List<Prescription> findByAppointmentIdOrderByCreatedAtDesc(String appointmentId);

    Page<Prescription> findByPatientIdOrderByCreatedAtDesc(String patientId, Pageable pageable);

    List<Prescription> findByProfessionalIdAndAppointmentId(String professionalId, String appointmentId);
}
