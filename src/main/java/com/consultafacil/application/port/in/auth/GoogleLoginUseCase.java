package com.consultafacil.application.port.in.auth;

import com.consultafacil.api.dto.auth.LoginResponseDTO;

public interface GoogleLoginUseCase {
    LoginResponseDTO execute(String idToken);
}
