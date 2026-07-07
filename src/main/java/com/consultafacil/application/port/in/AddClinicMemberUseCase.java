package com.consultafacil.application.port.in;

public interface AddClinicMemberUseCase {

    void execute(String clinicId, String professionalProfileId, String requesterId);
}
