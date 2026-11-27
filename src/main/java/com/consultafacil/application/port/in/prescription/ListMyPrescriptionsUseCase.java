package com.consultafacil.application.port.in.prescription;

import com.consultafacil.api.dto.prescription.PrescriptionDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ListMyPrescriptionsUseCase {
    Page<PrescriptionDTO> execute(String patientUserId, Pageable pageable);
}
