package com.consultafacil.application.service.prescription;

import com.consultafacil.api.dto.prescription.PrescriptionDTO;
import com.consultafacil.application.port.in.prescription.ListAppointmentPrescriptionsUseCase;
import com.consultafacil.domain.port.out.prescription.PrescriptionRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ListAppointmentPrescriptionsService implements ListAppointmentPrescriptionsUseCase {

    private final PrescriptionRepositoryPort prescriptionRepository;

    @Override
    @Transactional(readOnly = true)
    public List<PrescriptionDTO> execute(String userId, String appointmentId) {
        return prescriptionRepository.findByAppointmentIdOrderByCreatedAtDesc(appointmentId)
                .stream()
                .map(p -> {
                    String professionalName = p.getProfessional() != null && p.getProfessional().getUser() != null
                            ? p.getProfessional().getUser().getName() : null;
                    String patientName = p.getPatient() != null && p.getPatient().getUser() != null
                            ? p.getPatient().getUser().getName() : null;
                    return IssuePrescriptionService.toDTO(p, professionalName, patientName);
                })
                .toList();
    }
}
