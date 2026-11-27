package com.consultafacil.adapter.out.persistence.prescription;

import com.consultafacil.domain.entity.Prescription;
import com.consultafacil.domain.port.out.prescription.PrescriptionRepositoryPort;
import com.consultafacil.domain.repository.prescription.PrescriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class PrescriptionRepositoryAdapter implements PrescriptionRepositoryPort {

    private final PrescriptionRepository prescriptionRepository;

    @Override
    public Prescription save(Prescription prescription) {
        return prescriptionRepository.save(prescription);
    }

    @Override
    public Optional<Prescription> findById(String id) {
        return prescriptionRepository.findById(id);
    }

    @Override
    public List<Prescription> findByAppointmentIdOrderByCreatedAtDesc(String appointmentId) {
        return prescriptionRepository.findByAppointmentIdOrderByCreatedAtDesc(appointmentId);
    }

    @Override
    public Page<Prescription> findByPatientIdOrderByCreatedAtDesc(String patientId, Pageable pageable) {
        return prescriptionRepository.findByPatientIdOrderByCreatedAtDesc(patientId, pageable);
    }

    @Override
    public List<Prescription> findByProfessionalIdAndAppointmentId(String professionalId, String appointmentId) {
        return prescriptionRepository.findByProfessionalIdAndAppointmentId(professionalId, appointmentId);
    }
}
