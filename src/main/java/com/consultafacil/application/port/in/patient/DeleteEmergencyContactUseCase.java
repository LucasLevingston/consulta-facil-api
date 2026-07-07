package com.consultafacil.application.port.in.patient;

public interface DeleteEmergencyContactUseCase {

    void execute(String userId, String contactId);
}
