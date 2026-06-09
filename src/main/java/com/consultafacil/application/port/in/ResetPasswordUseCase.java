package com.consultafacil.application.port.in;

public interface ResetPasswordUseCase {

    void execute(String rawToken, String newPassword);
}
