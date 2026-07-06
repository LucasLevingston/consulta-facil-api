package com.consultafacil.application.port.in;

public interface RemoveClinicMemberUseCase {

    void execute(String clinicId, String professionalProfileId, String requesterId);
}
