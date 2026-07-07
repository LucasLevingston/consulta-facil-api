package com.consultafacil.application.port.in.clinic;

public interface AddClinicMemberUseCase {

    void execute(String clinicId, String professionalProfileId, String requesterId);
}
