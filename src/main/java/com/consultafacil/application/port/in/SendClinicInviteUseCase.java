package com.consultafacil.application.port.in;

public interface SendClinicInviteUseCase {

    void execute(String clinicId, String professionalProfileId, String requesterId);
}
