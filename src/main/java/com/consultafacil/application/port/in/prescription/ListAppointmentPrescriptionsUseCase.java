package com.consultafacil.application.port.in.prescription;

import com.consultafacil.api.dto.prescription.PrescriptionDTO;

import java.util.List;

public interface ListAppointmentPrescriptionsUseCase {
    List<PrescriptionDTO> execute(String userId, String appointmentId);
}
