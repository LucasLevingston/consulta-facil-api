package com.consultafacil.application.port.in.patient;

public interface DeleteVaccineUseCase {

    void execute(String userId, String vaccineId);
}
