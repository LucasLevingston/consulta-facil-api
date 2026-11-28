package com.consultafacil.application.port.in.auth;

public interface LogoutUseCase {

    void execute(String bearerToken);
}
