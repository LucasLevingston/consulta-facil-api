package com.consultafacil.application.port.in.clinic;

public interface RemoveReceptionistUseCase {

    void execute(String clinicId, String receptionistId, String ownerUserId);
}
