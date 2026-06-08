package com.consultafacil.application.port.in;

public interface RemoveReceptionistUseCase {

    void execute(String clinicId, String receptionistId, String ownerUserId);
}
