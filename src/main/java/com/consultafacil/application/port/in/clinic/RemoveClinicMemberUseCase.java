package com.consultafacil.application.port.in.clinic;

public interface RemoveClinicMemberUseCase {

    void execute(String clinicId, String professionalProfileId, String requesterId);
}
