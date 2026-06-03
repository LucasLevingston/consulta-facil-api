package com.example.consulta.application.port.in;

public interface ResetPasswordUseCase {

    void execute(String rawToken, String newPassword);
}
