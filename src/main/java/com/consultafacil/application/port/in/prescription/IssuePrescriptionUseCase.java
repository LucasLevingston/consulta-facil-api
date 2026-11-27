package com.consultafacil.application.port.in.prescription;

import com.consultafacil.api.dto.prescription.IssuePrescriptionRequest;
import com.consultafacil.api.dto.prescription.PrescriptionDTO;

public interface IssuePrescriptionUseCase {
    PrescriptionDTO execute(String professionalUserId, IssuePrescriptionRequest request);
}
