package com.consultafacil.application.port.in;

public interface DeleteEmergencyContactUseCase {

    void execute(String userId, String contactId);
}
