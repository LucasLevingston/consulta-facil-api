package com.consultafacil.application.port.in.prescription;

public interface DownloadPrescriptionUseCase {
    String execute(String userId, String prescriptionId);
}
