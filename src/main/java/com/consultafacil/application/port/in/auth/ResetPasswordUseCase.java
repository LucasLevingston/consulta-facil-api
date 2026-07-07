package com.consultafacil.application.port.in.auth;

public interface ResetPasswordUseCase {

    void execute(String rawToken, String newPassword);
}
