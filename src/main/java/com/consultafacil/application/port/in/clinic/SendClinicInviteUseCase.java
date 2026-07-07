package com.consultafacil.application.port.in.clinic;

public interface SendClinicInviteUseCase {

    void execute(String clinicId, String professionalProfileId, String requesterId);
}
