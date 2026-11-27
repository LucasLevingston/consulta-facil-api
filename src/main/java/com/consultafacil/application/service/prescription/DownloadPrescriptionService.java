package com.consultafacil.application.service.prescription;

import com.consultafacil.application.port.in.prescription.DownloadPrescriptionUseCase;
import com.consultafacil.core.exception.ResourceNotFoundException;
import com.consultafacil.domain.entity.Prescription;
import com.consultafacil.domain.port.out.prescription.PrescriptionRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DownloadPrescriptionService implements DownloadPrescriptionUseCase {

    private final PrescriptionRepositoryPort prescriptionRepository;

    @Override
    @Transactional(readOnly = true)
    public String execute(String userId, String prescriptionId) {
        Prescription prescription = prescriptionRepository.findById(prescriptionId)
                .orElseThrow(() -> new ResourceNotFoundException("Prescription", prescriptionId));
        return prescription.getPdfUrl();
    }
}
