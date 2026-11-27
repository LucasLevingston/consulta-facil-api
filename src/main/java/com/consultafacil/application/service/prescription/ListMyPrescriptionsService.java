package com.consultafacil.application.service.prescription;

import com.consultafacil.api.dto.prescription.PrescriptionDTO;
import com.consultafacil.application.port.in.prescription.ListMyPrescriptionsUseCase;
import com.consultafacil.core.exception.ResourceNotFoundException;
import com.consultafacil.domain.port.out.patient.PatientProfileRepositoryPort;
import com.consultafacil.domain.port.out.prescription.PrescriptionRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ListMyPrescriptionsService implements ListMyPrescriptionsUseCase {

    private final PatientProfileRepositoryPort patientRepository;
    private final PrescriptionRepositoryPort prescriptionRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<PrescriptionDTO> execute(String patientUserId, Pageable pageable) {
        String patientId = patientRepository.findByUserId(patientUserId)
                .orElseThrow(() -> new ResourceNotFoundException("PatientProfile", patientUserId))
                .getId();
        return prescriptionRepository.findByPatientIdOrderByCreatedAtDesc(patientId, pageable)
                .map(p -> {
                    String professionalName = p.getProfessional() != null && p.getProfessional().getUser() != null
                            ? p.getProfessional().getUser().getName() : null;
                    String patientName = p.getPatient() != null && p.getPatient().getUser() != null
                            ? p.getPatient().getUser().getName() : null;
                    return IssuePrescriptionService.toDTO(p, professionalName, patientName);
                });
    }
}
