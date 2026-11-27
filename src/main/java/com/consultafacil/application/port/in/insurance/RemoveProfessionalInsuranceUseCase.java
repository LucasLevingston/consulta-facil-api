package com.consultafacil.application.port.in.insurance;

public interface RemoveProfessionalInsuranceUseCase {

    void execute(String professionalId, String planId);
}
